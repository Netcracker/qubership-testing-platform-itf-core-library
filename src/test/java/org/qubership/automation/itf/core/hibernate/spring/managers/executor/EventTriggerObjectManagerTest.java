package org.qubership.automation.itf.core.hibernate.spring.managers.executor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.qubership.automation.itf.core.hibernate.spring.repositories.executor.EventTriggerRepository;
import org.qubership.automation.itf.core.model.jpa.system.stub.EventTrigger;
import org.qubership.automation.itf.core.model.jpa.system.stub.OperationEventTrigger;
import org.qubership.automation.itf.core.model.jpa.system.stub.Situation;
import org.qubership.automation.itf.core.model.jpa.system.stub.SituationEventTrigger;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EventTriggerObjectManagerTest {

    @Mock
    private EventTriggerRepository<EventTrigger> eventTriggerRepository;

    @Mock
    private Situation parent;

    @Mock
    private EventTrigger savedTrigger;

    private TestEventTriggerObjectManager manager;

    // Concrete implementation for testing
    static class TestEventTriggerObjectManager extends EventTriggerObjectManager<EventTrigger> {
        public TestEventTriggerObjectManager(EventTriggerRepository<EventTrigger> repository) {
            super(EventTrigger.class, repository);
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        manager = new TestEventTriggerObjectManager(eventTriggerRepository);
        // Initialize subclasses map via reflection
        java.lang.reflect.Method initMethod = EventTriggerObjectManager.class.getDeclaredMethod("init");
        initMethod.setAccessible(true);
        initMethod.invoke(manager);
    }

    // ==================== protectedOnRemove TESTS ====================

    @Test
    void protectedOnRemove_ShouldDoNothing() {
        // given
        EventTrigger trigger = new OperationEventTrigger();

        // when
        manager.protectedOnRemove(trigger);

        // then
        verify(eventTriggerRepository, never()).delete(any());
        verify(eventTriggerRepository, never()).save(any());
    }

    // ==================== create TESTS ====================

    @Test
    void create_WithOperationEventTriggerType_ShouldCreateOperationEventTrigger() {
        // given
        String type = OperationEventTrigger.TYPE;
        when(eventTriggerRepository.save(any(EventTrigger.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        EventTrigger result = manager.create(parent, type);

        // then
        Assertions.assertNotNull(result);
        Assertions.assertInstanceOf(OperationEventTrigger.class, result);
        Assertions.assertEquals(parent, result.getParent());
        verify(eventTriggerRepository).save(result);
    }

    @Test
    void create_WithSituationEventTriggerType_ShouldCreateSituationEventTrigger() {
        // given
        String type = SituationEventTrigger.TYPE;
        when(eventTriggerRepository.save(any(EventTrigger.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        EventTrigger result = manager.create(parent, type);

        // then
        Assertions.assertNotNull(result);
        Assertions.assertInstanceOf(SituationEventTrigger.class, result);
        Assertions.assertEquals(parent, result.getParent());
        verify(eventTriggerRepository).save(result);
    }

    @Test
    void create_WithInvalidType_ShouldThrowIllegalArgumentException() {
        // given
        String invalidType = "InvalidTriggerType";

        // when & then
        IllegalArgumentException ex = Assertions.assertThrows(IllegalArgumentException.class,
                () -> manager.create(parent, invalidType));
        Assertions.assertTrue(ex.getMessage().contains("Cannot create trigger of type " + invalidType));
        verify(eventTriggerRepository, never()).save(any());
    }

    @Test
    void create_WithNullType_ShouldThrowIllegalArgumentException() {
        // when & then
        IllegalArgumentException ex = Assertions.assertThrows(IllegalArgumentException.class,
                () -> manager.create(parent, null));
        Assertions.assertTrue(ex.getMessage().contains("Cannot create trigger of type null"));
        verify(eventTriggerRepository, never()).save(any());
    }

    @Test
    void create_WithNullParent_ShouldStillCreateTrigger() {
        // given
        String type = OperationEventTrigger.TYPE;
        when(eventTriggerRepository.save(any(EventTrigger.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        EventTrigger result = manager.create(null, type);

        // then
        Assertions.assertNotNull(result);
        Assertions.assertInstanceOf(OperationEventTrigger.class, result);
        Assertions.assertNull(result.getParent());
        verify(eventTriggerRepository).save(result);
    }

    // ==================== create() without parameters TESTS ====================

    @Test
    void create_WithoutParameters_ShouldThrowIllegalArgumentException() {
        // when & then
        IllegalArgumentException ex = Assertions.assertThrows(IllegalArgumentException.class,
                () -> manager.create());
        Assertions.assertTrue(ex.getMessage().contains("Cannot create trigger of unknown type"));
    }
}