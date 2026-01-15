package com.ubs.ExpenseManager.usecases.expense.strategies;

import com.ubs.ExpenseManager.entities.department.enums.SpendingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("SpendingValidationFactory Tests")
class SpendingValidationFactoryTest {

    private SpendingValidationFactory factory;
    private DailyValidationStrategy dailyStrategy;
    private MonthlyValidationStrategy monthlyStrategy;

    @BeforeEach
    void setUp() {
        dailyStrategy = new DailyValidationStrategy();
        monthlyStrategy = new MonthlyValidationStrategy();
    }

    @Test
    @DisplayName("Should initialize factory with all strategies")
    void shouldInitializeWithStrategies() {
        List<SpendingValidationStrategy> strategies = Arrays.asList(dailyStrategy, monthlyStrategy);
        factory = new SpendingValidationFactory(strategies);

        assertThat(factory).isNotNull();
    }

    @Test
    @DisplayName("Should return daily strategy for DAILY type")
    void shouldReturnDailyStrategy() {
        List<SpendingValidationStrategy> strategies = Arrays.asList(dailyStrategy, monthlyStrategy);
        factory = new SpendingValidationFactory(strategies);

        SpendingValidationStrategy result = factory.getStrategy(SpendingType.DAILY);

        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(DailyValidationStrategy.class);
        assertThat(result.getType()).isEqualTo(SpendingType.DAILY);
    }

    @Test
    @DisplayName("Should return monthly strategy for MONTHLY type")
    void shouldReturnMonthlyStrategy() {
        List<SpendingValidationStrategy> strategies = Arrays.asList(dailyStrategy, monthlyStrategy);
        factory = new SpendingValidationFactory(strategies);

        SpendingValidationStrategy result = factory.getStrategy(SpendingType.MONTHLY);

        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(MonthlyValidationStrategy.class);
        assertThat(result.getType()).isEqualTo(SpendingType.MONTHLY);
    }

    @Test
    @DisplayName("Should return null for unknown strategy type")
    void shouldReturnNullForUnknownType() {
        List<SpendingValidationStrategy> strategies = Arrays.asList(dailyStrategy);
        factory = new SpendingValidationFactory(strategies);

        SpendingValidationStrategy result = factory.getStrategy(SpendingType.MONTHLY);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should handle empty strategy list")
    void shouldHandleEmptyStrategyList() {
        List<SpendingValidationStrategy> strategies = Collections.emptyList();
        factory = new SpendingValidationFactory(strategies);

        SpendingValidationStrategy dailyResult = factory.getStrategy(SpendingType.DAILY);
        SpendingValidationStrategy monthlyResult = factory.getStrategy(SpendingType.MONTHLY);

        assertThat(dailyResult).isNull();
        assertThat(monthlyResult).isNull();
    }

    @Test
    @DisplayName("Should handle single strategy")
    void shouldHandleSingleStrategy() {
        List<SpendingValidationStrategy> strategies = Arrays.asList(dailyStrategy);
        factory = new SpendingValidationFactory(strategies);

        SpendingValidationStrategy result = factory.getStrategy(SpendingType.DAILY);

        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo(SpendingType.DAILY);
    }

    @Test
    @DisplayName("Should maintain correct strategy mapping")
    void shouldMaintainCorrectMapping() {
        List<SpendingValidationStrategy> strategies = Arrays.asList(dailyStrategy, monthlyStrategy);
        factory = new SpendingValidationFactory(strategies);

        SpendingValidationStrategy daily1 = factory.getStrategy(SpendingType.DAILY);
        SpendingValidationStrategy daily2 = factory.getStrategy(SpendingType.DAILY);
        SpendingValidationStrategy monthly1 = factory.getStrategy(SpendingType.MONTHLY);

        // Should return same instances
        assertThat(daily1).isSameAs(daily2);
        assertThat(daily1).isNotSameAs(monthly1);
    }

    @Test
    @DisplayName("Should work with mock strategies")
    void shouldWorkWithMockStrategies() {
        SpendingValidationStrategy mockStrategy = mock(SpendingValidationStrategy.class);
        when(mockStrategy.getType()).thenReturn(SpendingType.DAILY);

        List<SpendingValidationStrategy> strategies = Arrays.asList(mockStrategy);
        factory = new SpendingValidationFactory(strategies);

        SpendingValidationStrategy result = factory.getStrategy(SpendingType.DAILY);

        assertThat(result).isSameAs(mockStrategy);
    }
}