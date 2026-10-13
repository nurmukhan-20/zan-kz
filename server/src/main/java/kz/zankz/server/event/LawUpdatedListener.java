package kz.zankz.server.event;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import kz.zankz.server.model.Law;
import kz.zankz.server.model.Notification;
import kz.zankz.server.service.NotificationService;

@Component
public class LawUpdatedListener {

    private final NotificationService notificationService;

    public LawUpdatedListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @EventListener
    public void onLawUpdated(LawUpdatedEvent event) {
        Law law = event.law();
        Notification notification = new Notification();
        notification.setLaw(law);
        notification.setTitleKk("Заң жаңартылды");
        notification.setTitleRu("Закон обновлён");
        notification.setTitleEn("Law updated");
        notification.setMessageKk("«" + law.getTitleKk() + "» туралы ақпарат жаңартылды. Жаңа нұсқасын қолданбадан оқыңыз.");
        notification.setMessageRu("Информация о документе «" + law.getTitleRu() + "» обновлена. Новую редакцию можно прочитать в приложении.");
        notification.setMessageEn("Information on \"" + law.getTitleEn() + "\" has been updated. Read the new version in the app.");
        notificationService.create(notification);
    }
}