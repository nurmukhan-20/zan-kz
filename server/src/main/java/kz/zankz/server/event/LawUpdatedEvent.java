package kz.zankz.server.event;

import kz.zankz.server.model.Law;

// Событие «закон обновлён». Шаблон Observer: сервис законов только сообщает о событии,
// а кто и как на него реагирует — решают слушатели
public record LawUpdatedEvent(Law law) {
}