package be.lutske.leolegacy.domain.event;

import java.util.Date;

public class CategoryUpdatedEvent implements DomainEvent {
    private String eventId;
    private Date timestamp;
    private String aggregateId;
    private String categoryId;
    private String name;

    public CategoryUpdatedEvent(String eventId, Date timestamp, String aggregateId, String categoryId, String name) {
        this.eventId = eventId;
        this.timestamp = timestamp;
        this.aggregateId = aggregateId;
        this.categoryId = categoryId;
        this.name = name;
    }

    // Getters and setters
    public String getEventId() { return eventId; }
    public Date getTimestamp() { return timestamp; }
    public String getAggregateId() { return aggregateId; }
    public String getCategoryId() { return categoryId; }
    public String getName() { return name; }
}