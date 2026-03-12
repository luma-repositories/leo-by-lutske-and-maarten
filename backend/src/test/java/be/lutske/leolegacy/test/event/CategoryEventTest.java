package be.lutske.leolegacy.test.event;

import be.lutske.leolegacy.domain.event.CategoryUpdatedEvent;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CategoryEventTest {
    @Test
    public void testCategoryUpdatedEvent() {
        CategoryUpdatedEvent event = new CategoryUpdatedEvent("1", new Date(), "category-456", "category-456", "Vegetables");
        
        assertEquals("1", event.getEventId());
        assertEquals("category-456", event.getAggregateId());
        assertEquals("Vegetables", event.getName());
    }
}