package kz.zankz.server.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kz.zankz.server.dto.PollRequest;
import kz.zankz.server.dto.PollResponse;
import kz.zankz.server.exception.BadRequestException;
import kz.zankz.server.exception.ResourceNotFoundException;
import kz.zankz.server.model.Poll;
import kz.zankz.server.model.PollOption;
import kz.zankz.server.model.PollVote;
import kz.zankz.server.model.User;
import kz.zankz.server.repository.PollRepository;
import kz.zankz.server.repository.PollVoteRepository;

// Голосование в приложении: список, голос (один раз), результаты; создание/закрытие/удаление — ADMIN
@Service
public class PollService {

    private static final Set<String> KINDS = Set.of("REFERENDUM", "ELECTION", "PROPOSAL");

    private final PollRepository pollRepository;
    private final PollVoteRepository voteRepository;

    public PollService(PollRepository pollRepository, PollVoteRepository voteRepository) {
        this.pollRepository = pollRepository;
        this.voteRepository = voteRepository;
    }

    @Transactional(readOnly = true)
    public List<PollResponse> getAll(User user) {
        return pollRepository.findAllByOrderByActiveDescEventDateDesc().stream()
                .map(poll -> toResponse(poll, user))
                .toList();
    }

    @Transactional
    public PollResponse vote(User user, Long pollId, Long optionId) {
        Poll poll = find(pollId);
        if (!poll.isActive()) {
            throw new BadRequestException("Голосование закрыто");
        }
        PollOption option = poll.getOptions().stream()
                .filter(o -> o.getId().equals(optionId))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Такого варианта нет в этом голосовании"));
        if (voteRepository.findByPollIdAndUserId(pollId, user.getId()).isPresent()) {
            throw new BadRequestException("Вы уже проголосовали");
        }
        PollVote vote = new PollVote();
        vote.setPoll(poll);
        vote.setOption(option);
        vote.setUser(user);
        vote.setCreatedAt(LocalDateTime.now());
        try {
            voteRepository.saveAndFlush(vote);
        } catch (DataIntegrityViolationException e) {
            // два быстрых нажатия подряд — база не даст сохранить второй голос
            throw new BadRequestException("Вы уже проголосовали");
        }
        return toResponse(poll, user);
    }

    @Transactional
    public PollResponse create(PollRequest r, User admin) {
        String kind = r.kind().trim().toUpperCase(Locale.ROOT);
        if (!KINDS.contains(kind)) {
            throw new BadRequestException("Вид голосования: REFERENDUM, ELECTION или PROPOSAL");
        }
        Poll poll = new Poll();
        poll.setKind(kind);
        poll.setEventDate(r.eventDate());
        poll.setTitleKk(r.titleKk().trim());
        poll.setTitleRu(r.titleRu().trim());
        poll.setTitleEn(r.titleEn().trim());
        poll.setDescriptionKk(r.descriptionKk());
        poll.setDescriptionRu(r.descriptionRu());
        poll.setDescriptionEn(r.descriptionEn());
        poll.setQuestionKk(r.questionKk().trim());
        poll.setQuestionRu(r.questionRu().trim());
        poll.setQuestionEn(r.questionEn().trim());
        poll.setActive(true);
        for (PollRequest.Option o : r.options()) {
            poll.addOption(new PollOption(o.textKk().trim(), o.textRu().trim(), o.textEn().trim()));
        }
        return toResponse(pollRepository.save(poll), admin);
    }

    @Transactional
    public PollResponse close(Long pollId, User admin) {
        Poll poll = find(pollId);
        poll.setActive(false);
        return toResponse(pollRepository.save(poll), admin);
    }

    @Transactional
    public void delete(Long pollId) {
        Poll poll = find(pollId);
        voteRepository.deleteByPollId(poll.getId());
        pollRepository.delete(poll);
    }

    private Poll find(Long id) {
        return pollRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Голосование не найдено"));
    }

    private PollResponse toResponse(Poll p, User user) {
        List<PollResponse.Option> options = p.getOptions().stream()
                .map(o -> new PollResponse.Option(o.getId(), o.getTextKk(), o.getTextRu(), o.getTextEn(),
                        voteRepository.countByOptionId(o.getId())))
                .toList();
        long total = options.stream().mapToLong(PollResponse.Option::votes).sum();
        Long myOptionId = user == null ? null
                : voteRepository.findByPollIdAndUserId(p.getId(), user.getId())
                        .map(v -> v.getOption().getId()).orElse(null);
        return new PollResponse(p.getId(), p.getKind(), p.getEventDate(), p.isActive(),
                p.getTitleKk(), p.getTitleRu(), p.getTitleEn(),
                p.getDescriptionKk(), p.getDescriptionRu(), p.getDescriptionEn(),
                p.getQuestionKk(), p.getQuestionRu(), p.getQuestionEn(),
                options, total, myOptionId);
    }
}