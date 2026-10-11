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

        // Раздел I. Основы конституционного строя
        // Порядок у каждой статьи: текст (каз, рус, англ), затем объяснение (каз, рус, англ)

        article(law, "1",
                "Қазақстан Республикасы – демократиялық, зайырлы, құқықтық және әлеуметтік мемлекет. "
                        + "Мемлекеттің ең жоғары құндылығы – адам мен оның өмірі, құқықтары мен бостандықтары.",
                "Республика Казахстан – демократическое, светское, правовое и социальное государство. "
                        + "Высшие ценности государства – человек, его жизнь, права и свободы.",
                "The Republic of Kazakhstan is a democratic, secular, law-bound and social State. "
                        + "The supreme value of the State shall be man, his life, rights and freedoms.",
                "Бұл бап мемлекеттің негізін сипаттайды: билік халықтан шығады, дін мемлекеттен бөлек, "
                        + "бәрі заңға бағынады, мемлекет адамдарға қамқорлық жасайды. Ең бастысы – адам және оның құқықтары.",
                "Эта статья описывает основу государства: власть исходит от народа, религия отделена от государства, "
                        + "все подчиняются закону, государство заботится о людях. Главное – человек и его права.",
                "This article describes the foundation of the state: power comes from the people, religion is separate "
                        + "from the state, everyone obeys the law, and the state takes care of people. "
                        + "What matters most is the person and their rights.");

        article(law, "2",
                "1. Қазақстан Республикасы – унитарлы мемлекет. Басқару үлгісі – президенттік республика.\n"
                        + "2. Қазақстан Республикасының Егемендігі оның бүкіл аумағын қамтиды. Мемлекет өз аумағының "
                        + "тұтастығын, қол сұғылмауын, бөлінбеуін қамтамасыз етеді.\n"
                        + "3. Қазақстан Республикасының әкімшілік-аумақтық құрылысы конституциялық заңда айқындалады.\n"
                        + "4. Қазақстан Республикасының елордасы – Астана қаласы. Елорда мәртебесі конституциялық заңда айқындалады.\n"
                        + "5. Қазақстан Республикасы мен Қазақстан атауларының мәні бірдей.\n"
                        + "6. Қазақстан Республикасының ұлттық валютасы (ақша бірлігі) – теңге.\n"
                        + "Теңгені эмиссиялау айрықша құқығы Қазақстан Республикасына тиесілі.\n"
                        + "7. Қазақстан Республикасының Егемендігі, Тәуелсіздігі, унитарлығы, аумақтық тұтастығы мен "
                        + "басқару үлгісі өзгермейді.",
                "1. Республика Казахстан – унитарное государство. Форма правления – президентская республика.\n"
                        + "2. Суверенитет Республики Казахстан распространяется на всю ее территорию. Государство обеспечивает "
                        + "целостность, неприкосновенность, неотчуждаемость своей территории.\n"
                        + "3. Административно-территориальное устройство Республики Казахстан определяется конституционным законом.\n"
                        + "4. Столица Республики Казахстан – город Астана. Статус столицы определяется конституционным законом.\n"
                        + "5. Наименования Республика Казахстан и Казахстан равнозначны.\n"
                        + "6. Национальная валюта (денежная единица) Республики Казахстан – теңге.\n"
                        + "Республике Казахстан принадлежит исключительное право эмиссии теңге.\n"
                        + "7. Суверенитет, Независимость, унитарность, территориальная целостность, форма правления "
                        + "Республики Казахстан неизменны.",
                "1. The Republic of Kazakhstan shall be a unitary State. The form of government shall be a presidential republic.\n"
                        + "2. The sovereignty of the Republic of Kazakhstan extends over its entire territory.\n"
                        + "The State shall ensure the integrity, inviolability and inalienability of its territory.\n"
                        + "3. The administrative-territorial structure of the Republic of Kazakhstan shall be determined by "
                        + "constitutional law.\n"
                        + "4. The capital of the Republic of Kazakhstan shall be Astana. The status of the capital shall be "
                        + "determined by constitutional law.\n"
                        + "5. The names “The Republic of Kazakhstan” and “Kazakhstan” shall be equivalent.\n"
                        + "6. The national currency (monetary unit) of the Republic of Kazakhstan shall be the tenge (KZT).\n"
                        + "The Republic of Kazakhstan holds the exclusive right to issue the tenge.\n"
                        + "7. The Sovereignty, Independence, unitarity, territorial integrity, the form of government of the "
                        + "Republic of Kazakhstan shall be immutable.",
                "Қазақстан – біртұтас мемлекет, оны Президент басқарады. Елорда – Астана, валюта – теңге. "
                        + "Тәуелсіздік, аумақтың тұтастығы және басқару үлгісі өзгермейді.",
                "Казахстан – единое государство, которое возглавляет Президент. Столица – Астана, валюта – теңге. "
                        + "Независимость, целостность территории и форма правления не могут быть изменены.",
                "Kazakhstan is a single state headed by the President. The capital is Astana and the currency is the tenge. "
                        + "Independence, territorial integrity and the form of government cannot be changed.");

        article(law, "3",
                "1. Қазақстан Республикасы қызметінің негіз құраушы қағидаттары: Егемендік пен Тәуелсіздікті қорғау; "
                        + "адамның және азаматтың құқықтары мен бостандықтарын сақтау; заң мен тәртіп үстемдігін қамтамасыз ету; "
                        + "жалпыұлттық бірлікті бекемдеу; халықтың әл-ауқатын арттыру; жауапты әрі жасампаз отаншылдық идеясын "
                        + "ілгерілету; қоғамдық диалогты дамыту; еңбексүйгіштік, прогресс, білім құндылықтарын бекіту; "
                        + "жоғары экологиялық мәдениетті қалыптастыру; тарихи-мәдени мұраны сақтау; төл мәдениетті қолдау.\n"
                        + "2. Қазақстан Республикасы адам капиталын, білімді, ғылымды, инновацияны дамытуды мемлекет қызметінің "
                        + "стратегиялық бағыты деп таниды.",
                "1. Основополагающие принципы деятельности Республики Казахстан: защита Суверенитета и Независимости; "
                        + "соблюдение прав и свобод человека и гражданина; обеспечение верховенства закона и порядка; "
                        + "укрепление общенационального единства; повышение благосостояния народа; продвижение идеи "
                        + "ответственного, созидательного патриотизма; развитие общественного диалога; утверждение ценностей "
                        + "трудолюбия, прогресса, знаний; формирование высокой экологической культуры; сохранение "
                        + "историко-культурного наследия; поддержка национальной культуры.\n"
                        + "2. Республика Казахстан признает развитие человеческого капитала, образования, науки, инноваций "
                        + "в качестве стратегического направления деятельности государства.",
                "1. The fundamental principles of the Republic of Kazakhstan shall be: protecting the Sovereignty and "
                        + "Independence; respecting human and civil rights and freedoms; ensuring the rule of law and order; "
                        + "strengthening the national unity; improving the well-being of the people; promoting the idea of "
                        + "responsible, constructive patriotism; fostering public dialogue; upholding the values of diligence, "
                        + "progress, knowledge; fostering a high level of environmental culture; preserving historical and "
                        + "cultural heritage; supporting national culture.\n"
                        + "2. The Republic of Kazakhstan recognizes the development of human capital, education, science, and "
                        + "innovation as a strategic direction of state activity.",
                "Мұнда мемлекет жұмысының басты қағидаттары аталған: тәуелсіздікті қорғау, адам құқықтарын сақтау, "
                        + "заң үстемдігі, халық бірлігі. Білім, ғылым және инновация стратегиялық бағыт деп танылған.",
                "Здесь перечислены главные принципы работы государства: защита независимости, соблюдение прав человека, "
                        + "верховенство закона, единство народа. Образование, наука и инновации названы стратегическим направлением.",
                "This article lists the main principles the state works by: protecting independence, respecting human rights, "
                        + "the rule of law and national unity. Education, science and innovation are named a strategic direction.");

        article(law, "4",
                "1. Қазақстан халқы – мемлекеттік биліктің бірден-бір бастауы және Егемендік иесі.\n"
                        + "2. Халық билікті жалпыхалықтық референдум және еркін сайлау арқылы тікелей жүргізеді, сондай-ақ "
                        + "өз билігін мемлекеттік органдар арқылы жүзеге асырады.\n"
                        + "3. Қазақстан Республикасында билікті ешкім де иемденіп кете алмайды. Билікті иемденіп кету заңмен "
                        + "қудаланады. Халық пен мемлекет атынан әрекет ету құқығы Қазақстан Республикасының Президентіне, "
                        + "сондай-ақ конституциялық өкілеттігі шегінде Қазақстан Республикасының Құрылтайына тиесілі. "
                        + "Қазақстан Республикасының Үкіметі мен өзге де мемлекеттік органдар өздеріне берілген өкілеттік "
                        + "шегінде мемлекет атынан әрекет етеді.\n"
                        + "4. Қазақстан Республикасында мемлекеттік билік біртұтас, ол заң шығарушы, атқарушы, сот тармақтарына "
                        + "бөліну және өзара іс-қимыл жасау қағидатына сәйкес Конституция мен заңдар негізінде жүзеге асырылады.",
                "1. Народ Казахстана – единственный источник государственной власти и носитель Суверенитета.\n"
                        + "2. Народ осуществляет власть непосредственно через всенародный референдум и свободные выборы, "
                        + "а также делегирует осуществление своей власти государственным органам.\n"
                        + "3. Никто не может присваивать власть в Республике Казахстан. Присвоение власти преследуется по закону. "
                        + "Право выступать от имени народа и государства принадлежит Президенту Республики Казахстан, а также "
                        + "Курултаю Республики Казахстан в пределах его конституционных полномочий. Правительство Республики "
                        + "Казахстан и иные государственные органы выступают от имени государства в пределах делегированных "
                        + "им полномочий.\n"
                        + "4. Государственная власть в Республике Казахстан едина и осуществляется на основе Конституции и законов "
                        + "в соответствии с принципом ее разделения на законодательную, исполнительную, судебную ветви "
                        + "и их взаимодействия.",
                "1. The people of Kazakhstan shall be the sole source of state power and the bearer of Sovereignty.\n"
                        + "2. The people shall exercise power directly through national referenda and free elections, and also "
                        + "delegate the exercise of their power to state bodies.\n"
                        + "3. No one may usurp power in the Republic of Kazakhstan. The usurpation of power shall be prosecuted "
                        + "by law. The right to act on behalf of the people and the State shall belong to the President of the "
                        + "Republic of Kazakhstan, and also to the Kurultai of the Republic of Kazakhstan within its "
                        + "constitutional authority. The Government of the Republic of Kazakhstan and other state bodies shall "
                        + "act on behalf of the State within the powers delegated to them.\n"
                        + "4. State power in the Republic of Kazakhstan shall be unified and exercised on the basis of the "
                        + "Constitution and laws in accordance with the principle of its division into the legislative, "
                        + "executive and judicial branches and their interaction.",
                "Елдегі билік халыққа тиесілі. Халық оны референдум мен сайлау арқылы жүргізеді. "
                        + "Билікті басып алуға болмайды – бұл заңмен қудаланады. Билік үш тармаққа бөлінеді: "
                        + "заң шығарушы, атқарушы және сот.",
                "Власть в стране принадлежит народу. Народ пользуется ею через референдум и выборы. "
                        + "Захватить власть нельзя – это преследуется по закону. Власть делится на три ветви: "
                        + "законодательную, исполнительную и судебную.",
                "Power in the country belongs to the people, who use it through referendums and elections. "
                        + "Seizing power is not allowed and is prosecuted by law. Power is divided into three branches: "
                        + "legislative, executive and judicial.");

        article(law, "5",
                "1. Қазақстан Республикасының қолданыстағы құқығы – Конституцияның, оған сәйкес келетін заңдардың, "
                        + "Қазақстан Республикасы Конституциялық Соты мен Қазақстан Республикасы Жоғарғы Сотының нормативтік "
                        + "қаулыларының, өзге де нормативтік құқықтық актілердің, Қазақстан Республикасының халықаралық шарт "
                        + "міндеттемелерінің нормалары.\n"
                        + "2. Конституцияның ең жоғары заңды күші бар және ол Қазақстан Республикасының бүкіл аумағында "
                        + "тікелей қолданылады.\n"
                        + "3. Қазақстан Республикасының аумағында халықаралық шарттарды қолдану тәртібі заңдарда айқындалады.\n"
                        + "4. Барлық заңдар мен Қазақстан Республикасы ратификациялаған халықаралық шарттар жарияланады. "
                        + "Азаматтардың құқықтарына, бостандықтары мен міндеттеріне қатысты нормативтік құқықтық актілердің "
                        + "ресми жариялануы – оларды қолданудың міндетті шарты.\n"
                        + "5. Азаматтарға жаңа міндеттер жүктейтін немесе олардың жағдайын нашарлататын заңдардың кері күші болмайды.\n"
                        + "Жауаптылықты белгілейтін немесе күшейтетін заңдардың кері күші болмайды.\n"
                        + "Жаңа заң бұрын жасалған құқық бұзушылық үшін жауаптылықты жоятын немесе жеңілдететін болса, "
                        + "онда жаңа заң қолданылады.\n"
                        + "6. Қазақстан Республикасының жекелеген өңірлерінің экономикасын қарқынды дамыту мақсатында қаржы "
                        + "саласына қатысты арнаулы құқықтық режим немесе \"қарқынды дамитын қала\" арнаулы құқықтық режимі "
                        + "конституциялық заңдарға сәйкес белгіленуі мүмкін. Осы арнаулы құқықтық режимдер мемлекеттік басқару, "
                        + "сот жүйесінің жұмыс істеу ерекшеліктерін көздеуі мүмкін.",
                "1. Действующее право Республики Казахстан – нормы Конституции, соответствующих ей законов, нормативных "
                        + "постановлений Конституционного Суда Республики Казахстан и Верховного Суда Республики Казахстан, "
                        + "иных нормативных правовых актов, международных договорных обязательств Республики Казахстан.\n"
                        + "2. Конституция имеет высшую юридическую силу и прямое действие на всей территории Республики Казахстан.\n"
                        + "3. Порядок действия на территории Республики Казахстан международных договоров определяется законами.\n"
                        + "4. Все законы и ратифицированные Республикой Казахстан международные договоры публикуются. "
                        + "Официальное опубликование нормативных правовых актов, касающихся прав, свобод и обязанностей граждан, – "
                        + "обязательное условие их применения.\n"
                        + "5. Законы, возлагающие новые обязанности на граждан или ухудшающие их положение, обратной силы не имеют.\n"
                        + "Законы, устанавливающие или усиливающие ответственность, обратной силы не имеют.\n"
                        + "В случае отмены или смягчения новым законом ответственности за ранее совершенное правонарушение "
                        + "применяется новый закон.\n"
                        + "6. В целях ускоренного экономического развития отдельных регионов Республики Казахстан в соответствии "
                        + "с конституционными законами может быть установлен специальный правовой режим в финансовой сфере или "
                        + "специальный правовой режим \"города ускоренного развития\". Указанные специальные правовые режимы "
                        + "могут предусматривать особенности государственного управления, функционирования судебной системы.",
                "1. The current law of the Republic of Kazakhstan shall consist of the provisions of the Constitution, "
                        + "corresponding laws, regulatory resolutions of the Constitutional Court of the Republic of Kazakhstan "
                        + "and the Supreme Court of the Republic of Kazakhstan, other regulatory legal acts and the international "
                        + "treaty obligations of the Republic of Kazakhstan.\n"
                        + "2. The Constitution shall have supreme legal force and direct effect throughout the territory of the "
                        + "Republic of Kazakhstan.\n"
                        + "3. The application procedure of international treaties within the territory of the Republic of Kazakhstan.\n"
                        + "4. All laws and international treaties ratified by the Republic of Kazakhstan shall be published. "
                        + "Official publication of regulatory legal acts concerning the rights, freedoms and obligations of the "
                        + "citizens shall be a mandatory condition for their application.\n"
                        + "5. Laws imposing new obligations on citizens or worsening their status shall not have retroactive effect.\n"
                        + "Laws establishing or increasing liability shall not have retroactive effect.\n"
                        + "If a new law abolishes or mitigates liability for a previously committed offense, the new law shall apply.\n"
                        + "6. For the purpose of accelerating the economic development of certain regions of the Republic of "
                        + "Kazakhstan, a special legal regime in the financial sector or a special legal regime for “a city of "
                        + "accelerated development” may be established in accordance with constitutional laws. The said special "
                        + "legal regimes may provide for specific aspects of public administration and functioning of the "
                        + "judicial system.",
                "Конституция – елдің басты заңы, басқа заңдардың бәрі оған сәйкес болуға тиіс. "
                        + "Азаматтардың құқықтары мен міндеттеріне қатысты заң ресми жарияланғаннан кейін ғана қолданылады. "
                        + "Адамның жағдайын нашарлататын жаңа заң өткен оқиғаларға қолданылмайды, "
                        + "ал жауаптылықты жеңілдететін заң қолданылады.",
                "Конституция – главный закон страны, все остальные законы должны ей соответствовать. "
                        + "Закон, который касается прав и обязанностей граждан, применяется только после официального опубликования. "
                        + "Новый закон, который ухудшает положение человека, не действует на прошлые события, "
                        + "а закон, который смягчает ответственность, – действует.",
                "The Constitution is the country's main law, and all other laws must comply with it. "
                        + "A law that concerns citizens' rights and duties applies only after it is officially published. "
                        + "A new law that makes a person's position worse does not apply to past events, "
                        + "while a law that reduces liability does.");

        article(law, "6",
                "1. Қазақстан Республикасында идеологиялық және саяси әралуандық танылады.\n"
                        + "2. Қоғамдық бірлестіктер заң алдында тең. Мемлекеттің қоғамдық бірлестік ісіне, ал қоғамдық "
                        + "бірлестіктің мемлекет ісіне заңсыз араласуына және қоғамдық бірлестікке мемлекеттік орган "
                        + "функцияларын жүктеуге жол берілмейді. Мемлекеттік органда саяси партия ұйымын құруға жол берілмейді.\n"
                        + "3. Мақсаты немесе әрекеті конституциялық құрылыс негіздерін күштеп өзгертуге, Қазақстан "
                        + "Республикасының тұтастығын және қоғамдық тәртіпті бұзуға, ұлттық қауіпсіздікке нұқсан келтіруге, "
                        + "соғысты, әлеуметтік, нәсілдік, ұлттық, діни араздықты қоздыруға бағытталған қоғамдық бірлестік "
                        + "құруға және оның қызметіне, сондай-ақ заңнамада көзделмеген әскерилендірілген жасақ құруға "
                        + "тыйым салынады.\n"
                        + "4. Басқа мемлекеттің саяси партиясы мен кәсіптік одағының, діни негіздегі саяси партияның қызметіне "
                        + "жол берілмейді, сондай-ақ саяси партия мен кәсіптік одақты шетелдік заңды тұлғаның, шетел азаматы "
                        + "мен азаматтығы жоқ адамның, шетелдік қатысуы бар заңды тұлғаның, шет мемлекет пен халықаралық "
                        + "ұйымның қаржыландыруына жол берілмейді.\n"
                        + "5. Коммерциялық емес ұйымның шет мемлекеттен, халықаралық және шетелдік заңды тұлғадан, шетел "
                        + "азаматы мен азаматтығы жоқ адамнан алатын ақша қаражатының қозғалысы және активтері туралы ақпарат "
                        + "Қазақстан Республикасының заңдарына сәйкес ашық әрі қолжетімді болуға тиіс.",
                "1. В Республике Казахстан признается идеологическое и политическое многообразие.\n"
                        + "2. Общественные объединения равны перед законом. Не допускаются незаконное вмешательство государства "
                        + "в дела общественных объединений и общественных объединений в дела государства, а также возложение "
                        + "на общественные объединения функций государственных органов. Не допускается создание в государственных "
                        + "органах организаций политических партий.\n"
                        + "3. Создание и деятельность общественных объединений, цели или действия которых направлены на "
                        + "насильственное изменение основ конституционного строя, нарушение целостности Республики Казахстан, "
                        + "общественного порядка, подрыв национальной безопасности, разжигание войны, социальной, расовой, "
                        + "национальной, религиозной розни, а также создание не предусмотренных законодательством военизированных "
                        + "формирований запрещены.\n"
                        + "4. Не допускаются деятельность политических партий и профессиональных союзов других государств, "
                        + "политических партий на религиозной основе, а также финансирование политических партий и "
                        + "профессиональных союзов иностранными юридическими лицами, иностранными гражданами и лицами без "
                        + "гражданства, юридическими лицами с иностранным участием, иностранными государствами и международными "
                        + "организациями.\n"
                        + "5. Информация о движении денежных средств и активах некоммерческих организаций, получаемых от "
                        + "иностранных государств, международных и иностранных юридических лиц, иностранных граждан и лиц без "
                        + "гражданства, должна быть открытой и доступной в соответствии с законами Республики Казахстан.",
                "1. Ideological and political diversity shall be recognized in the Republic of Kazakhstan.\n"
                        + "2. Public associations shall be equal before the law. Unlawful state interference in the affairs of "
                        + "public associations, public associations in state affairs, also assignment of state functions to "
                        + "public associations shall be prohibited. Establishing political party organizations within state "
                        + "bodies shall not be permitted.\n"
                        + "3. Creation and activities of public associations pursuing the goals or actions directed at violent "
                        + "change of the foundations of the constitutional order, violation of the integrity of the Republic of "
                        + "Kazakhstan or public order, undermining of national security, incitement of war, social, racial, "
                        + "national, or religious discord, or creation of paramilitary formations not provided for by law, "
                        + "shall be prohibited.\n"
                        + "4. The activities of political parties and trade unions of other states, political parties based on "
                        + "religion and financing of political parties and trade unions by foreign legal entities, foreign "
                        + "nationals and stateless persons, legal entities with foreign participation, foreign states and "
                        + "international organizations shall not be permitted.\n"
                        + "5. Information on the movement of funds and assets of non-profit organizations received from foreign "
                        + "states, international and foreign legal entities, foreign nationals and stateless persons must be "
                        + "open and accessible in accordance with the laws of the Republic of Kazakhstan.",
                "Елде әртүрлі саяси көзқарасқа жол беріледі, қоғамдық бірлестіктер заң алдында тең. "
                        + "Күш қолдануға, араздық қоздыруға немесе ел тұтастығын бұзуға бағытталған бірлестіктерге тыйым салынған. "
                        + "Партиялар мен кәсіподақтарды шетелден қаржыландыруға болмайды.",
                "В стране допускаются разные политические взгляды, а общественные объединения равны перед законом. "
                        + "Запрещены объединения, которые направлены на насилие, разжигание розни или нарушение целостности страны. "
                        + "Партии и профсоюзы нельзя финансировать из-за рубежа.",
                "Different political views are allowed in the country, and public associations are equal before the law. "
                        + "Associations aimed at violence, inciting discord or breaking up the country are banned. "
                        + "Political parties and trade unions may not be financed from abroad.");

        article(law, "7",
                "1. Дін мемлекеттен бөлек.\n"
                        + "2. Қазақстан аумағында діни ұйымдардың қызметі заңға сәйкес жүзеге асырылады және ол конституциялық "
                        + "құрылыс негіздерін, адамның және азаматтың құқықтары мен бостандықтарын қорғау, ұлттық қауіпсіздікті "
                        + "қамтамасыз ету, қоғамдық тәртіпті, азаматтардың денсаулығы мен қоғамның адамгершілік болмысын сақтау "
                        + "мақсатында шектелуі мүмкін.",
                "1. Религия отделена от государства.\n"
                        + "2. Деятельность религиозных организаций на территории Казахстана осуществляется в соответствии с законом "
                        + "и может быть ограничена в целях защиты основ конституционного строя, прав и свобод человека и гражданина, "
                        + "обеспечения национальной безопасности, охраны общественного порядка, здоровья граждан и нравственности "
                        + "общества.",
                "1. Religion shall be separated from the State.\n"
                        + "2. The activities of religious organizations in Kazakhstan shall be carried out in accordance with the "
                        + "law and may be restricted to protect the foundations of the constitutional order, human and civil "
                        + "rights and freedoms, ensure national security, safeguard public order, public health and public morality.",
                "Дін мен мемлекет бір-бірінен бөлек. Діни ұйымдар заң бойынша жұмыс істейді, олардың қызметі "
                        + "қауіпсіздік, тәртіп, денсаулық және адам құқықтары үшін шектелуі мүмкін.",
                "Религия и государство существуют отдельно друг от друга. Религиозные организации работают по закону, "
                        + "и их деятельность может быть ограничена ради безопасности, порядка, здоровья и прав людей.",
                "Religion and the state are separate from each other. Religious organizations work according to the law, "
                        + "and their activities may be restricted for the sake of security, public order, health and people's rights.");

        article(law, "8",
                "1. Қазақстан Республикасында барлық меншік түрі танылады, оларға кепілдік беріледі және тең қорғалады.\n"
                        + "2. Меншікті пайдалану қоғам мен мемлекет мүддесіне сай келуге, қоршаған ортаны қорғау саласындағы "
                        + "белгіленген талаптар сақталып іске асырылуға тиіс, басқа тұлғалардың заңмен қорғалатын құқықтары мен "
                        + "мүдделеріне нұқсан келтірмеуге тиіс. Меншік субъектілері мен объектілері, меншік иелерінің "
                        + "құқықтарының көлемі мен шегі, оларды қорғау кепілдіктері заңда айқындалады.\n"
                        + "3. Жер жəне оның қойнауы, су көздері, өсімдіктер мен жануарлар дүниесі, басқа да табиғи ресурстар "
                        + "халыққа тиесілі. Халық атынан меншік құқығын мемлекет жүзеге асырады. Жер заңда белгіленген "
                        + "негіздерде, шарттар мен шектерде жеке меншікте де болуы мүмкін.",
                "1. В Республике Казахстан собственность всех форм признается, гарантируется и равным образом защищается.\n"
                        + "2. Пользование собственностью должно отвечать интересам общества и государства, осуществляться с "
                        + "соблюдением установленных требований в области охраны окружающей среды и не должно ущемлять защищаемые "
                        + "законом права и интересы других лиц. Субъекты и объекты собственности, объем и пределы осуществления "
                        + "собственниками своих прав, гарантии их защиты определяются законом.\n"
                        + "3. Земля и ее недра, воды, растительный и животный мир, другие природные ресурсы принадлежат народу. "
                        + "От имени народа право собственности осуществляет государство. Земля может находиться также в частной "
                        + "собственности на основаниях, условиях и в пределах, установленных законом.",
                "1. Property of all forms in the Republic of Kazakhstan shall be recognized, guaranteed and equally protected.\n"
                        + "2. The use of property shall serve the interests of the society and the State, shall be exercised in "
                        + "compliance with established environmental protection requirements and shall not infringe on the "
                        + "legally protected rights and interests of other persons protected by law. The subjects and objects of "
                        + "property, the scope and limits of the exercise of rights by owners, guarantees for their protection "
                        + "shall be determined by law.\n"
                        + "3. Land and its subsoil resources, waters, flora and fauna, other natural resources shall belong to "
                        + "the people. The State shall exercise ownership rights on behalf of the people. Land may also be "
                        + "privately owned on the grounds, conditions and within the frames established by law.",
                "Кез келген меншік бірдей қорғалады. Оны табиғатқа зиян келтірмей және басқа адамдардың құқықтарын "
                        + "бұзбай пайдалану керек. Табиғи ресурстар халыққа тиесілі, ал жер заңда белгіленген шекте "
                        + "жеке меншікте де бола алады.",
                "Любая собственность защищается одинаково. Пользоваться ею нужно так, чтобы не вредить природе "
                        + "и не нарушать права других людей. Природные ресурсы принадлежат народу, "
                        + "а земля может быть и в частной собственности – в пределах, которые установил закон.",
                "All property is protected equally. It must be used without harming nature or violating other people's rights. "
                        + "Natural resources belong to the people, and land may also be privately owned "
                        + "within the limits set by law.");

        article(law, "9",
                "1. Қазақстан Республикасының мемлекеттік тілі – қазақ тілі.\n"
                        + "2. Мемлекеттік ұйымдарда және жергілікті өзін-өзі басқару органдарында қазақ тілімен қатар "
                        + "орыс тілі ресми түрде қолданылады.\n"
                        + "3. Мемлекет біртұтас Қазақстан халқының тілдерін үйрену және дамыту үшін жағдай жасауға "
                        + "қамқорлық көрсетеді.",
                "1. Государственный язык Республики Казахстан – казахский язык.\n"
                        + "2. В государственных организациях и органах местного самоуправления наряду с казахским официально "
                        + "употребляется русский язык.\n"
                        + "3. Государство заботится о создании условий для изучения и развития языков единого народа Казахстана.",
                "1. The state language of the Republic of Kazakhstan shall be Kazakh.\n"
                        + "2. The Russian language shall be officially used along with the Kazakh language in state organizations "
                        + "and local government bodies.\n"
                        + "3. The State is committed to creating conditions for the study and development of the languages of "
                        + "the united people of Kazakhstan.",
                "Мемлекеттік тіл – қазақ тілі. Мемлекеттік ұйымдарда онымен қатар орыс тілі ресми түрде қолданылады. "
                        + "Мемлекет Қазақстан халқының тілдерін үйренуге және дамытуға жағдай жасайды.",
                "Государственный язык – казахский. В государственных организациях наряду с ним официально "
                        + "употребляется русский. Государство создаёт условия для изучения и развития языков народа Казахстана.",
                "The state language is Kazakh. Russian is officially used alongside it in state organizations. "
                        + "The state creates conditions for learning and developing the languages of the people of Kazakhstan.");

        article(law, "10",
                "Қазақстан Республикасы халықаралық құқық қағидаттары мен нормаларын құрметтейді, мүдделі мемлекеттермен "
                        + "бейбітшілік пен ынтымақтастықты, олардың ішкі ісіне араласпауды, халықаралық дауды бейбіт жолмен "
                        + "шешуді көздейтін сыртқы саясат жүргізеді.",
                "Республика Казахстан уважает принципы и нормы международного права, проводит внешнюю политику мира и "
                        + "сотрудничества с заинтересованными государствами, невмешательства в их внутренние дела, мирного "
                        + "разрешения международных споров.",
                "The Republic of Kazakhstan shall respect the principles and norms of the international law and pursue the "
                        + "foreign policy of peace and cooperation with interested states, non-interference in their internal "
                        + "affairs, peaceful settlement of international disputes.",
                "Қазақстан халықаралық құқықты сақтайды, бейбіт сыртқы саясат жүргізеді, "
                        + "басқа елдердің ісіне араласпайды және дауларды бейбіт жолмен шешеді.",
                "Казахстан соблюдает международное право, ведёт мирную внешнюю политику, "
                        + "не вмешивается в дела других стран и решает споры мирным путём.",
                "Kazakhstan follows international law, pursues a peaceful foreign policy, "
                        + "does not interfere in other countries' affairs and settles disputes peacefully.");

        article(law, "11",
                "Қазақстан Республикасының мемлекеттік рәміздері – Мемлекеттік Ту, Мемлекеттік Елтаңба, "
                        + "Мемлекеттік Әнұран. Олардың сипаттамасы және пайдалану тәртібі конституциялық заңда белгіленеді.",
                "Республика Казахстан имеет государственные символы – Государственный Флаг, Государственный Герб, "
                        + "Государственный Гимн. Их описание и порядок использования устанавливаются конституционным законом.",
                "The Republic of Kazakhstan shall have state symbols: the State Flag, the State Emblem and the State Anthem. "
                        + "Their description and the procedure for their use shall be established by constitutional law.",
                "Қазақстанның үш мемлекеттік рәмізі бар: Ту, Елтаңба және Әнұран. "
                        + "Олардың сипаттамасы мен пайдалану тәртібі жеке конституциялық заңда жазылған.",
                "У Казахстана три государственных символа: Флаг, Герб и Гимн. "
                        + "Как они выглядят и как их использовать, описано в отдельном конституционном законе.",
                "Kazakhstan has three state symbols: the Flag, the Emblem and the Anthem. "
                        + "What they look like and how they are used is set out in a separate constitutional law.");
    }

    private Category category(String nameKk, String nameRu, String nameEn, String icon) {
        Category category = new Category();
        category.setNameKk(nameKk);
        category.setNameRu(nameRu);
        category.setNameEn(nameEn);
        category.setIcon(icon);
        return categoryRepository.save(category);
    }

    private void article(Law law, String number, String textKk, String textRu, String textEn,
            String explanationKk, String explanationRu, String explanationEn) {
        Article article = new Article();
        article.setLaw(law);
        article.setArticleNumber(number);
        article.setTitleKk(number + "-бап");
        article.setTitleRu("Статья " + number);
        article.setTitleEn("Article " + number);
        article.setTextKk(textKk);
        article.setTextRu(textRu);
        article.setTextEn(textEn);
        article.setExplanationKk(explanationKk);
        article.setExplanationRu(explanationRu);
        article.setExplanationEn(explanationEn);
        articleRepository.save(article);
    }
}