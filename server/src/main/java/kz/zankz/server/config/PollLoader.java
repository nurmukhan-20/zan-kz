package kz.zankz.server.config;

import java.time.LocalDate;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import kz.zankz.server.model.Poll;
import kz.zankz.server.model.PollOption;
import kz.zankz.server.repository.PollRepository;

// При запуске добавляет 3 голосования (как в бюллетене), если их ещё нет:
// референдум по АЭС, референдум по новой Конституции, выборы в Курултай.
// В приложении это неофициальный опрос пользователей.
@Component
public class PollLoader implements CommandLineRunner {

    private final PollRepository pollRepository;

    public PollLoader(PollRepository pollRepository) {
        this.pollRepository = pollRepository;
    }

    @Override
    public void run(String... args) {
        int added = 0;

        // ---------- 1. Референдум по АЭС ----------
        Poll aes = poll("REFERENDUM", LocalDate.of(2024, 10, 6),
                "Атом электр стансасын салу туралы республикалық референдум",
                "Республиканский референдум о строительстве АЭС",
                "National referendum on building a nuclear power plant",
                "Азаматтар Қазақстанда атом электр стансасын (АЭС) салу-салмауды шешті. АЭС — ядролық отын арқылы "
                        + "электр энергиясын өндіретін станса. Мәселе: елге жеткілікті әрі тұрақты электр энергиясы қажет.",
                "Граждане решали, строить ли в Казахстане атомную электростанцию (АЭС). АЭС — это станция, которая "
                        + "производит электроэнергию из ядерного топлива. Вопрос важен, потому что стране нужно "
                        + "достаточно стабильного электричества.",
                "Citizens decided whether Kazakhstan should build a nuclear power plant (NPP). An NPP produces electricity "
                        + "from nuclear fuel. The question matters because the country needs enough stable electricity.",
                "Қазақстанда атом электр стансасын салуға келісесіз бе?",
                "Согласны ли Вы со строительством атомной электростанции в Казахстане?",
                "Do you agree with the construction of a nuclear power plant in Kazakhstan?");
        aes.addOption(new PollOption("Иә, келісемін", "Да, согласен(-на)", "Yes, I agree"));
        aes.addOption(new PollOption("Жоқ, келіспеймін", "Нет, не согласен(-на)", "No, I do not agree"));
        added += save(aes);

        // ---------- 2. Референдум по новой Конституции ----------
        Poll constitution = poll("REFERENDUM", LocalDate.of(2026, 3, 15),
                "Жаңа Конституция бойынша республикалық референдум",
                "Республиканский референдум по новой Конституции",
                "National referendum on the new Constitution",
                "Азаматтар жаңа Конституцияны — елдің басты заңын қабылдау-қабылдамауды шешті. Конституция "
                        + "азаматтардың құқықтарын, билік қалай құрылатынын және заңдар қалай қабылданатынын белгілейді.",
                "Граждане решали, принять ли новую Конституцию — главный закон страны. Конституция определяет права "
                        + "граждан, устройство власти и то, как принимаются законы.",
                "Citizens decided whether to adopt the new Constitution, the main law of the country. It sets out "
                        + "citizens' rights, how power is organised and how laws are adopted.",
                "Қазақстан Республикасының жаңа Конституциясын қабылдайсыз ба?",
                "Принимаете ли Вы новую Конституцию Республики Казахстан?",
                "Do you accept the new Constitution of the Republic of Kazakhstan?");
        constitution.addOption(new PollOption("Иә", "Да", "Yes"));
        constitution.addOption(new PollOption("Жоқ", "Нет", "No"));
        added += save(constitution);

        // ---------- 3. Выборы в Курултай (партии — в порядке бюллетеня) ----------
        Poll kurultai = poll("ELECTION", LocalDate.of(2026, 8, 23),
                "Жаңа парламент — Құрылтай депутаттарын сайлау",
                "Выборы депутатов нового парламента — Курултая",
                "Elections of deputies of the new parliament, the Kurultai",
                "Құрылтай — Қазақстанның жаңа бір палаталы парламенті: 5 жылға сайланатын 145 депутат. Ол елдің "
                        + "заңдарын қабылдайды. Сайлаушы адамға емес, бір партияның тізіміне немесе «Барлығына қарсымын» "
                        + "деген жолға дауыс береді.",
                "Курултай — новый однопалатный парламент Казахстана: 145 депутатов, избираемых на 5 лет. Он принимает "
                        + "законы страны. Избиратель голосует не за человека, а за список одной партии или за строку "
                        + "«Против всех».",
                "The Kurultai is Kazakhstan's new single-chamber parliament: 145 deputies elected for 5 years. It adopts "
                        + "the country's laws. Voters choose not a person but one party list, or \"Against all\".",
                "Қай партияға дауыс бересіз?",
                "За какую партию Вы голосуете?",
                "Which party do you vote for?");
        kurultai.addOption(new PollOption("«Әділет» партиясы", "Партия «Әділет»", "Adilet party"));
        kurultai.addOption(new PollOption("Жалпыұлттық социал-демократиялық партия (ЖСДП)",
                "Общенациональная социал-демократическая партия (ОСДП)", "National Social Democratic Party (OSDP)"));
        kurultai.addOption(new PollOption("«Байтақ» жасылдар партиясы", "Партия зелёных «Байтақ»", "Baytak Green Party"));
        kurultai.addOption(new PollOption("Қазақстан Халық партиясы", "Народная партия Казахстана", "People's Party of Kazakhstan"));
        kurultai.addOption(new PollOption("«Ауыл» партиясы", "Партия «Ауыл»", "Auyl party"));
        kurultai.addOption(new PollOption("«Ақ жол» партиясы", "Партия «Ак жол»", "Ak Zhol party"));
        kurultai.addOption(new PollOption("«Respublica» партиясы", "Партия «Respublica»", "Respublica party"));
        kurultai.addOption(new PollOption("Барлығына қарсымын", "Против всех", "Against all"));
        added += save(kurultai);

        if (added > 0) {
            System.out.println("Голосования: добавлено " + added);
        }
    }

    private static Poll poll(String kind, LocalDate date, String tKk, String tRu, String tEn,
            String dKk, String dRu, String dEn, String qKk, String qRu, String qEn) {
        Poll p = new Poll();
        p.setKind(kind);
        p.setEventDate(date);
        p.setTitleKk(tKk);
        p.setTitleRu(tRu);
        p.setTitleEn(tEn);
        p.setDescriptionKk(dKk);
        p.setDescriptionRu(dRu);
        p.setDescriptionEn(dEn);
        p.setQuestionKk(qKk);
        p.setQuestionRu(qRu);
        p.setQuestionEn(qEn);
        p.setActive(true);
        return p;
    }

    // сохраняет, если голосования с таким названием ещё нет
    private int save(Poll p) {
        if (pollRepository.existsByTitleRu(p.getTitleRu())) {
            return 0;
        }
        pollRepository.save(p);
        return 1;
    }
}