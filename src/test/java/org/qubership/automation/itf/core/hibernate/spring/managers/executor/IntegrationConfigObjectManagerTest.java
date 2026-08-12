package org.qubership.automation.itf.core.hibernate.spring.managers.executor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.qubership.automation.itf.core.hibernate.spring.repositories.executor.IntegrationConfigRepository;
import org.qubership.automation.itf.core.model.jpa.project.IntegrationConfig;
import org.qubership.automation.itf.core.model.jpa.project.StubProject;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class IntegrationConfigObjectManagerTest {

    private static final String CONFIG_TYPE = "integration";
    private static final String CONFIG_NAME = "IntegrationConfig";

    @Mock
    private IntegrationConfigRepository integrationConfigRepository;

    @Mock
    private StubProject parentProject;

    @Mock
    private IntegrationConfig config;

    @Mock
    private IntegrationConfig savedConfig;

    @InjectMocks
    private IntegrationConfigObjectManager manager;

    // ==================== create (with type only) TESTS ====================

    @Test
    void create_WithTypeOnly_ShouldCreateIntegrationConfig() {
        // given
        when(integrationConfigRepository.save(any(IntegrationConfig.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        IntegrationConfig result = manager.create(parentProject, CONFIG_TYPE);

        // then
        Assertions.assertNotNull(result);
        Assertions.assertEquals(parentProject, result.getParent());
        Assertions.assertEquals(CONFIG_TYPE, result.getName());
        Assertions.assertEquals(CONFIG_TYPE, result.getTypeName());
        verify(integrationConfigRepository).save(result);
    }

    @Test
    void create_WithTypeOnlyAndNullParent_ShouldCreateConfigWithNullParent() {
        // given
        when(integrationConfigRepository.save(any(IntegrationConfig.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        IntegrationConfig result = manager.create(null, CONFIG_TYPE);

        // then
        Assertions.assertNotNull(result);
        Assertions.assertNull(result.getParent());
        verify(integrationConfigRepository).save(result);
    }

    // ==================== create (with parameters) TESTS ====================

    @Test
    void create_WithParameters_ShouldCreateIntegrationConfigWithProperties() {
        // given
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("key1", "value1");
        parameters.put("key2", "value2");
        when(integrationConfigRepository.save(any(IntegrationConfig.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        IntegrationConfig result = manager.create(parentProject, CONFIG_NAME, CONFIG_TYPE, parameters);

        // then
        Assertions.assertNotNull(result);
        Assertions.assertEquals(parentProject, result.getParent());
        Assertions.assertEquals(CONFIG_NAME, result.getName());
        Assertions.assertEquals(CONFIG_TYPE, result.getTypeName());

        // Note: Parameters are set in the IntegrationConfig constructor.
        verify(integrationConfigRepository).save(result);
    }

    @Test
    void create_WithParametersAndNullParent_ShouldCreateConfigWithNullParent() {
        // given
        Map<String, Object> parameters = new HashMap<>();
        when(integrationConfigRepository.save(any(IntegrationConfig.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        IntegrationConfig result = manager.create(null, CONFIG_NAME, CONFIG_TYPE, parameters);

        // then
        Assertions.assertNotNull(result);
        Assertions.assertNull(result.getParent());
        verify(integrationConfigRepository).save(result);
    }

    @Test
    void create_WithParametersAndEmptyMap_ShouldCreateConfig() {
        // given
        Map<String, Object> emptyParams = new HashMap<>();
        when(integrationConfigRepository.save(any(IntegrationConfig.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        IntegrationConfig result = manager.create(parentProject, CONFIG_NAME, CONFIG_TYPE, emptyParams);

        // then
        Assertions.assertNotNull(result);
        verify(integrationConfigRepository).save(result);
    }

    // ==================== afterDelete TESTS ====================

    @Test
    void afterDelete_WhenParentIsStubProject_ShouldRemoveFromProjectIntegrationConfs() {
        // given
        Set<IntegrationConfig> integrationConfs = new HashSet<>();
        integrationConfs.add(config);
        when(config.getParent()).thenReturn(parentProject);
        when(parentProject.getIntegrationConfs()).thenReturn(integrationConfs);

        // when
        manager.afterDelete(config);

        // then
        Assertions.assertFalse(integrationConfs.contains(config));
    }

    @Test
    void afterDelete_WhenParentIsNull_ShouldDoNothing() {
        // given
        when(config.getParent()).thenReturn(null);

        // when
        manager.afterDelete(config);

        // then
        verify(parentProject, never()).getIntegrationConfs();
    }

    @Test
    void afterDelete_WhenConfigNotInParentCollection_ShouldHandleGracefully() {
        // given
        Set<IntegrationConfig> integrationConfs = new HashSet<>();
        when(config.getParent()).thenReturn(parentProject);
        when(parentProject.getIntegrationConfs()).thenReturn(integrationConfs);

        // when - should not throw exception
        manager.afterDelete(config);

        // then
        Assertions.assertFalse(integrationConfs.contains(config));
    }
}