package kz.zankz.server.event;

import kz.zankz.server.model.Law;

public record LawUpdatedEvent(Law law) {
}