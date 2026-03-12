package be.lutske.leolegacy.test.event;

import be.lutske.leolegacy.domain.event.DomainEvent;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EventTestBase {
    // Base class for domain event tests
    // Provides common setup and assertions
    
    @Test
    public void testEventBasics() {
        DomainEvent event = new RecipeCreatedEvent("1", new Date(), "recipe-123", "recipe-123", "Test Recipe");
        assertNotNull(event);
        assertNotNull(event.getEventId());
        assertNotNull(event.getTimestamp());
        assertNotNull(event.getAggregateId());
    }
}