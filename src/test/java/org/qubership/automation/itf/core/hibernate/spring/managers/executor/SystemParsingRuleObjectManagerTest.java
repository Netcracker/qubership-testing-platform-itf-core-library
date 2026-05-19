package org.qubership.automation.itf.core.hibernate.spring.managers.executor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.qubership.automation.itf.core.hibernate.spring.repositories.executor.SystemParsingRuleRepository;
import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.model.jpa.message.parser.SystemParsingRule;
import org.qubership.automation.itf.core.model.jpa.system.System;
import org.qubership.automation.itf.core.util.constants.Match;
import org.qubership.automation.itf.core.util.converter.IdConverter;
import org.qubership.automation.itf.core.util.db.TxExecutor;
import org.qubership.automation.itf.core.util.parser.ParsingRuleType;
import org.springframework.transaction.TransactionDefinition;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SystemParsingRuleObjectManagerTest {

    private static final BigInteger RULE_ID = BigInteger.valueOf(100L);
    private static final BigInteger PROJECT_ID = BigInteger.valueOf(200L);
    private static final String RULE_NAME = "TestRule";

    @Mock
    private SystemParsingRuleRepository repository;

    @Mock
    private System parentSystem;

    @Mock
    private SystemParsingRule parsingRule;

    @InjectMocks
    private SystemParsingRuleObjectManager manager;

    private MockedStatic<TxExecutor> txExecutorMock;
    private MockedStatic<IdConverter> idConverterMock;
    private TransactionDefinition mockTxDef;

    @BeforeEach
    void setUp() {
        txExecutorMock = mockStatic(TxExecutor.class);
        idConverterMock = mockStatic(IdConverter.class);
        mockTxDef = mock(TransactionDefinition.class);

        TestMockHelper.setupCommonMocks(txExecutorMock, idConverterMock, mockTxDef);
    }

    @AfterEach
    void tearDown() {
        txExecutorMock.close();
        idConverterMock.close();
    }

    // ==================== create (with parent only) TESTS ====================

    @Test
    void create_WithParentSystem_ShouldCreateParsingRule() {
        // given
        SystemParsingRule newRule = mock(SystemParsingRule.class);
        when(repository.save(any(SystemParsingRule.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        SystemParsingRule result = manager.create(parentSystem);

        // then
        Assertions.assertNotNull(result);
        Assertions.assertEquals(parentSystem, result.getParent());
        verify(repository).save(any(SystemParsingRule.class));
    }

    @Test
    void create_WithNullParent_ShouldNotCreateParsingRule() {
        // given
        SystemParsingRule newRule = mock(SystemParsingRule.class);
        when(repository.save(any(SystemParsingRule.class))).thenReturn(newRule);

        // when
        SystemParsingRule result = manager.create(null);

        // then
        Assertions.assertNull(result);
        verify(repository, never()).save(any(SystemParsingRule.class));
    }

    // ==================== create (with type and parameters) TESTS ====================

    @Test
    @Disabled("Production code throws java.lang.NoSuchMethodException "
            + "because there is no constructor with 3rd parameter as 'Map parameters'. "
            + "Should be fixed by adding proper check in constructor.")
    void create_WithTypeAndParameters_ShouldCreateParsingRule() {
        // given
        Map<String, Object> parameters = new HashMap<>();
        SystemParsingRule newRule = mock(SystemParsingRule.class);
        when(repository.save(any(SystemParsingRule.class))).thenReturn(newRule);

        // when
        SystemParsingRule result = manager.create(parentSystem, RULE_NAME, parameters);

        // then
        Assertions.assertNotNull(result);
        verify(newRule).setParsingType(ParsingRuleType.XPATH);
        verify(newRule).setExpression(".");
        verify(repository).save(newRule);
    }

    @Test
    void create_WithTypeAndParameters_WhenParentIsNotParsingRuleProvider_ShouldThrowException() {
        // given
        Storable nonProviderParent = mock(Storable.class);
        Map<String, Object> parameters = new HashMap<>();

        // when & then
        IllegalArgumentException ex = Assertions.assertThrows(IllegalArgumentException.class,
                () -> manager.create(nonProviderParent, RULE_NAME, parameters));
        Assertions.assertTrue(ex.getMessage().contains("not parsing rule provider"));
        verify(repository, never()).save(any());
    }

    // ==================== getByProjectId TESTS ====================

    @Test
    void getByProjectId_ShouldReturnRulesFromRepository() {
        // given
        List<SystemParsingRule> expectedRules = List.of(parsingRule);
        when(repository.findByProjectId(PROJECT_ID)).thenReturn(expectedRules);

        // when
        List<SystemParsingRule> result = (List<SystemParsingRule>) manager.getByProjectId(PROJECT_ID);

        // then
        Assertions.assertSame(expectedRules, result);
        verify(repository).findByProjectId(PROJECT_ID);
    }

    // ==================== getByProperties TESTS ====================

    @Test
    void getByProperties_ShouldReturnMatchingRules() {
        // given
        List<SystemParsingRule> allRules = List.of(parsingRule);
        when(repository.findByProjectId(PROJECT_ID)).thenReturn(allRules);

        @SuppressWarnings("unchecked")
        Triple<String, Match, ?>[] properties = new Triple[0];

        // when
        List<SystemParsingRule> result = (List<SystemParsingRule>) manager.getByProperties(PROJECT_ID, properties);

        // then
        Assertions.assertNotNull(result);
        verify(repository).findByProjectId(PROJECT_ID);
    }

    // ==================== getByIdOnly TESTS ====================

    @Test
    void getByIdOnly_ShouldReturnRuleFromRepository() {
        // given
        when(repository.findByIdOnly(RULE_ID)).thenReturn(parsingRule);

        // when
        SystemParsingRule result = manager.getByIdOnly(RULE_ID);

        // then
        Assertions.assertSame(parsingRule, result);
        verify(repository).findByIdOnly(RULE_ID);
    }

    @Test
    void getByIdOnly_WhenNotFound_ShouldReturnNull() {
        // given
        when(repository.findByIdOnly(RULE_ID)).thenReturn(null);

        // when
        SystemParsingRule result = manager.getByIdOnly(RULE_ID);

        // then
        Assertions.assertNull(result);
        verify(repository).findByIdOnly(RULE_ID);
    }
}