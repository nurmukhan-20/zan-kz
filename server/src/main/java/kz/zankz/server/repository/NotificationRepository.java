package kz.zankz.server.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import kz.zankz.server.model.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findAllByOrderByCreatedAtDesc();
}