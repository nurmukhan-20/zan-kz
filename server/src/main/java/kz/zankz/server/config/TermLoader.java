package kz.zankz.server.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import kz.zankz.server.model.Term;
import kz.zankz.server.repository.TermRepository;

@Component
public class TermLoader implements CommandLineRunner {

    private final TermRepository termRepository;

    public TermLoader(TermRepository termRepository) {
        this.termRepository = termRepository;
    }

    @Override
    public void run(String... args) {
        if (termRepository.count() > 0) {
            return;
        }

        term("Конституция", "Конституция", "Constitution",
                "Елдің басты заңы. Басқа заңдардың бәрі оған сәйкес болуға тиіс.",
                "Главный закон страны. Все остальные законы должны ему соответствовать.",
                "The main law of the country. All other laws must comply with it.");

        term("Егемендік", "Суверенитет", "Sovereignty",
                "Мемлекеттің өз ісін өзі шешу құқығы, басқа елдер оған бұйрық бере алмайды.",
                "Право государства самому решать свои дела. Другие страны не могут им управлять.",
                "The right of a state to decide its own affairs. Other countries cannot control it.");

        term("Унитарлы мемлекет", "Унитарное государство", "Unitary state",
                "Біртұтас мемлекет: бүкіл елде бір Конституция және бір орталық билік бар.",
                "Единое государство: во всей стране действуют одна Конституция и одна центральная власть.",
                "A single state: one Constitution and one central government apply across the whole country.");

        term("Президенттік республика", "Президентская республика", "Presidential republic",
                "Басқару үлгісі, онда мемлекеттің басшысы – халық сайлаған Президент.",
                "Форма правления, при которой государство возглавляет Президент, избранный народом.",
                "A form of government in which the state is headed by a President elected by the people.");

        term("Референдум", "Референдум", "Referendum",
                "Маңызды мәселе бойынша бүкіл халықтың дауыс беруі.",
                "Голосование всего народа по важному вопросу.",
                "A vote of the whole people on an important question.");

        term("Зайырлы мемлекет", "Светское государство", "Secular state",
                "Дін мемлекеттен бөлек тұратын мемлекет: ешбір дін міндетті емес.",
                "Государство, в котором религия отделена от государства и ни одна религия не обязательна.",
                "A state where religion is separate from the state and no religion is compulsory.");

        term("Ратификация", "Ратификация", "Ratification",
                "Мемлекеттің халықаралық шартты ресми түрде бекітуі, содан кейін ол міндетті болады.",
                "Официальное утверждение государством международного договора, после которого он становится обязательным.",
                "Official approval of an international treaty by the state, after which it becomes binding.");

        term("Заңның кері күші", "Обратная сила закона", "Retroactive effect of law",
                "Жаңа заңның ол шыққанға дейін болған оқиғаларға қолданылуы.",
                "Применение нового закона к событиям, которые произошли до его принятия.",
                "Applying a new law to events that happened before it was adopted.");
    }

    private void term(String termKk, String termRu, String termEn,
            String definitionKk, String definitionRu, String definitionEn) {
        Term term = new Term();
        term.setTermKk(termKk);
        term.setTermRu(termRu);
        term.setTermEn(termEn);
        term.setDefinitionKk(definitionKk);
        term.setDefinitionRu(definitionRu);
        term.setDefinitionEn(definitionEn);
        termRepository.save(term);
    }
}