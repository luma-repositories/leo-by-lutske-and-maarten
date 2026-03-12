package be.lutske.leolegacy.domain.event;

import java.util.Date;

public interface DomainEvent {
    String getEventId();
    Date getTimestamp();
    String getAggregateId();
}