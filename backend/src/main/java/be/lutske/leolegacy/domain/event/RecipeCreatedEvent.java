package be.lutske.leolegacy.domain.event;

import java.util.Date;

public class RecipeCreatedEvent implements DomainEvent {
    private String eventId;
    private Date timestamp;
    private String aggregateId;
    private String recipeId;
    private String title;

    public RecipeCreatedEvent(String eventId, Date timestamp, String aggregateId, String recipeId, String title) {
        this.eventId = eventId;
        this.timestamp = timestamp;
        this.aggregateId = aggregateId;
        this.recipeId = recipeId;
        this.title = title;
    }

    // Getters and setters
    public String getEventId() { return eventId; }
    public Date getTimestamp() { return timestamp; }
    public String getAggregateId() { return aggregateId; }
    public String getRecipeId() { return recipeId; }
    public String getTitle() { return title; }
}