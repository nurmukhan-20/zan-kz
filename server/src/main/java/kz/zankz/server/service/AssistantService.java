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
import kz.zankz.server.repository.ArticleRepository;

// ИИ-помощник: 1) находит в базе подходящие статьи, 2) отправляет их в Gemini,
// 3) Gemini отвечает ТОЛЬКО по этим статьям, 4) возвращаем ответ + ссылки на статьи.
@Service
public class AssistantService {

    private static final int MAX_ARTICLES = 6;      // сколько статей отправляем в Gemini
    private static final int MAX_TEXT = 2500;       // сколько символов текста одной статьи

    // частые слова, которые не помогают искать
    private static final Set<String> STOP = Set.of(
            "что", "как", "если", "это", "мне", "меня", "можно", "нужно", "есть", "или", "для", "при", "его", "она",
            "они", "мой", "моя", "мою", "мои", "все", "так", "уже", "быть", "будет", "какой", "какие", "какая", "где",
            "когда", "почему", "делать", "закон", "статья", "статьи",
            "және", "мен", "бар", "жоқ", "үшін", "қалай", "қандай", "неге", "керек", "болады", "бола", "менің", "маған",
            "деген", "заң", "бап", "туралы",
            "what", "how", "can", "the", "and", "for", "with", "that", "this", "does", "have", "about", "law", "article");

    private static final Pattern SOURCES = Pattern.compile("(?im)^\\s*\\[?SOURCES\\]?\\s*:?\\s*(.*)$");

    private final ArticleRepository articleRepository;
    private final GeminiClient gemini;

    public AssistantService(ArticleRepository articleRepository, GeminiClient gemini) {
        this.articleRepository = articleRepository;
        this.gemini = gemini;
    }

    @Transactional(readOnly = true)
    public AssistantResponse ask(String question, String lang) {
        String l = normalizeLang(lang);
        List<Article> found = findRelevant(question);

        // ничего не нашли — Gemini не вызываем (экономим бесплатный лимит)
        if (found.isEmpty()) {
            return new AssistantResponse(text(l, NOT_FOUND), List.of());
        }

        String raw = gemini.ask(instruction(l, found), question.trim());

        // Gemini в конце пишет строку «SOURCES: 12, 45» — id статей, которые он использовал
        Set<Long> used = new LinkedHashSet<>();
        Matcher m = SOURCES.matcher(raw);
        String answer = raw;
        if (m.find()) {
            for (String part : m.group(1).split("[^0-9]+")) {
                if (!part.isBlank()) used.add(Long.parseLong(part));
            }
            answer = raw.substring(0, m.start()).trim();
        }

        List<ArticleRef> refs = new ArrayList<>();
        for (Article a : found) {
            if (used.isEmpty() || used.contains(a.getId())) refs.add(ArticleRef.of(a));
        }
        return new AssistantResponse(answer + "\n\n" + text(l, DISCLAIMER), refs);
    }

    // ---------- 1. Поиск статей по словам вопроса ----------

    private List<Article> findRelevant(String question) {
        Set<String> stems = new HashSet<>();
        Set<String> numbers = new HashSet<>();
        for (String w : question.toLowerCase(Locale.ROOT).split("[^\\p{L}\\p{N}]+")) {
            if (w.isBlank()) continue;
            if (w.chars().allMatch(Character::isDigit)) { numbers.add(w); continue; }
            if (w.length() < 3 || STOP.contains(w)) continue;
            stems.add(stem(w));
        }
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

    private String instruction(String l, List<Article> articles) {
        String language = switch (l) {
            case "kk" -> "Kazakh (қазақ тілі)";
            case "en" -> "English";
            default -> "Russian";
        };
        StringBuilder sb = new StringBuilder();
        sb.append("""
                You are the legal assistant of the "Zan KZ" app about the laws of the Republic of Kazakhstan.
                Rules:
                1. Answer ONLY using the articles below. Do not use any other knowledge, do not invent laws or article numbers.
                2. If the articles do not answer the question, say so honestly and suggest contacting a lawyer.
                3. Answer in %s, in simple words, short (up to 8 sentences), you may use a short list.
                4. Mention the article number and the law, for example: "ст. 152, Трудовой кодекс".
                5. Do not ask the user for personal data (ИИН, address, phone).
                6. On the LAST line write: SOURCES: <ids of the articles you used, comma separated>
                Articles:
                """.formatted(language));
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

    private static final Map<String, String> NOT_FOUND = Map.of(
            "kk", "Кешіріңіз, біздің базадағы баптардан бұл сұраққа сәйкес ақпарат таппадым. Сұрақты басқаша жазып көріңіз немесе заңгерге хабарласыңыз.",
            "ru", "Извините, в статьях нашей базы я не нашёл ответа на этот вопрос. Попробуйте сформулировать иначе или обратитесь к юристу.",
            "en", "Sorry, I couldn't find an answer to this question in the articles of our database. Try rephrasing it or contact a lawyer.");

    private static final Map<String, String> DISCLAIMER = Map.of(
            "kk", "⚠️ Бұл ресми заң кеңесі емес. Маңызды мәселе бойынша заңгерге жүгініңіз.",
            "ru", "⚠️ Это не официальная юридическая консультация. По важным вопросам обратитесь к юристу.",
            "en", "⚠️ This is not official legal advice. For important matters, consult a lawyer.");

    private static String text(String l, Map<String, String> map) {
        return map.get(l);
    }
}