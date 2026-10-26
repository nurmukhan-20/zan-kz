package kz.zankz.server.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import kz.zankz.server.model.PollVote;

public interface PollVoteRepository extends JpaRepository<PollVote, Long> {

    long countByOptionId(Long optionId);

    Optional<PollVote> findByPollIdAndUserId(Long pollId, Long userId);

    @Transactional
    void deleteByPollId(Long pollId);
}