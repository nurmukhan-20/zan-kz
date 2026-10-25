package kz.zankz.server.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import tools.jackson.databind.json.JsonMapper;

import kz.zankz.server.dto.AssistantResponse;
import kz.zankz.server.dto.DocumentRequest;
import kz.zankz.server.dto.CompareResponse;
import kz.zankz.server.dto.DocumentResponse;
import kz.zankz.server.dto.QuizResponse;
import kz.zankz.server.dto.SituationResponse;
import kz.zankz.server.exception.BadRequestException;
import kz.zankz.server.dto.AssistantResponse.ArticleRef;
import kz.zankz.server.model.Article;
import kz.zankz.server.model.Term;
import kz.zankz.server.repository.ArticleRepository;
import kz.zankz.server.repository.TermRepository;

// ИИ-помощник: 1) находит в базе подходящие статьи и термины, 2) отправляет их в Gemini,
// 3) если ответ есть в статьях — Gemini отвечает по ним со ссылками;
//    если нет — честно пишет «в базе нет» и даёт общую информацию с пометкой,
// 4) возвращаем ответ + ссылки на статьи.
@Service
public class AssistantService {

    private static final int MAX_ARTICLES = 6;      // сколько статей отправляем в Gemini
    private static final int MAX_TEXT = 2500;       // сколько символов текста одной статьи
    private static final int MAX_TERMS = 4;         // сколько терминов из словаря отправляем

    // частые слова, которые не помогают искать
    private static final Set<String> STOP = Set.of(
            "что", "как", "если", "это", "мне", "меня", "можно", "нужно", "есть", "или", "для", "при", "его", "она",
            "они", "мой", "моя", "мою", "мои", "все", "так", "уже", "быть", "будет", "какой", "какие", "какая", "где",
            "когда", "почему", "делать", "закон", "статья", "статьи",
            "және", "мен", "бар", "жоқ", "үшін", "қалай", "қандай", "неге", "керек", "болады", "бола", "менің", "маған",
            "деген", "заң", "бап", "туралы",
            "what", "how", "can", "the", "and", "for", "with", "that", "this", "does", "have", "about", "law", "article");

    private static final Pattern PERSONAL = Pattern.compile("(?im)^\\s*\\[?PERSONAL\\]?\\s*:?\\s*(yes|no)\\s*\\.?\\s*$");
    private static final Pattern SOURCES = Pattern.compile("(?im)^\\s*\\[?SOURCES\\]?\\s*:?\\s*(.*)$");

    private final ArticleRepository articleRepository;
    private final TermRepository termRepository;
    private final GeminiClient gemini;

    public AssistantService(ArticleRepository articleRepository, TermRepository termRepository, GeminiClient gemini) {
        this.articleRepository = articleRepository;
        this.termRepository = termRepository;
        this.gemini = gemini;
    }

    @Transactional(readOnly = true)
    public AssistantResponse ask(String question, String lang) {
        String l = normalizeLang(lang);
        Set<String> stems = new HashSet<>();
        Set<String> numbers = new HashSet<>();
        splitWords(question, stems, numbers);

        List<Article> found = findArticles(stems, numbers);
        List<Term> terms = findTerms(stems);

        String raw = gemini.ask(instruction(l, found, terms), question.trim());

        // строка «PERSONAL: yes/no» — вопрос про личную ситуацию человека? (тогда нужно предупреждение)
        boolean personal = false;
        Matcher p = PERSONAL.matcher(raw);
        if (p.find()) {
            personal = p.group(1).equalsIgnoreCase("yes");
            raw = (raw.substring(0, p.start()) + raw.substring(p.end())).trim();
        }

        // Gemini в конце пишет строку «SOURCES: 12, 45» (или «SOURCES: none») — id статей, которые он использовал
        Set<Long> used = new LinkedHashSet<>();
        Matcher m = SOURCES.matcher(raw);
        String answer = raw;
        boolean hasLine = m.find();
        if (hasLine) {
            for (String part : m.group(1).split("[^0-9]+")) {
                if (!part.isBlank()) used.add(Long.parseLong(part));
            }
            answer = raw.substring(0, m.start()).trim();
        }

        List<ArticleRef> refs = new ArrayList<>();
        for (Article a : found) {
            if (!hasLine || used.contains(a.getId())) refs.add(ArticleRef.of(a));
        }
        // предупреждение показываем только когда нужно:
        // общий ответ (не по базе) — всегда; ответ по базе — только если вопрос про личную ситуацию
        String note = refs.isEmpty() ? text(l, GENERAL_NOTE) : personal ? text(l, DISCLAIMER) : null;
        return new AssistantResponse(note == null ? answer : answer + "\n\n" + note, refs);
    }

    // ---------- Документы: заявление, претензия, расписка ----------

    // тип документа -> [что составить (для Gemini), слова для поиска статей]
    private static final Map<String, String[]> DOC_TYPES = Map.of(
            "VACATION", new String[] { "an application (заявление) to the employer for annual paid leave",
                    "ежегодный оплачиваемый трудовой отпуск продолжительность" },
            "RESIGNATION", new String[] { "an application (заявление) to the employer for dismissal at the employee's own request",
                    "расторжение трудового договора по инициативе работника увольнение" },
            "CLAIM", new String[] { "a pre-trial claim (претензия) to a seller about a defective product: return, exchange or refund",
                    "потребитель товар ненадлежащего качества возврат продавец" },
            "RECEIPT", new String[] { "a receipt (расписка) confirming that one person received money from another as a loan, with the return date",
                    "договор займа заем возврат денег" });

    private static final Map<String, Map<String, String>> DOC_TITLES = Map.of(
            "VACATION", Map.of("kk", "Демалысқа өтініш", "ru", "Заявление на отпуск", "en", "Leave application"),
            "RESIGNATION", Map.of("kk", "Жұмыстан босату туралы өтініш", "ru", "Заявление об увольнении", "en", "Resignation letter"),
            "CLAIM", Map.of("kk", "Сатушыға талап-арыз (претензия)", "ru", "Претензия продавцу", "en", "Claim to a seller"),
            "RECEIPT", Map.of("kk", "Ақша алғаны туралы қолхат", "ru", "Расписка о получении денег", "en", "Money receipt"));

    @Transactional(readOnly = true)
    public DocumentResponse document(DocumentRequest r) {
        String l = normalizeLang(r.lang());
        String type = r.type() == null ? "" : r.type().trim().toUpperCase(Locale.ROOT);
        String[] doc = DOC_TYPES.get(type);
        if (doc == null) throw new BadRequestException("Неизвестный тип документа");

        Set<String> stems = new HashSet<>();
        Set<String> numbers = new HashSet<>();
        splitWords(doc[1] + " " + r.details(), stems, numbers);
        List<Article> found = findArticles(stems, Set.of());

        String instruction = """
                You draft simple document templates for citizens of the Republic of Kazakhstan.
                Write %s in %s.
                Rules:
                1. Official business style used in Kazakhstan. Structure: "to whom / from whom" lines at the top,
                   the document name in the middle (for example ЗАЯВЛЕНИЕ / ӨТІНІШ / РАСПИСКА), the main text,
                   then date and signature lines.
                2. Use only the data the user gave. Where data is missing, put "________".
                3. Never write ИИН, passport numbers, phone numbers or addresses — always leave "________" for them.
                4. Do not invent facts. If one of the ARTICLES below fits, refer to it in the text
                   (for example "в соответствии со статьёй 88 Трудового кодекса РК"); otherwise do not cite article numbers.
                5. Output only the document text: no markdown, no ** or #, no comments before or after.
                6. On the LAST line write: SOURCES: <ids of the articles you referred to>, or SOURCES: none
                ARTICLES:
                """.formatted(doc[0], languageName(l)) + articlesBlock(l, found);

        String user = "To whom: " + blankIfEmpty(r.to()) + "\nFrom whom: " + blankIfEmpty(r.from())
                + "\nDetails: " + r.details().trim();

        String raw = gemini.ask(instruction, user);

        Set<Long> used = new LinkedHashSet<>();
        Matcher m = SOURCES.matcher(raw);
        String text = raw;
        if (m.find()) {
            for (String part : m.group(1).split("[^0-9]+")) {
                if (!part.isBlank()) used.add(Long.parseLong(part));
            }
            text = raw.substring(0, m.start());
        }
        text = text.replace("```", "").replace("**", "").trim();

        List<ArticleRef> refs = new ArrayList<>();
        for (Article a : found) {
            if (used.contains(a.getId())) refs.add(ArticleRef.of(a));
        }
        return new DocumentResponse(DOC_TITLES.get(type).get(l), text, refs);
    }

    private static String blankIfEmpty(String s) {
        return s == null || s.isBlank() ? "________" : s.trim();
    }

    // ---------- Поиск по жизненной ситуации ----------

    private static final int SITUATION_CANDIDATES = 10;   // сколько статей-кандидатов даём ИИ на выбор

    @JsonIgnoreProperties(ignoreUnknown = true)
    record SituationItem(Long id, String why) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record SituationJson(String summary, List<String> steps, List<SituationItem> articles) {
    }

    @Transactional(readOnly = true)
    public SituationResponse situation(String situation, String lang) {
        String l = normalizeLang(lang);
        Set<String> stems = new HashSet<>();
        Set<String> numbers = new HashSet<>();
        splitWords(situation, stems, numbers);
        List<Article> candidates = findArticles(stems, numbers, SITUATION_CANDIDATES);

        String instruction = """
                You help ordinary people in Kazakhstan understand their life situation from the point of view of the law.
                Answer in %s, in simple words.
                1. "summary": 2–3 sentences — what this situation is legally, without judging the person.
                2. "steps": 3–5 short practical steps — what the person can do (whom to contact, what to ask, what to keep).
                3. "articles": choose ONLY the ARTICLES below that really fit this situation (0 to 4). For each give its "id"
                   and "why" — one sentence why it fits. If none fit, return an empty list. Never invent ids.
                4. If no article fits, base summary and steps on general knowledge of Kazakhstan law, without exact article
                   numbers or amounts you are not sure about.
                5. Do not help to commit crimes or hide them. Do not ask for personal data (ИИН, address, phone).
                Answer ONLY with JSON, no markdown:
                {"summary":"...","steps":["...","..."],"articles":[{"id":12,"why":"..."}]}
                ARTICLES:
                """.formatted(languageName(l)) + articlesBlock(l, candidates);

        String raw = gemini.ask(instruction, situation.trim());

        SituationJson parsed;
        try {
            int from = raw.indexOf('{'), to = raw.lastIndexOf('}');
            parsed = JSON.readValue(raw.substring(from, to + 1), SituationJson.class);
        } catch (RuntimeException e) {
            throw new BadRequestException("ИИ ответил в неверном формате. Нажмите «Разобрать» ещё раз.");
        }

        List<SituationResponse.Match> matches = new ArrayList<>();
        if (parsed.articles() != null) {
            for (SituationItem item : parsed.articles()) {
                for (Article a : candidates) {
                    if (item != null && a.getId().equals(item.id())) {
                        matches.add(new SituationResponse.Match(ArticleRef.of(a), item.why() == null ? "" : item.why()));
                    }
                }
            }
        }
        List<String> steps = parsed.steps() == null ? List.of() : parsed.steps();
        String summary = parsed.summary() == null ? "" : parsed.summary();
        // личная ситуация — предупреждение нужно всегда; без статей — строже
        String note = matches.isEmpty() ? text(l, GENERAL_NOTE) : text(l, DISCLAIMER);
        return new SituationResponse(summary, steps, matches, note);
    }

    // ---------- Сравнение двух статей ----------

    @Transactional(readOnly = true)
    public CompareResponse compare(List<Long> ids, String lang) {
        String l = normalizeLang(lang);
        if (ids == null || ids.size() != 2 || ids.get(0) == null || ids.get(1) == null) {
            throw new BadRequestException("Выберите две статьи");
        }
        if (ids.get(0).equals(ids.get(1))) throw new BadRequestException("Выберите две разные статьи");

        List<Article> pair = new ArrayList<>();
        for (Long id : ids) {
            pair.add(articleRepository.findById(id)
                    .orElseThrow(() -> new BadRequestException("Статья не найдена: " + id)));
        }

        String instruction = """
                You explain the laws of the Republic of Kazakhstan to ordinary people.
                Compare the two ARTICLES below in %s, in simple words, using ONLY their text.
                Structure (headings in **bold**, points start with "• "):
                **<short name of article 1>** — 1–2 sentences: what it is about.
                **<short name of article 2>** — 1–2 sentences: what it is about.
                **Общее** — what they have in common (translate this heading into the answer language).
                **Разница** — the main differences: who it applies to, rights/duties, terms, amounts, consequences.
                **Что это значит на практике** — 1–2 sentences with a simple life example.
                If the articles are about completely different things, say so honestly and explain each one briefly.
                Mention article numbers and laws (for example "ст. 88 Трудового кодекса"). Do not invent anything.
                Keep it short: up to 15 lines. Do not write a SOURCES line.
                ARTICLES:
                """.formatted(languageName(l)) + articlesBlock(l, pair);

        String text = gemini.ask(instruction, "Compare these two articles.").replace("```", "").trim();
        return new CompareResponse(text, pair.stream().map(ArticleRef::of).toList());
    }

    // ---------- Тест по статьям ----------

    private static final int QUIZ_SIZE = 5;
    private static final JsonMapper JSON = JsonMapper.builder().build();

    // так Gemini возвращает один вопрос (JSON)
    @JsonIgnoreProperties(ignoreUnknown = true)
    record QuizItem(String question, List<String> options, Integer answer, String explanation, Long articleId) {
    }

    @Transactional(readOnly = true)
    public QuizResponse quiz(Long categoryId, String lang) {
        String l = normalizeLang(lang);

        // берём случайные статьи нужной темы (или всех тем)
        List<Article> pool = new ArrayList<>();
        for (Article a : articleRepository.findAll()) {
            if (categoryId == null || (a.getLaw().getCategory() != null
                    && categoryId.equals(a.getLaw().getCategory().getId()))) {
                pool.add(a);
            }
        }
        if (pool.isEmpty()) throw new BadRequestException("В этой теме пока нет статей");
        Collections.shuffle(pool);
        List<Article> chosen = pool.subList(0, Math.min(QUIZ_SIZE, pool.size()));

        String instruction = """
                You create a short quiz to teach the laws of the Republic of Kazakhstan.
                Make exactly %d multiple-choice questions in %s — one question for each ARTICLE below.
                Rules:
                1. Each question must be answered ONLY from its article. Ask about something useful for ordinary people
                   (rights, terms, ages, amounts, who decides), not about article numbers.
                2. Exactly 4 options, one correct. Wrong options must be believable. Put the correct one in a random place.
                3. "explanation" — 1–2 simple sentences why the answer is correct.
                4. Answer ONLY with a JSON array, no markdown, in this format:
                [{"question":"...","options":["...","...","...","..."],"answer":0,"explanation":"...","articleId":12}]
                "answer" is the index of the correct option (0-3), "articleId" is the id of the article.
                ARTICLES:
                """.formatted(chosen.size(), languageName(l)) + articlesBlock(l, chosen);

        String raw = gemini.ask(instruction, "Create the quiz.");

        QuizItem[] items;
        try {
            int from = raw.indexOf('['), to = raw.lastIndexOf(']');
            items = JSON.readValue(raw.substring(from, to + 1), QuizItem[].class);
        } catch (RuntimeException e) {
            throw new BadRequestException("ИИ ответил в неверном формате. Нажмите «Начать тест» ещё раз.");
        }

        List<QuizResponse.Question> questions = new ArrayList<>();
        for (QuizItem it : items) {
            if (it.question() == null || it.options() == null || it.options().size() != 4
                    || it.answer() == null || it.answer() < 0 || it.answer() > 3) continue;   // пропускаем кривые вопросы
            ArticleRef ref = null;
            for (Article a : chosen) {
                if (a.getId().equals(it.articleId())) ref = ArticleRef.of(a);
            }
            questions.add(new QuizResponse.Question(it.question(), it.options(), it.answer(),
                    it.explanation() == null ? "" : it.explanation(), ref));
        }
        if (questions.isEmpty()) throw new BadRequestException("Не получилось составить тест. Попробуйте ещё раз.");
        return new QuizResponse(questions);
    }

    // ---------- 1. Поиск статей и терминов по словам вопроса ----------

    private static void splitWords(String question, Set<String> stems, Set<String> numbers) {
        for (String w : question.toLowerCase(Locale.ROOT).split("[^\\p{L}\\p{N}]+")) {
            if (w.isBlank()) continue;
            if (w.chars().allMatch(Character::isDigit)) { numbers.add(w); continue; }
            if (w.length() < 3 || STOP.contains(w)) continue;
            stems.add(stem(w));
        }
    }

    private List<Article> findArticles(Set<String> stems, Set<String> numbers) {
        return findArticles(stems, numbers, MAX_ARTICLES);
    }

    private List<Article> findArticles(Set<String> stems, Set<String> numbers, int limit) {
        if (stems.isEmpty() && numbers.isEmpty()) return List.of();

        record Scored(Article article, int score) { }
        List<Scored> scored = new ArrayList<>();
        for (Article a : articleRepository.findAll()) {
            String title = lower(a.getTitleKk(), a.getTitleRu(), a.getTitleEn(),
                    a.getLaw().getTitleKk(), a.getLaw().getTitleRu(), a.getLaw().getTitleEn());
            String body = lower(a.getTextKk(), a.getTextRu(), a.getTextEn(),
                    a.getExplanationKk(), a.getExplanationRu(), a.getExplanationEn());
            int score = 0;
            for (String s : stems) {
                if (title.contains(s)) score += 3;      // слово в названии — важнее
                else if (body.contains(s)) score += 1;
            }
            for (String n : numbers) {
                if (n.equals(a.getArticleNumber())) score += 4;   // «статья 152»
            }
            if (score > 0) scored.add(new Scored(a, score));
        }
        scored.sort(Comparator.comparingInt(Scored::score).reversed());
        return scored.stream().limit(limit).map(Scored::article).toList();
    }

    // термины из словаря, в названии которых есть слово из вопроса
    private List<Term> findTerms(Set<String> stems) {
        if (stems.isEmpty()) return List.of();
        List<Term> result = new ArrayList<>();
        for (Term t : termRepository.findAll()) {
            String name = lower(t.getTermKk(), t.getTermRu(), t.getTermEn());
            for (String s : stems) {
                if (name.contains(s)) { result.add(t); break; }
            }
            if (result.size() >= MAX_TERMS) break;
        }
        return result;
    }

    // простая «основа» слова: отрезаем окончание, чтобы «уволить» и «увольнение» совпали
    private static String stem(String w) {
        if (w.length() <= 4) return w;
        int keep = Math.max(4, w.length() - 3);
        return w.substring(0, Math.min(keep, 7));
    }

    private static String lower(String... parts) {
        StringBuilder sb = new StringBuilder();
        for (String p : parts) if (p != null) sb.append(p).append(' ');
        return sb.toString().toLowerCase(Locale.ROOT);
    }

    // ---------- 2. Инструкция для Gemini ----------

    private String instruction(String l, List<Article> articles, List<Term> terms) {
        String language = languageName(l);
        StringBuilder sb = new StringBuilder();
        sb.append("""
                You are the legal assistant of the "Zan KZ" app about the laws of the Republic of Kazakhstan.
                Rules:
                1. Answer in %s, in simple words, short (up to 8 sentences), you may use a short list.
                2. If the ARTICLES below answer the question — answer using them and mention the article number
                   and the law, for example: "ст. 152, Трудовой кодекс".
                3. If the articles below do NOT answer the question — start the answer with exactly this sentence:
                   "%s"
                   Then give short general information about how this works under the laws of Kazakhstan.
                   Do NOT invent article numbers, exact terms or amounts if you are not sure — say "примерно" / "обычно"
                   and advise checking the official text on adilet.zan.kz.
                4. If the user asks what a legal term means — explain it simply. If the term is in the GLOSSARY below,
                   use that definition first; if not, explain it from general knowledge.
                5. Do not help to commit crimes or avoid responsibility; explain only what the law says and the consequences.
                6. Do not ask the user for personal data (ИИН, address, phone).
                7. On the second to last line write: PERSONAL: yes — if the user describes his own situation or asks what he
                   should do (dismissal, divorce, fine, conflict, court, money); PERSONAL: no — if it is a general question
                   about what a law, article or term says.
                8. On the LAST line write: SOURCES: <ids of the articles you used, comma separated>, or SOURCES: none
                ARTICLES:
                """.formatted(language, text(l, NOT_IN_BASE)));
        sb.append(articlesBlock(l, articles));
        sb.append("\nGLOSSARY:\n");
        if (terms.isEmpty()) sb.append("(no matching terms)\n");
        for (Term t : terms) {
            sb.append("- ").append(pick(l, t.getTermKk(), t.getTermRu(), t.getTermEn())).append(": ")
              .append(cut(pick(l, t.getDefinitionKk(), t.getDefinitionRu(), t.getDefinitionEn()))).append('\n');
        }
        return sb.toString();
    }

    // список статей для Gemini: [id=..] закон, номер — название, текст, объяснение
    private static String articlesBlock(String l, List<Article> articles) {
        StringBuilder sb = new StringBuilder();
        if (articles.isEmpty()) sb.append("(no matching articles)\n");
        for (Article a : articles) {
            sb.append("\n[id=").append(a.getId()).append("] ")
              .append(pick(l, a.getLaw().getTitleKk(), a.getLaw().getTitleRu(), a.getLaw().getTitleEn()))
              .append(", ").append(a.getArticleNumber() == null ? "" : "ст. " + a.getArticleNumber()).append(" — ")
              .append(pick(l, a.getTitleKk(), a.getTitleRu(), a.getTitleEn())).append('\n')
              .append(cut(pick(l, a.getTextKk(), a.getTextRu(), a.getTextEn()))).append('\n');
            String expl = pick(l, a.getExplanationKk(), a.getExplanationRu(), a.getExplanationEn());
            if (expl != null && !expl.isBlank()) sb.append("Explanation: ").append(cut(expl)).append('\n');
        }
        return sb.toString();
    }

    private static String languageName(String l) {
        return switch (l) {
            case "kk" -> "Kazakh (қазақ тілі)";
            case "en" -> "English";
            default -> "Russian";
        };
    }

    // текст на нужном языке; если его нет — русский, потом любой
    private static String pick(String l, String kk, String ru, String en) {
        String v = switch (l) { case "kk" -> kk; case "en" -> en; default -> ru; };
        if (v == null || v.isBlank()) v = ru;
        if (v == null || v.isBlank()) v = kk != null && !kk.isBlank() ? kk : en;
        return v == null ? "" : v;
    }

    private static String cut(String s) {
        return s.length() <= MAX_TEXT ? s : s.substring(0, MAX_TEXT) + "…";
    }

    private static String normalizeLang(String lang) {
        return lang != null && (lang.equals("kk") || lang.equals("en")) ? lang : "ru";
    }

    // ---------- 3. Готовые фразы на трёх языках ----------

    // первая фраза, если в базе нет подходящей статьи
    private static final Map<String, String> NOT_IN_BASE = Map.of(
            "kk", "Заң KZ базасында бұл сұрақ бойынша бап әзірге жоқ. Жалпы ақпарат:",
            "ru", "В базе Заң KZ пока нет статьи по этому вопросу. Общая информация:",
            "en", "The Zan KZ database has no article on this question yet. General information:");

    // предупреждение для общего ответа (не по статьям базы)
    private static final Map<String, String> GENERAL_NOTE = Map.of(
            "kk", "⚠️ Бұл жауап Заң KZ баптары бойынша емес, жалпы ақпарат және дәл болмауы мүмкін. Ресми мәтінді adilet.zan.kz сайтынан тексеріңіз немесе заңгерге жүгініңіз.",
            "ru", "⚠️ Этот ответ — общая информация, не по статьям базы Заң KZ, и может быть неточным. Проверьте официальный текст на adilet.zan.kz или обратитесь к юристу.",
            "en", "⚠️ This answer is general information, not based on Zan KZ articles, and may be inaccurate. Check the official text on adilet.zan.kz or consult a lawyer.");

    private static final Map<String, String> DISCLAIMER = Map.of(
            "kk", "⚠️ Бұл ресми заң кеңесі емес. Маңызды мәселе бойынша заңгерге жүгініңіз.",
            "ru", "⚠️ Это не официальная юридическая консультация. По важным вопросам обратитесь к юристу.",
            "en", "⚠️ This is not official legal advice. For important matters, consult a lawyer.");

    private static String text(String l, Map<String, String> map) {
        return map.get(l);
    }
}