package kz.zankz.server.service;

import java.util.ArrayList;
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

import kz.zankz.server.dto.AssistantResponse;
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
        return scored.stream().limit(MAX_ARTICLES).map(Scored::article).toList();
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
        String language = switch (l) {
            case "kk" -> "Kazakh (қазақ тілі)";
            case "en" -> "English";
            default -> "Russian";
        };
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
        sb.append("\nGLOSSARY:\n");
        if (terms.isEmpty()) sb.append("(no matching terms)\n");
        for (Term t : terms) {
            sb.append("- ").append(pick(l, t.getTermKk(), t.getTermRu(), t.getTermEn())).append(": ")
              .append(cut(pick(l, t.getDefinitionKk(), t.getDefinitionRu(), t.getDefinitionEn()))).append('\n');
        }
        return sb.toString();
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