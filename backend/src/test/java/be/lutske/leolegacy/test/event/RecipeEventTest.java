package be.lutske.leolegacy.test.event;

import be.lutske.leolegacy.domain.event.RecipeCreatedEvent;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RecipeEventTest {
    @Test
    public void testRecipeCreatedEvent() {
        RecipeCreatedEvent event = new RecipeCreatedEvent("1", new Date(), "recipe-123", "recipe-123", "Test Recipe");
        
        assertEquals("1", event.getEventId());
        assertEquals("recipe-123", event.getAggregateId());
        assertEquals("Test Recipe", event.getTitle());
    }
}