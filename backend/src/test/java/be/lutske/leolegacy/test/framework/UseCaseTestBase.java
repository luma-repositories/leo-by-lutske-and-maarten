package be.lutske.leolegacy.test.framework;

import be.lutske.leolegacy.domain.event.DomainEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;

public abstract class UseCaseTestBase {
    protected DomainEventPublisher eventPublisher;

    @BeforeEach
    public void setUp() {
        eventPublisher = Mockito.mock(DomainEventPublisher.class);
    }

    protected void verifyEventPublished(Class<?> eventType) {
        Mockito.verify(eventPublisher).publish(Mockito.argThat(event ->
            event.getClass().equals(eventType)
        ));
    }
}