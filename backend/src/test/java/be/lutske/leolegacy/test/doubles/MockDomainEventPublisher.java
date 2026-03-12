package be.lutske.leolegacy.test.doubles;

import be.lutske.leolegacy.domain.event.DomainEvent;
import be.lutske.leolegacy.domain.event.DomainEventPublisher;
import java.util.List;
import java.util.ArrayList;
import java.util.ArrayList;
import java.util.List;

public class MockDomainEventPublisher implements DomainEventPublisher {
    private final List<DomainEvent> publishedEvents = new ArrayList<>();

    @Override
    public void publish(DomainEvent event) {
        publishedEvents.add(event);
    }

    public List<DomainEvent> getPublishedEvents() {
        return publishedEvents;
    }

    public void clearEvents() {
        publishedEvents.clear();
    }
}