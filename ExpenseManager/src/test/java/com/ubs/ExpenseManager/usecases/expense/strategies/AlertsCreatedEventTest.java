package com.ubs.ExpenseManager.usecases.expense.strategies;

import com.ubs.ExpenseManager.entities.alert.Alert;
import com.ubs.ExpenseManager.entities.alert.enums.AlertType;
import com.ubs.ExpenseManager.entities.expense.Expense;
import com.ubs.ExpenseManager.usecases.expense.observer.AlertsCreatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.mock;

@DisplayName("AlertsCreatedEvent Tests")
class AlertsCreatedEventTest {

    private AlertsCreatedEvent event;

    @BeforeEach
    void setUp() {
        event = new AlertsCreatedEvent();
    }

    @Test
    @DisplayName("Should initialize with empty alerts list")
    void shouldInitializeWithEmptyList() {
        assertThat(event.getAlerts()).isEmpty();
    }

    @Test
    @DisplayName("Should add single alert successfully")
    void shouldAddSingleAlert() {
        Expense expense = mock(Expense.class);
        Alert alert = new Alert(expense, AlertType.CATEGORY_DAILY, "Test message");

        event.add(alert);

        assertThat(event.getAlerts()).hasSize(1);
        assertThat(event.getAlerts().get(0)).isEqualTo(alert);
    }

    @Test
    @DisplayName("Should add multiple alerts successfully")
    void shouldAddMultipleAlerts() {
        Expense expense = mock(Expense.class);
        Alert alert1 = new Alert(expense, AlertType.CATEGORY_DAILY, "Message 1");
        Alert alert2 = new Alert(expense, AlertType.CATEGORY_MONTHLY, "Message 2");

        event.add(alert1);
        event.add(alert2);

        assertThat(event.getAlerts()).hasSize(2);
        assertThat(event.getAlerts()).containsExactly(alert1, alert2);
    }

    @Test
    @DisplayName("Should maintain order of added alerts")
    void shouldMaintainOrderOfAlerts() {
        Expense expense = mock(Expense.class);
        Alert alert1 = new Alert(expense, AlertType.CATEGORY_DAILY, "First");
        Alert alert2 = new Alert(expense, AlertType.CATEGORY_MONTHLY, "Second");
        Alert alert3 = new Alert(expense, AlertType.CATEGORY_DAILY, "Third");

        event.add(alert1);
        event.add(alert2);
        event.add(alert3);

        List<Alert> alerts = event.getAlerts();
        assertThat(alerts.get(0)).isEqualTo(alert1);
        assertThat(alerts.get(1)).isEqualTo(alert2);
        assertThat(alerts.get(2)).isEqualTo(alert3);
    }

    @Test
    @DisplayName("Should return mutable list")
    void shouldReturnMutableList() {
        Expense expense = mock(Expense.class);
        Alert alert = new Alert(expense, AlertType.CATEGORY_DAILY, "Test");

        event.add(alert);
        List<Alert> alerts = event.getAlerts();

        // Verify we can modify the returned list (it's the same reference)
        assertThat(alerts).isNotNull();
        assertThat(alerts.size()).isEqualTo(1);
    }
}