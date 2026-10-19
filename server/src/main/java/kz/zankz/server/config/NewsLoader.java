package kz.zankz.server.config;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import kz.zankz.server.model.News;
import kz.zankz.server.repository.NewsRepository;

// Новости ленты (источник — Kazinform). Тексты — краткий пересказ на трёх языках, полная версия — по ссылке.
// При каждом запуске новость обновляется по ключу (n1…n5), а фото и видео берутся из папки
// src/main/resources/static/news: файлы n3-1.jpg, n3-2.jpg … n3-video.mp4 (ключ + дефис).
@Component
public class NewsLoader implements CommandLineRunner {

    private static final String KAZINFORM = "Kazinform";

    // первые тестовые новости — больше не нужны, удаляем
    private static final List<String> OLD_TITLES = List.of(
            "Изменена статья 23 Налогового кодекса",
            "Верховный Суд разъяснил вопросы наследования",
            "Новое постановление о защите чести и достоинства",
            "Купля-продажа цифровых финансовых активов",
            "Вступил в силу новый Налоговый кодекс",
            "Новое правило о договорах на строящееся жильё");

    record Seed(String slug, LocalDate date, String time, String url,
            String titleKk, String titleRu, String titleEn,
            String textKk, String textRu, String textEn) {
    }

    // порядок — от старых к новым
    static final List<Seed> NEWS = List.of(
            new Seed("n1", LocalDate.of(2026, 10, 14), "20:25",
                    "https://www.inform.kz/ru/prezident-kazahstana-podpisal-zakon-o-sotrudnichestve-s-katarom-v-borbe-s-prestu-79691b58",
                    "Мемлекет басшысы Катармен қылмысқа қарсы күрестегі ынтымақтастық туралы заңға қол қойды",
                    "Президент Казахстана подписал закон о сотрудничестве с Катаром в борьбе с преступностью",
                    "President of Kazakhstan signs law on cooperation with Qatar in fighting crime",
                    "Мемлекет басшысы Қазақстан мен Катар үкіметтері арасындағы қылмысқа қарсы күрестегі ынтымақтастық туралы келісімді ратификациялау туралы заңға қол қойды. Бұған дейін заңды Құрылтай екі оқылымда қабылдаған.\n"
                            + "Келісім елдерге ақпаратпен жедел алмасуға және бір-бірінің тәжірибесін үйренуге мүмкіндік береді. Сонымен қатар, егер сұрау ұлттық қауіпсіздікке, қоғамдық тәртіпке нұқсан келтірсе немесе ішкі заңнамаға қайшы келсе, әр тарап көмек көрсетуден бас тарта алады.",
                    "Глава государства подписал закон о ратификации соглашения между правительствами Казахстана и Катара о сотрудничестве в борьбе с преступностью. До этого закон в двух чтениях принял Курултай.\n"
                            + "Соглашение позволит странам оперативно обмениваться информацией и перенимать опыт друг друга. При этом каждая сторона может отказать в помощи, если запрос угрожает её национальной безопасности, общественному порядку или противоречит её законам.",
                    "The Head of State signed a law ratifying the agreement between the governments of Kazakhstan and Qatar on cooperation in combating crime. The Kurultai had earlier passed the law in two readings.\n"
                            + "The agreement will allow the two countries to exchange information quickly and learn from each other's experience. Each side may refuse assistance if a request could harm its national security or public order, or contradicts its domestic law."),
            new Seed("n2", LocalDate.of(2026, 10, 14), "20:30",
                    "https://www.inform.kz/ru/tokaev-podpisal-zakon-obelektronnom-obmene-dannimi-priroziske-lits-vsng-c422bcb2",
                    "Тоқаев ТМД-да адамдарды іздестіру кезінде деректермен электрондық алмасу туралы заңға қол қойды",
                    "Токаев подписал закон об электронном обмене данными при розыске лиц в СНГ",
                    "Tokayev signs law on electronic data exchange in the search for persons within the CIS",
                    "Президент ТМД мемлекеттерінің адамдарды мемлекетаралық іздестіру туралы 2010 жылғы 10 желтоқсандағы шартына өзгерістер енгізетін хаттаманы ратификациялау туралы заңға қол қойды. Құрылтай хаттаманы 30 қыркүйекте ратификациялаған.\n"
                            + "Өзгерістер ТМД елдерінің құзыретті органдарына ведомстволық электрондық пошта серверлері арқылы құжат алмасуға мүмкіндік береді. Бұл шетелде жасырынып жүрген адамдарды іздеуді жеделдетуі тиіс.",
                    "Президент подписал закон о ратификации протокола, который вносит изменения в договор государств СНГ о межгосударственном розыске лиц от 10 декабря 2010 года. Курултай ратифицировал протокол 30 сентября.\n"
                            + "Изменения позволяют компетентным органам стран СНГ обмениваться документами через ведомственные серверы электронной почты. Это должно ускорить поиск людей, которые скрываются за рубежом.",
                    "The President signed a law ratifying a protocol that amends the CIS treaty of 10 December 2010 on the interstate search for persons. The Kurultai ratified the protocol on 30 September.\n"
                            + "The changes allow the competent authorities of CIS countries to exchange documents via departmental e-mail servers, which should speed up the search for people hiding abroad."),
            new Seed("n5", LocalDate.of(2026, 10, 19), "13:26", null,
                    "Қасым-Жомарт Тоқаев қазақстандық құтқарушылардың кәсібилігін жоғары бағалады",
                    "Касым-Жомарт Токаев высоко оценил профессионализм казахстанских спасателей",
                    "Kassym-Jomart Tokayev praises the professionalism of Kazakh rescuers",
                    "Президент салтанатты рәсімге қатысушыларды Құтқарушылар күнімен құттықтап, төтенше жағдайлар саласының барлық қызметкерлеріне алғыс айтты.\n"
                            + "Ол құтқарушыларды бейбіт заманның батырлары деп атап, бұл кәсіп батылдықты, шешімділікті және жоғары біліктілікті талап ететінін атап өтті. Мемлекет басшысы ТЖ қызметкерлері өзгенің өмірін сақтау үшін күн сайын тәуекелге баратынын, олардың еңбегі ең жоғары құрметке лайық екенін айтты.",
                    "Президент поздравил участников торжественной церемонии с Днём спасателя и поблагодарил всех сотрудников сферы чрезвычайных ситуаций.\n"
                            + "Он назвал спасателей героями мирного времени и отметил, что эта профессия требует смелости, решительности и высокой квалификации. Глава государства подчеркнул, что сотрудники ЧС каждый день рискуют собой, чтобы спасти чужие жизни, и их труд достоин самого высокого уважения.",
                    "The President congratulated the participants of the ceremony on Rescuer's Day and thanked everyone working in emergency services.\n"
                            + "He called rescuers heroes of peacetime and said the profession demands courage, determination and high qualifications. He stressed that emergency workers risk their lives every day to save others, and their work deserves the highest respect."),
            new Seed("n4", LocalDate.of(2026, 10, 19), "13:44", null,
                    "Тоқаев: Ортақ қауіпсіздік әр адамның мінез-құлық мәдениетінен құралады",
                    "Общая безопасность складывается из культуры поведения каждого человека — Токаев",
                    "Tokayev: Common safety is built on the behaviour of every person",
                    "Құтқарушылар күніне арналған салтанатты рәсімде Президент мемлекет өз азаматтары қай жерде жүрсе де көмекке келетінін еске салды. Бұл қағида Конституциядан туындайды: онда адамның құқықтарын, өмірі мен қауіпсіздігін қорғау ең жоғары құндылық деп танылған.\n"
                            + "Мемлекет басшысының айтуынша, көптеген апаттар бейқамдық пен немқұрайлылықтан болады, сондықтан қауіпсіздік әр адамның жауапкершілігінен басталады. Ол жастар бойында заңға бағынуды, тәртіпті және қоршаған ортаға ұқыпты қарауды тәрбиелеуге шақырды.",
                    "Выступая на церемонии в честь Дня спасателя, Президент напомнил, что государство приходит на помощь своим гражданам, где бы они ни находились. Этот принцип вытекает из Конституции, где защита прав, жизни и безопасности человека названа высшей ценностью.\n"
                            + "По словам главы государства, многие аварии случаются из-за беспечности и халатности, поэтому безопасность начинается с ответственности каждого. Он призвал воспитывать у молодёжи законопослушность, дисциплину и бережное отношение к окружающей среде.",
                    "Speaking at a ceremony for Rescuer's Day, the President recalled that the state comes to the aid of its citizens wherever they are. This principle follows from the Constitution, which names the protection of human rights, life and safety as the highest value.\n"
                            + "He noted that many accidents are caused by carelessness and negligence, so safety starts with each person's responsibility, and called for teaching young people respect for the law, discipline and care for the environment."),
            new Seed("n3", LocalDate.of(2026, 10, 19), "14:12", null,
                    "Мемлекет басшысы құтқару қызметтерінің қызметкерлерін марапаттады",
                    "Глава государства наградил сотрудников служб спасения Казахстана",
                    "Head of State presents awards to rescue service workers",
                    "Қасым-Жомарт Тоқаев қызметтік борышын атқару кезінде көрсеткен ерлігі мен жанқиярлығы үшін құтқару қызметтерінің қызметкерлеріне мемлекеттік наградаларды табыс етті.\n"
                            + "Елдің түрлі өңірлерінен келген құтқарушылар ІІ дәрежелі «Даңқ», ІІ дәрежелі «Айбын», «Құрмет» ордендерімен, сондай-ақ «Ерлігі үшін» және «Ерен еңбегі үшін» медальдарымен марапатталды. Салтанатты рәсім 19 қазанда — Құтқарушылар күні өтті.",
                    "Касым-Жомарт Токаев вручил государственные награды работникам служб спасения за мужество и самоотверженность, проявленные при исполнении служебного долга.\n"
                            + "Спасатели из разных регионов страны получили ордена «Даңқ» II степени, «Айбын» II степени, «Құрмет», а также медали «Ерлігі үшін» и «Ерен еңбегі үшін». Церемония прошла 19 октября — в День спасателя.",
                    "Kassym-Jomart Tokayev presented state awards to rescue service workers for courage and dedication in the line of duty.\n"
                            + "Rescuers from various regions received the orders Dank (2nd class), Aibyn (2nd class) and Kurmet, as well as the medals Erligi Ushin and Eren Enbegi Ushin. The ceremony took place on 19 October, Rescuer's Day."));

    private static final List<String> IMAGE_EXT = List.of("jpg", "jpeg", "png", "webp");
    private static final List<String> VIDEO_EXT = List.of("mp4", "mov", "webm", "m4v");

    private final NewsRepository newsRepository;
    private final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

    public NewsLoader(NewsRepository newsRepository) {
        this.newsRepository = newsRepository;
    }

    @Override
    public void run(String... args) {
        newsRepository.findAll().stream()
                .filter(n -> n.getSlug() == null && OLD_TITLES.contains(n.getTitleRu()))
                .forEach(newsRepository::delete);

        int added = 0;
        for (Seed seed : NEWS) {
            News news = newsRepository.findFirstBySlug(seed.slug()).orElse(null);
            if (news == null) {
                news = new News();
                news.setSlug(seed.slug());
                news.setViews(0);
                added++;
            }
            news.setTitleKk(seed.titleKk());
            news.setTitleRu(seed.titleRu());
            news.setTitleEn(seed.titleEn());
            news.setTextKk(seed.textKk());
            news.setTextRu(seed.textRu());
            news.setTextEn(seed.textEn());
            news.setPublishedAt(seed.date());
            news.setPublishedTime(seed.time());
            news.setSourceUrl(seed.url());
            news.setSourceName(KAZINFORM);

            List<String> files = mediaFiles(seed.slug());
            news.setImages(files.stream().filter(f -> IMAGE_EXT.contains(ext(f)))
                    .map(f -> "/news/" + f).collect(Collectors.joining(",")));
            news.setVideoUrl(files.stream().filter(f -> VIDEO_EXT.contains(ext(f)))
                    .map(f -> "/news/" + f).findFirst().orElse(null));
            newsRepository.save(news);
        }
        System.out.println("Новости: в файле " + NEWS.size() + ", добавлено новых " + added);
    }

    // файлы вида n3-1.jpg, n3-2.jpg, n3-video.mp4 — по порядку номеров
    private List<String> mediaFiles(String slug) {
        try {
            Resource[] found = resolver.getResources("classpath:static/news/" + slug + "-*");
            return Arrays.stream(found)
                    .map(Resource::getFilename)
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparingInt(NewsLoader::number).thenComparing(Comparator.naturalOrder()))
                    .toList();
        } catch (IOException e) {
            return List.of();
        }
    }

    private static String ext(String file) {
        int dot = file.lastIndexOf('.');
        return dot < 0 ? "" : file.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    // «n3-12.jpg» -> 12, чтобы 10 шло после 9
    private static int number(String file) {
        String digits = file.replaceAll("^[^-]+-(\\d+).*$", "$1");
        return digits.matches("\\d+") ? Integer.parseInt(digits) : Integer.MAX_VALUE;
    }
}