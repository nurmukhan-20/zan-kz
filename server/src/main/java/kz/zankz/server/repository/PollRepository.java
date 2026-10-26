package kz.zankz.server.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import kz.zankz.server.model.Poll;

public interface PollRepository extends JpaRepository<Poll, Long> {

    List<Poll> findAllByOrderByActiveDescEventDateDesc();   // сначала открытые, потом самые новые

    boolean existsByTitleRu(String titleRu);
}