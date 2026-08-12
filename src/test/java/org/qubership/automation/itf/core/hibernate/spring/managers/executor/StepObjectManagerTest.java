package org.qubership.automation.itf.core.hibernate.spring.managers.executor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.qubership.automation.itf.core.hibernate.spring.repositories.executor.StepRepository;
import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.model.jpa.callchain.CallChain;
import org.qubership.automation.itf.core.model.jpa.step.EmbeddedStep;
import org.qubership.automation.itf.core.model.jpa.step.IntegrationStep;
import org.qubership.automation.itf.core.model.jpa.step.SituationStep;
import org.qubership.automation.itf.core.model.jpa.step.Step;
import org.qubership.automation.itf.core.model.jpa.system.stub.Situation;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StepObjectManagerTest {

    @Mock
    private StepRepository stepRepository;

    @Mock
    private CallChain callChainParent;

    @Mock
    private Situation situationParent;

    @Mock
    private Step step;

    @InjectMocks
    private StepObjectManager manager;

    @BeforeEach
    void setUp() throws Exception {
        // Initialize subclasses map via reflection (since @PostConstruct is called by Spring)
        java.lang.reflect.Method initMethod = StepObjectManager.class.getDeclaredMethod("init");
        initMethod.setAccessible(true);
        initMethod.invoke(manager);
    }

    // ==================== protectedOnRemove TESTS ====================

    @Test
    void protectedOnRemove_ShouldDoNothing() {
        // given
        Step step = mock(Step.class);

        // when
        manager.protectedOnRemove(step);

        // then - no interactions with any repository
        verify(stepRepository, never()).delete(any());
        verify(stepRepository, never()).save(any());
    }

    // ==================== create TESTS ====================

    @Test
    void create_WithCallChainParentAndSituationStepType_ShouldCreateSituationStep() {
        // given
        String type = SituationStep.TYPE;
        ArrayList<Step> stepsList = new ArrayList<>();
        when(callChainParent.getSteps()).thenReturn(stepsList);
        when(stepRepository.save(any(Step.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Step result = manager.create(callChainParent, type);

        // then
        Assertions.assertNotNull(result);
        Assertions.assertInstanceOf(SituationStep.class, result);
        Assertions.assertEquals(callChainParent, result.getParent());
        Assertions.assertEquals(0, result.getOrder());
        Assertions.assertTrue(stepsList.contains(result));
        verify(stepRepository).save(result);
    }

    @Test
    void create_WithCallChainParentAndEmbeddedStepType_ShouldCreateEmbeddedStep() {
        // given
        String type = EmbeddedStep.TYPE;
        ArrayList<Step> stepsList = new ArrayList<>();
        when(callChainParent.getSteps()).thenReturn(stepsList);
        when(stepRepository.save(any(Step.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Step result = manager.create(callChainParent, type);

        // then
        Assertions.assertNotNull(result);
        Assertions.assertInstanceOf(EmbeddedStep.class, result);
        Assertions.assertEquals(callChainParent, result.getParent());
        Assertions.assertEquals(0, result.getOrder());
        Assertions.assertTrue(stepsList.contains(result));
        verify(stepRepository).save(result);
    }

    @Test
    void create_WithSituationParentAndIntegrationStepType_ShouldCreateIntegrationStep() {
        // given
        String type = IntegrationStep.TYPE;
        ArrayList<Step> stepsList = new ArrayList<>();
        when(situationParent.getSteps()).thenReturn(stepsList);
        when(stepRepository.save(any(Step.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Step result = manager.create(situationParent, type);

        // then
        Assertions.assertNotNull(result);
        Assertions.assertInstanceOf(IntegrationStep.class, result);
        Assertions.assertEquals(situationParent, result.getParent());
        Assertions.assertEquals(0, result.getOrder());
        Assertions.assertTrue(stepsList.contains(result));
        verify(stepRepository).save(result);
    }

    @Test
    void create_WithCallChainParentAndInvalidStepType_ShouldThrowIllegalArgumentException() {
        // given
        String invalidType = "InvalidStepType";

        // when & then
        IllegalArgumentException ex = Assertions.assertThrows(IllegalArgumentException.class,
                () -> manager.create(callChainParent, invalidType));
        Assertions.assertTrue(ex.getMessage().contains("Cannot create step of type " + invalidType));
        verify(stepRepository, never()).save(any());
    }

    @Test
    void create_WithSituationParentAndInvalidStepType_ShouldThrowIllegalArgumentException() {
        // given
        String invalidType = "InvalidStepType";

        // when & then
        IllegalArgumentException ex = Assertions.assertThrows(IllegalArgumentException.class,
                () -> manager.create(situationParent, invalidType));
        Assertions.assertTrue(ex.getMessage().contains("Cannot create step of type " + invalidType));
        verify(stepRepository, never()).save(any());
    }

    @Test
    void create_WithCallChainParentAndNullType_ShouldThrowIllegalArgumentException() {
        // when & then
        IllegalArgumentException ex = Assertions.assertThrows(IllegalArgumentException.class,
                () -> manager.create(callChainParent, null));
        Assertions.assertTrue(ex.getMessage().contains("Cannot create step of type null"));
        verify(stepRepository, never()).save(any());
    }

    @Test
    void create_WhenParentIsNull_ShouldStillCreateStep() {
        // given
        String type = EmbeddedStep.TYPE;
        when(stepRepository.save(any(Step.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Step result = manager.create(null, type);

        // then
        Assertions.assertNotNull(result);
        Assertions.assertInstanceOf(EmbeddedStep.class, result);
        Assertions.assertNull(result.getParent());
        verify(stepRepository).save(result);
    }

    @Test
    void create_WhenCallChainParentHasExistingSteps_ShouldSetOrderToCurrentSize() {
        // given
        String type = SituationStep.TYPE;
        ArrayList<Step> existingSteps = new ArrayList<>();
        existingSteps.add(mock(Step.class));
        existingSteps.add(mock(Step.class));
        when(callChainParent.getSteps()).thenReturn(existingSteps);
        when(stepRepository.save(any(Step.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Step result = manager.create(callChainParent, type);

        // then
        Assertions.assertEquals(2, result.getOrder());
        Assertions.assertTrue(callChainParent.getSteps().contains(result));
    }

    @Test
    void create_WhenSituationParentHasExistingSteps_ShouldSetOrderToCurrentSize() {
        // given
        String type = IntegrationStep.TYPE;
        ArrayList<Step> existingSteps = new ArrayList<>();
        existingSteps.add(mock(Step.class));
        existingSteps.add(mock(Step.class));
        when(situationParent.getSteps()).thenReturn(existingSteps);
        when(stepRepository.save(any(Step.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Step result = manager.create(situationParent, type);

        // then
        Assertions.assertEquals(2, result.getOrder());
        Assertions.assertTrue(situationParent.getSteps().contains(result));
    }

    // ==================== create() without parameters TESTS ====================

    @Test
    void create_WithoutParameters_ShouldThrowIllegalArgumentException() {
        // when & then
        IllegalArgumentException ex = Assertions.assertThrows(IllegalArgumentException.class,
                () -> manager.create());
        Assertions.assertTrue(ex.getMessage().contains("Cannot create step of unknown type"));
    }

    // ==================== afterDelete TESTS ====================

    @Test
    void afterDelete_WhenParentIsCallChain_ShouldRemoveStepFromCallChainCollection() {
        // given
        List<Step> steps = new ArrayList<>();
        steps.add(step);
        when(step.getParent()).thenReturn(callChainParent);
        when(callChainParent.getSteps()).thenReturn(steps);

        // when
        manager.afterDelete(step);

        // then
        Assertions.assertFalse(steps.contains(step));
    }

    @Test
    void afterDelete_WhenParentIsNotStepContainer_ShouldNotRemoveAnything() {
        // given
        Storable nonContainerParent = mock(Storable.class);
        when(step.getParent()).thenReturn(nonContainerParent);

        // when
        manager.afterDelete(step);

        // then
        verify(callChainParent, never()).getSteps();
        verify(situationParent, never()).getSteps();
    }

    @Test
    void afterDelete_WhenParentIsNull_ShouldDoNothing() {
        // given
        when(step.getParent()).thenReturn(null);

        // when
        manager.afterDelete(step);

        // then
        verify(callChainParent, never()).getSteps();
        verify(situationParent, never()).getSteps();
    }

    @Test
    void afterDelete_WhenStepNotInParentCollection_ShouldHandleGracefully() {
        // given
        List<Step> steps = new ArrayList<>();
        when(step.getParent()).thenReturn(callChainParent);
        when(callChainParent.getSteps()).thenReturn(steps);

        // when - should not throw exception
        manager.afterDelete(step);

        // then
        Assertions.assertFalse(steps.contains(step));
    }

    // ==================== Edge Cases for create method ====================

    @Test
    void create_WithNullParentButValidType_ShouldCreateStepWithDefaultOrder() {
        // given
        String type = IntegrationStep.TYPE;
        when(stepRepository.save(any(Step.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Step result = manager.create(null, type);

        // then
        Assertions.assertNotNull(result);
        Assertions.assertInstanceOf(IntegrationStep.class, result);
        Assertions.assertNull(result.getParent());
        Assertions.assertEquals(0, result.getOrder());
    }

    @Test
    void create_ShouldCreateAllStepTypesWithCorrectParentChildRelationships() {
        // given
        when(stepRepository.save(any(Step.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // SituationStep belongs to CallChain
        when(callChainParent.getSteps()).thenReturn(new ArrayList<>());
        Step situationStep = manager.create(callChainParent, SituationStep.TYPE);
        Assertions.assertInstanceOf(SituationStep.class, situationStep);
        Assertions.assertEquals(callChainParent, situationStep.getParent());

        // EmbeddedStep belongs to CallChain
        Step embeddedStep = manager.create(callChainParent, EmbeddedStep.TYPE);
        Assertions.assertInstanceOf(EmbeddedStep.class, embeddedStep);
        Assertions.assertEquals(callChainParent, embeddedStep.getParent());

        // IntegrationStep belongs to Situation
        when(situationParent.getSteps()).thenReturn(new ArrayList<>());
        Step integrationStep = manager.create(situationParent, IntegrationStep.TYPE);
        Assertions.assertInstanceOf(IntegrationStep.class, integrationStep);
        Assertions.assertEquals(situationParent, integrationStep.getParent());
    }

    @Test
    void create_WhenCallChainParentHasSteps_ShouldAddToEndOfList() {
        // given
        String type = SituationStep.TYPE;
        ArrayList<Step> existingSteps = new ArrayList<>();
        Step existingStep1 = mock(Step.class);
        Step existingStep2 = mock(Step.class);
        existingSteps.add(existingStep1);
        existingSteps.add(existingStep2);
        when(callChainParent.getSteps()).thenReturn(existingSteps);
        when(stepRepository.save(any(Step.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Step result = manager.create(callChainParent, type);

        // then
        Assertions.assertEquals(2, result.getOrder());
        Assertions.assertEquals(3, existingSteps.size());
        Assertions.assertEquals(result, existingSteps.get(2));
    }
}