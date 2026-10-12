package kz.zankz.server.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import kz.zankz.server.exception.ResourceNotFoundException;
import kz.zankz.server.model.Notification;
import kz.zankz.server.repository.NotificationRepository;

// Логика уведомлений
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public List<Notification> getAll() {
        return notificationRepository.findAllByOrderByCreatedAtDesc();
    }

    public Notification getById(Long id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Уведомление с номером " + id + " не найдено"));
    }

    public Notification create(Notification notification) {
        notification.setId(null);
        notification.setCreatedAt(LocalDateTime.now());
        return notificationRepository.save(notification);
    }

    public void delete(Long id) {
        notificationRepository.delete(getById(id));
    }
}