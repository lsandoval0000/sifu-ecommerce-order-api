package org.sifu.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sifu.entities.OrderStatus;
import org.sifu.exceptions.InvalidStatusTransitionException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for OrderStateMachine
 */
class OrderStateMachineTest {

    private OrderStateMachine stateMachine;

    @BeforeEach
    void setUp() {
        stateMachine = new OrderStateMachine();
    }

    @Nested
    @DisplayName("isValidTransition - PENDING state transitions")
    class PendingTransitions {

        @Test
        @DisplayName("PENDING -> CONFIRMED is valid")
        void pendingToConfirmed_isValid() {
            assertTrue(stateMachine.isValidTransition(OrderStatus.PENDING, OrderStatus.CONFIRMED));
        }

        @Test
        @DisplayName("PENDING -> CANCELLED is valid")
        void pendingToCancelled_isValid() {
            assertTrue(stateMachine.isValidTransition(OrderStatus.PENDING, OrderStatus.CANCELLED));
        }

        @Test
        @DisplayName("PENDING -> DELETED is valid")
        void pendingToDeleted_isValid() {
            assertTrue(stateMachine.isValidTransition(OrderStatus.PENDING, OrderStatus.DELETED));
        }

        @Test
        @DisplayName("PENDING -> PROCESSING is invalid")
        void pendingToProcessing_isInvalid() {
            assertFalse(stateMachine.isValidTransition(OrderStatus.PENDING, OrderStatus.PROCESSING));
        }

        @Test
        @DisplayName("PENDING -> SHIPPED is invalid")
        void pendingToShipped_isInvalid() {
            assertFalse(stateMachine.isValidTransition(OrderStatus.PENDING, OrderStatus.SHIPPED));
        }

        @Test
        @DisplayName("PENDING -> DELIVERED is invalid")
        void pendingToDelivered_isInvalid() {
            assertFalse(stateMachine.isValidTransition(OrderStatus.PENDING, OrderStatus.DELIVERED));
        }

        @Test
        @DisplayName("PENDING -> REFUNDED is invalid")
        void pendingToRefunded_isInvalid() {
            assertFalse(stateMachine.isValidTransition(OrderStatus.PENDING, OrderStatus.REFUNDED));
        }
    }

    @Nested
    @DisplayName("isValidTransition - CONFIRMED state transitions")
    class ConfirmedTransitions {

        @Test
        @DisplayName("CONFIRMED -> PROCESSING is valid")
        void confirmedToProcessing_isValid() {
            assertTrue(stateMachine.isValidTransition(OrderStatus.CONFIRMED, OrderStatus.PROCESSING));
        }

        @Test
        @DisplayName("CONFIRMED -> CANCELLED is valid")
        void confirmedToCancelled_isValid() {
            assertTrue(stateMachine.isValidTransition(OrderStatus.CONFIRMED, OrderStatus.CANCELLED));
        }

        @Test
        @DisplayName("CONFIRMED -> DELETED is valid")
        void confirmedToDeleted_isValid() {
            assertTrue(stateMachine.isValidTransition(OrderStatus.CONFIRMED, OrderStatus.DELETED));
        }

        @Test
        @DisplayName("CONFIRMED -> PENDING is invalid")
        void confirmedToPending_isInvalid() {
            assertFalse(stateMachine.isValidTransition(OrderStatus.CONFIRMED, OrderStatus.PENDING));
        }

        @Test
        @DisplayName("CONFIRMED -> SHIPPED is invalid")
        void confirmedToShipped_isInvalid() {
            assertFalse(stateMachine.isValidTransition(OrderStatus.CONFIRMED, OrderStatus.SHIPPED));
        }
    }

    @Nested
    @DisplayName("isValidTransition - PROCESSING state transitions")
    class ProcessingTransitions {

        @Test
        @DisplayName("PROCESSING -> SHIPPED is valid")
        void processingToShipped_isValid() {
            assertTrue(stateMachine.isValidTransition(OrderStatus.PROCESSING, OrderStatus.SHIPPED));
        }

        @Test
        @DisplayName("PROCESSING -> DELETED is valid")
        void processingToDeleted_isValid() {
            assertTrue(stateMachine.isValidTransition(OrderStatus.PROCESSING, OrderStatus.DELETED));
        }

        @Test
        @DisplayName("PROCESSING -> CANCELLED is invalid")
        void processingToCancelled_isInvalid() {
            assertFalse(stateMachine.isValidTransition(OrderStatus.PROCESSING, OrderStatus.CANCELLED));
        }

        @Test
        @DisplayName("PROCESSING -> PENDING is invalid")
        void processingToPending_isInvalid() {
            assertFalse(stateMachine.isValidTransition(OrderStatus.PROCESSING, OrderStatus.PENDING));
        }
    }

    @Nested
    @DisplayName("isValidTransition - SHIPPED state transitions")
    class ShippedTransitions {

        @Test
        @DisplayName("SHIPPED -> DELIVERED is valid")
        void shippedToDelivered_isValid() {
            assertTrue(stateMachine.isValidTransition(OrderStatus.SHIPPED, OrderStatus.DELIVERED));
        }

        @Test
        @DisplayName("SHIPPED -> DELETED is valid")
        void shippedToDeleted_isValid() {
            assertTrue(stateMachine.isValidTransition(OrderStatus.SHIPPED, OrderStatus.DELETED));
        }

        @Test
        @DisplayName("SHIPPED -> CANCELLED is invalid")
        void shippedToCancelled_isInvalid() {
            assertFalse(stateMachine.isValidTransition(OrderStatus.SHIPPED, OrderStatus.CANCELLED));
        }
    }

    @Nested
    @DisplayName("isValidTransition - DELIVERED state transitions")
    class DeliveredTransitions {

        @Test
        @DisplayName("DELIVERED -> REFUNDED is valid")
        void deliveredToRefunded_isValid() {
            assertTrue(stateMachine.isValidTransition(OrderStatus.DELIVERED, OrderStatus.REFUNDED));
        }

        @Test
        @DisplayName("DELIVERED -> DELETED is valid")
        void deliveredToDeleted_isValid() {
            assertTrue(stateMachine.isValidTransition(OrderStatus.DELIVERED, OrderStatus.DELETED));
        }

        @Test
        @DisplayName("DELIVERED -> CANCELLED is invalid")
        void deliveredToCancelled_isInvalid() {
            assertFalse(stateMachine.isValidTransition(OrderStatus.DELIVERED, OrderStatus.CANCELLED));
        }
    }

    @Nested
    @DisplayName("isValidTransition - CANCELLED state transitions")
    class CancelledTransitions {

        @Test
        @DisplayName("CANCELLED -> DELETED is valid")
        void cancelledToDeleted_isValid() {
            assertTrue(stateMachine.isValidTransition(OrderStatus.CANCELLED, OrderStatus.DELETED));
        }

        @Test
        @DisplayName("CANCELLED -> any other state is invalid")
        void cancelledToOther_isInvalid() {
            assertFalse(stateMachine.isValidTransition(OrderStatus.CANCELLED, OrderStatus.PENDING));
            assertFalse(stateMachine.isValidTransition(OrderStatus.CANCELLED, OrderStatus.CONFIRMED));
            assertFalse(stateMachine.isValidTransition(OrderStatus.CANCELLED, OrderStatus.SHIPPED));
        }
    }

    @Nested
    @DisplayName("isValidTransition - REFUNDED state transitions")
    class RefundedTransitions {

        @Test
        @DisplayName("REFUNDED -> DELETED is valid")
        void refundedToDeleted_isValid() {
            assertTrue(stateMachine.isValidTransition(OrderStatus.REFUNDED, OrderStatus.DELETED));
        }

        @Test
        @DisplayName("REFUNDED -> any other state is invalid")
        void refundedToOther_isInvalid() {
            assertFalse(stateMachine.isValidTransition(OrderStatus.REFUNDED, OrderStatus.PENDING));
            assertFalse(stateMachine.isValidTransition(OrderStatus.REFUNDED, OrderStatus.DELIVERED));
        }
    }

    @Nested
    @DisplayName("isValidTransition - DELETED state transitions")
    class DeletedTransitions {

        @Test
        @DisplayName("DELETED -> any state is invalid")
        void deletedToAny_isInvalid() {
            for (OrderStatus status : OrderStatus.values()) {
                assertFalse(stateMachine.isValidTransition(OrderStatus.DELETED, status),
                        "DELETED -> " + status + " should be invalid");
            }
        }
    }

    @Nested
    @DisplayName("isValidTransition - Null handling")
    class NullHandling {

        @Test
        @DisplayName("null -> any state returns false")
        void nullFrom_returnsFalse() {
            assertFalse(stateMachine.isValidTransition(null, OrderStatus.PENDING));
            assertFalse(stateMachine.isValidTransition(null, OrderStatus.CONFIRMED));
        }

        @Test
        @DisplayName("any state -> null returns false")
        void nullTo_returnsFalse() {
            assertFalse(stateMachine.isValidTransition(OrderStatus.PENDING, null));
            assertFalse(stateMachine.isValidTransition(OrderStatus.CONFIRMED, null));
        }

        @Test
        @DisplayName("null -> null returns false")
        void nullToNull_returnsFalse() {
            assertFalse(stateMachine.isValidTransition(null, null));
        }
    }

    @Nested
    @DisplayName("transition method")
    class TransitionMethod {

        @Test
        @DisplayName("Valid transition returns new status")
        void validTransition_returnsNewStatus() {
            OrderStatus result = stateMachine.transition(OrderStatus.PENDING, OrderStatus.CONFIRMED);
            assertEquals(OrderStatus.CONFIRMED, result);
        }

        @Test
        @DisplayName("Invalid transition throws exception")
        void invalidTransition_throwsException() {
            InvalidStatusTransitionException exception = assertThrows(
                    InvalidStatusTransitionException.class,
                    () -> stateMachine.transition(OrderStatus.PENDING, OrderStatus.PROCESSING)
            );
            assertNotNull(exception.getMessage());
            assertTrue(exception.getMessage().contains("PENDING"));
            assertTrue(exception.getMessage().contains("PROCESSING"));
        }

        @Test
        @DisplayName("null from throws exception")
        void nullFrom_throwsException() {
            assertThrows(InvalidStatusTransitionException.class,
                    () -> stateMachine.transition(null, OrderStatus.CONFIRMED));
        }

        @Test
        @DisplayName("null to throws exception")
        void nullTo_throwsException() {
            assertThrows(InvalidStatusTransitionException.class,
                    () -> stateMachine.transition(OrderStatus.PENDING, null));
        }
    }

    @Nested
    @DisplayName("canCancel method")
    class CanCancelMethod {

        @Test
        @DisplayName("PENDING can be cancelled")
        void pending_canCancel() {
            assertTrue(stateMachine.canCancel(OrderStatus.PENDING));
        }

        @Test
        @DisplayName("CONFIRMED can be cancelled")
        void confirmed_canCancel() {
            assertFalse(stateMachine.canCancel(OrderStatus.CONFIRMED));
        }

        @Test
        @DisplayName("PROCESSING cannot be cancelled")
        void processing_cannotCancel() {
            assertFalse(stateMachine.canCancel(OrderStatus.PROCESSING));
        }

        @Test
        @DisplayName("SHIPPED cannot be cancelled")
        void shipped_cannotCancel() {
            assertFalse(stateMachine.canCancel(OrderStatus.SHIPPED));
        }

        @Test
        @DisplayName("DELIVERED cannot be cancelled")
        void delivered_cannotCancel() {
            assertFalse(stateMachine.canCancel(OrderStatus.DELIVERED));
        }

        @Test
        @DisplayName("CANCELLED cannot be cancelled")
        void cancelled_cannotCancel() {
            assertFalse(stateMachine.canCancel(OrderStatus.CANCELLED));
        }
    }

    @Nested
    @DisplayName("canConfirm method")
    class CanConfirmMethod {

        @Test
        @DisplayName("PENDING can be confirmed")
        void pending_canConfirm() {
            assertTrue(stateMachine.canConfirm(OrderStatus.PENDING));
        }

        @Test
        @DisplayName("CONFIRMED cannot be confirmed")
        void confirmed_cannotConfirm() {
            assertFalse(stateMachine.canConfirm(OrderStatus.CONFIRMED));
        }

        @Test
        @DisplayName("PROCESSING cannot be confirmed")
        void processing_cannotConfirm() {
            assertFalse(stateMachine.canConfirm(OrderStatus.PROCESSING));
        }
    }

    @Nested
    @DisplayName("canAddItems method")
    class CanAddItemsMethod {

        @Test
        @DisplayName("PENDING can add items")
        void pending_canAddItems() {
            assertTrue(stateMachine.canAddItems(OrderStatus.PENDING));
        }

        @Test
        @DisplayName("CONFIRMED cannot add items")
        void confirmed_cannotAddItems() {
            assertFalse(stateMachine.canAddItems(OrderStatus.CONFIRMED));
        }

        @Test
        @DisplayName("CANCELLED cannot add items")
        void cancelled_cannotAddItems() {
            assertFalse(stateMachine.canAddItems(OrderStatus.CANCELLED));
        }
    }

    @Nested
    @DisplayName("canModifyOrder method")
    class CanModifyOrderMethod {

        @Test
        @DisplayName("PENDING can be modified")
        void pending_canModify() {
            assertTrue(stateMachine.canModifyOrder(OrderStatus.PENDING));
        }

        @Test
        @DisplayName("CONFIRMED can be modified")
        void confirmed_canModify() {
            assertTrue(stateMachine.canModifyOrder(OrderStatus.CONFIRMED));
        }

        @Test
        @DisplayName("PROCESSING can be modified")
        void processing_canModify() {
            assertTrue(stateMachine.canModifyOrder(OrderStatus.PROCESSING));
        }

        @Test
        @DisplayName("SHIPPED cannot be modified")
        void shipped_cannotModify() {
            assertFalse(stateMachine.canModifyOrder(OrderStatus.SHIPPED));
        }

        @Test
        @DisplayName("DELIVERED cannot be modified")
        void delivered_cannotModify() {
            assertFalse(stateMachine.canModifyOrder(OrderStatus.DELIVERED));
        }

        @Test
        @DisplayName("CANCELLED cannot be modified")
        void cancelled_cannotModify() {
            assertFalse(stateMachine.canModifyOrder(OrderStatus.CANCELLED));
        }

        @Test
        @DisplayName("REFUNDED cannot be modified")
        void refunded_cannotModify() {
            assertFalse(stateMachine.canModifyOrder(OrderStatus.REFUNDED));
        }

        @Test
        @DisplayName("DELETED cannot be modified")
        void deleted_cannotModify() {
            assertFalse(stateMachine.canModifyOrder(OrderStatus.DELETED));
        }

        @Test
        @DisplayName("null returns false")
        void null_returnsFalse() {
            assertFalse(stateMachine.canModifyOrder(null));
        }
    }

    @Nested
    @DisplayName("canDelete method")
    class CanDeleteMethod {

        @Test
        @DisplayName("PENDING can be deleted")
        void pending_canDelete() {
            assertTrue(stateMachine.canDelete(OrderStatus.PENDING));
        }

        @Test
        @DisplayName("CONFIRMED can be deleted")
        void confirmed_canDelete() {
            assertFalse(stateMachine.canDelete(OrderStatus.CONFIRMED));
        }

        @Test
        @DisplayName("SHIPPED can be deleted")
        void shipped_canDelete() {
            assertFalse(stateMachine.canDelete(OrderStatus.SHIPPED));
        }

        @Test
        @DisplayName("DELIVERED can be deleted")
        void delivered_canDelete() {
            assertFalse(stateMachine.canDelete(OrderStatus.DELIVERED));
        }

        @Test
        @DisplayName("DELETED cannot be deleted")
        void deleted_cannotDelete() {
            assertFalse(stateMachine.canDelete(OrderStatus.DELETED));
        }
    }
}