package kz.zankz.server.config;

import java.time.LocalDate;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import kz.zankz.server.model.Article;
import kz.zankz.server.model.Category;
import kz.zankz.server.model.Law;
import kz.zankz.server.repository.ArticleRepository;
import kz.zankz.server.repository.CategoryRepository;
import kz.zankz.server.repository.LawRepository;

// При первом запуске заполняет пустую базу начальными данными
@Component
public class DataLoader implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final LawRepository lawRepository;
    private final ArticleRepository articleRepository;

    public DataLoader(CategoryRepository categoryRepository, LawRepository lawRepository,
            ArticleRepository articleRepository) {
        this.categoryRepository = categoryRepository;
        this.lawRepository = lawRepository;
        this.articleRepository = articleRepository;
    }

    @Override
    public void run(String... args) {
        // если данные уже есть — ничего не делаем
        if (categoryRepository.count() > 0) {
            return;
        }

        Category constitution = category("Конституция", "Конституция", "Constitution", "book-outline");
        category("Қылмыстық құқық", "Уголовное право", "Criminal law", "shield-outline");
        category("Әкімшілік құқық", "Административное право", "Administrative law", "document-text-outline");
        category("Еңбек құқығы", "Трудовое право", "Labor law", "briefcase-outline");
        category("Отбасы құқығы", "Семейное право", "Family law", "people-outline");
        category("Салық құқығы", "Налоговое право", "Tax law", "cash-outline");
        category("Азаматтық құқық", "Гражданское право", "Civil law", "home-outline");
        category("Азаматтардың құқықтары", "Права граждан", "Citizens' rights", "person-outline");

        Law law = new Law();
        law.setTitleKk("Қазақстан Республикасының Конституциясы");
        law.setTitleRu("Конституция Республики Казахстан");
        law.setTitleEn("Constitution of the Republic of Kazakhstan");
        law.setCategory(constitution);
        law.setSourceUrl("https://adilet.zan.kz/rus/docs/K2600000000");
        law.setLastUpdated(LocalDate.of(2026, 10, 11)); // дата, когда мы сверяли текст с источником
        law = lawRepository.save(law);

        article(law, "1", "1-бап", "Статья 1",
                "Қазақстан Республикасы – демократиялық, зайырлы, құқықтық және әлеуметтік мемлекет. "
                        + "Мемлекеттің ең жоғары құндылығы – адам мен оның өмірі, құқықтары мен бостандықтары.",
                "Республика Казахстан – демократическое, светское, правовое и социальное государство. "
                        + "Высшие ценности государства – человек, его жизнь, права и свободы.",
                "Бұл бап мемлекеттің негізін сипаттайды: билік халықтан шығады, дін мемлекеттен бөлек, "
                        + "бәрі заңға бағынады, мемлекет адамдарға қамқорлық жасайды. Ең бастысы – адам және оның құқықтары.",
                "Эта статья описывает основу государства: власть исходит от народа, религия отделена от государства, "
                        + "все подчиняются закону, государство заботится о людях. Главное – человек и его права.");

        article(law, "17", "17-бап", "Статья 17",
                "1. Өмір сүру құқығы – әр адамның ажыратуға және айыруға болмайтын құқығы.\n"
                        + "2. Ешкімнің өз бетімен адам өмірін қиюға құқығы жоқ.\n"
                        + "3. Өлім жазасына тыйым салынған.",
                "1. Право на жизнь – неотъемлемое и неотчуждаемое право каждого человека.\n"
                        + "2. Никто не вправе произвольно лишать человека жизни.\n"
                        + "3. Смертная казнь запрещена.",
                "Әр адамның өмір сүруге құқығы бар, оны ешкім тартып ала алмайды. Қазақстанда өлім жазасы жоқ.",
                "У каждого человека есть право на жизнь, и его нельзя отнять. Смертной казни в Казахстане нет.");

        article(law, "27", "27-бап", "Статья 27",
                "1. Әр адамның еңбек етуге, қызмет пен кәсіп түрін еркін таңдауға құқығы бар. "
                        + "Еңбекке мәжбүрлеп тартуға қылмыстық немесе әкімшілік құқық бұзушылық жасағаны үшін "
                        + "адамды кінәлі деп тану туралы сот актісінің негізінде, "
                        + "не төтенше жағдайда, не соғыс жағдайында жол беріледі.\n"
                        + "2. Қазақстан Республикасы қауiпсiздiк пен гигиена талаптарына сай еңбек жағдайлары құқығына, "
                        + "еңбегi үшiн қандай да бiр кемсiтусiз сыйақы алу, сондай-ақ әлеуметтiк қорғалу құқығына "
                        + "заңға сәйкес кепілдік береді.\n"
                        + "3. Заңда белгіленген тәсiлдерді қолдана отырып, жеке және ұжымдық еңбек дауларын шешу "
                        + "құқығы танылады.\n"
                        + "4. Тынығу, жұмыс уақытының ұзақтығы, демалыс және мереке күндерi, ақы төленетін демалыс "
                        + "құқығына заңмен кепiлдiк беріледi.",
                "1. Каждый человек имеет право на труд, свободный выбор рода деятельности и профессии. "
                        + "Принудительный труд допускается на основании судебного акта о признании человека виновным "
                        + "в совершении уголовного или административного правонарушения "
                        + "либо в условиях чрезвычайного или военного положения.\n"
                        + "2. Республика Казахстан гарантирует право на условия труда, отвечающие требованиям "
                        + "безопасности и гигиены, на вознаграждение за труд без какой-либо дискриминации, "
                        + "а также на социальную защиту в соответствии с законом.\n"
                        + "3. Признается право на индивидуальные и коллективные трудовые споры "
                        + "с использованием установленных законом способов их разрешения.\n"
                        + "4. Право на отдых, продолжительность рабочего времени, выходные и праздничные дни, "
                        + "оплачиваемый отпуск гарантируется законом.",
                "Әр адам жұмысы мен мамандығын өзі таңдайды. Жұмыс қауіпсіз болуы керек, еңбекақы кемсітусіз төленуі тиіс. "
                        + "Демалыс күндері мен ақылы демалысқа құқық заңмен қорғалады.",
                "Каждый сам выбирает работу и профессию. Работа должна быть безопасной, а зарплата – без дискриминации. "
                        + "Право на выходные и оплачиваемый отпуск защищено законом.");
    }

    private Category category(String nameKk, String nameRu, String nameEn, String icon) {
        Category category = new Category();
        category.setNameKk(nameKk);
        category.setNameRu(nameRu);
        category.setNameEn(nameEn);
        category.setIcon(icon);
        return categoryRepository.save(category);
    }

    private void article(Law law, String number, String titleKk, String titleRu,
            String textKk, String textRu, String explanationKk, String explanationRu) {
        Article article = new Article();
        article.setLaw(law);
        article.setArticleNumber(number);
        article.setTitleKk(titleKk);
        article.setTitleRu(titleRu);
        article.setTextKk(textKk);
        article.setTextRu(textRu);
        article.setExplanationKk(explanationKk);
        article.setExplanationRu(explanationRu);
        articleRepository.save(article);
    }
}