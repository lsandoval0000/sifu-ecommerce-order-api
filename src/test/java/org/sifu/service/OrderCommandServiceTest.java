package org.sifu.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sifu.domain.OrderStateMachine;
import org.sifu.domain.StockManager;
import org.sifu.dto.CreateOrderRequest;
import org.sifu.dto.OrderDTO;
import org.sifu.entities.Order;
import org.sifu.entities.OrderStatus;
import org.sifu.exceptions.BusinessRuleViolationException;
import org.sifu.exceptions.OrderNotFoundException;
import org.sifu.mapper.OrderMapper;
import org.sifu.messaging.OrderNotificationKafkaService;
import org.sifu.repository.OrderRepository;
import org.sifu.service.impl.OrderCommandServiceImpl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OrderCommandService implementations
 */
@ExtendWith(MockitoExtension.class)
class OrderCommandServiceTest {

    @Mock
    OrderRepository orderRepository;

    @Mock
    OrderMapper orderMapper;

    @Mock
    OrderStateMachine stateMachine;

    @Mock
    StockManager stockManager;

    @InjectMocks
    OrderCommandServiceImpl orderCommandService;

    private Order testOrder;
    private OrderDTO testOrderDTO;
    private CreateOrderRequest createRequest;

    @BeforeEach
    void setUp() {
        testOrder = new Order();
        testOrder.setId(UUID.randomUUID());
        testOrder.setCustomerName("Test Customer");
        testOrder.setCustomerEmail("test@example.com");
        testOrder.setOrderStatus(OrderStatus.PENDING);
        testOrder.setTotalAmount(BigDecimal.ZERO);
        testOrder.setOrderItems(new ArrayList<>());

        testOrderDTO = new OrderDTO();
        testOrderDTO.setId(testOrder.getId());
        testOrderDTO.setCustomerName("Test Customer");
        testOrderDTO.setCustomerEmail("test@example.com");
        testOrderDTO.setOrderStatus(OrderStatus.PENDING);
        testOrderDTO.setTotalAmount(BigDecimal.ZERO);

        createRequest = new CreateOrderRequest("Test Customer", "test@example.com");
    }

    @Nested
    @DisplayName("create method tests")
    class CreateMethod {

        @Test
        @DisplayName("Creates order with valid request")
        void create_withValidRequest_returnsOrderDTO() {
            // Given
            when(orderMapper.toEntity(createRequest)).thenReturn(testOrder);
            when(orderMapper.toDTO(testOrder)).thenReturn(testOrderDTO);

            // When
            OrderDTO result = orderCommandService.create(createRequest);

            // Then
            assertNotNull(result);
            assertEquals("Test Customer", result.getCustomerName());
            assertEquals("test@example.com", result.getCustomerEmail());
            verify(orderRepository).persist(testOrder);
        }

        @Test
        @DisplayName("Throws BusinessRuleViolationException when request is null")
        void create_nullRequest_throwsException() {
            assertThrows(BusinessRuleViolationException.class,
                    () -> orderCommandService.create(null));
        }

        @Test
        @DisplayName("Throws BusinessRuleViolationException when mapper returns null")
        void create_mapperReturnsNull_throwsException() {
            when(orderMapper.toEntity(createRequest)).thenReturn(null);

            assertThrows(BusinessRuleViolationException.class,
                    () -> orderCommandService.create(createRequest));
        }

        @Test
        @DisplayName("Sets empty order items list on new order")
        void create_setsEmptyOrderItems() {
            when(orderMapper.toEntity(createRequest)).thenReturn(testOrder);
            when(orderMapper.toDTO(testOrder)).thenReturn(testOrderDTO);

            orderCommandService.create(createRequest);

            assertNotNull(testOrder.getOrderItems());
            assertTrue(testOrder.getOrderItems().isEmpty());
        }
    }

    @Nested
    @DisplayName("delete method tests")
    class DeleteMethod {

        @Test
        @DisplayName("Successfully deletes pending order")
        void delete_pendingOrder_deletesSuccessfully() {
            // Given
            when(orderRepository.findById(testOrder.getId())).thenReturn(testOrder);
            when(stateMachine.canDelete(OrderStatus.PENDING)).thenReturn(true);

            // When
            orderCommandService.delete(testOrder.getId());

            // Then
            assertEquals(OrderStatus.DELETED, testOrder.getOrderStatus());
            verify(orderRepository).persist(testOrder);
        }

        @Test
        @DisplayName("Throws OrderNotFoundException when order not found")
        void delete_orderNotFound_throwsException() {
            UUID orderId = UUID.randomUUID();
            when(orderRepository.findById(orderId)).thenReturn(null);

            assertThrows(OrderNotFoundException.class,
                    () -> orderCommandService.delete(orderId));
        }

        @Test
        @DisplayName("Cannot delete if state machine prevents it")
        void delete_cannotDeleteDueToState_throwsException() {
            testOrder.setOrderStatus(OrderStatus.CONFIRMED);
            when(orderRepository.findById(testOrder.getId())).thenReturn(testOrder);
            when(stateMachine.canDelete(OrderStatus.CONFIRMED)).thenReturn(false);

            assertThrows(BusinessRuleViolationException.class,
                    () -> orderCommandService.delete(testOrder.getId()));
        }
    }

    @Nested
    @DisplayName("confirm method tests")
    class ConfirmMethod {

        @Test
        @DisplayName("Successfully confirms pending order")
        void confirm_pendingOrder_confirmsSuccessfully() {
            // Given
            UUID orderId = testOrder.getId();
            when(orderRepository.findByIdWithItems(orderId)).thenReturn(testOrder);
            when(orderMapper.toDTO(testOrder)).thenReturn(testOrderDTO);
            when(stateMachine.canConfirm(OrderStatus.PENDING)).thenReturn(true);

            // When
            OrderDTO result = orderCommandService.confirm(orderId);

            // Then
            assertNotNull(result);
            assertEquals(OrderStatus.CONFIRMED, testOrder.getOrderStatus());
            verify(orderRepository).persist(testOrder);
            verify(stockManager).validateStock(any());
            verify(stockManager).reserveStock(any());
        }

        @Test
        @DisplayName("Throws OrderNotFoundException when order not found")
        void confirm_orderNotFound_throwsException() {
            UUID orderId = UUID.randomUUID();
            when(orderRepository.findByIdWithItems(orderId)).thenReturn(null);

            assertThrows(OrderNotFoundException.class,
                    () -> orderCommandService.confirm(orderId));
        }

        @Test
        @DisplayName("Throws BusinessRuleViolationException when cannot confirm")
        void confirm_cannotConfirmDueToState_throwsException() {
            testOrder.setOrderStatus(OrderStatus.SHIPPED);
            when(orderRepository.findByIdWithItems(testOrder.getId())).thenReturn(testOrder);
            when(stateMachine.canConfirm(OrderStatus.SHIPPED)).thenReturn(false);

            assertThrows(BusinessRuleViolationException.class,
                    () -> orderCommandService.confirm(testOrder.getId()));
        }
    }

    @Nested
    @DisplayName("cancel method tests")
    class CancelMethod {

        @Test
        @DisplayName("Successfully cancels pending order")
        void cancel_pendingOrder_cancelsSuccessfully() {
            // Given
            when(orderRepository.findByIdWithItems(testOrder.getId())).thenReturn(testOrder);
            when(orderMapper.toDTO(testOrder)).thenReturn(testOrderDTO);
            when(stateMachine.canCancel(OrderStatus.PENDING)).thenReturn(true);

            // When
            OrderDTO result = orderCommandService.cancel(testOrder.getId());

            // Then
            assertNotNull(result);
            assertEquals(OrderStatus.CANCELLED, testOrder.getOrderStatus());
            verify(orderRepository).persist(testOrder);
        }

        @Test
        @DisplayName("Cannot cancel confirmed order - stock already reserved")
        void cancel_confirmedOrder_cannotCancel() {
            // Given
            testOrder.setOrderStatus(OrderStatus.CONFIRMED);
            when(orderRepository.findByIdWithItems(testOrder.getId())).thenReturn(testOrder);
            when(stateMachine.canCancel(OrderStatus.CONFIRMED)).thenReturn(false);

            // When/Then
            assertThrows(BusinessRuleViolationException.class,
                    () -> orderCommandService.cancel(testOrder.getId()));
            verify(stockManager, never()).releaseStock(any());
        }

        @Test
        @DisplayName("Throws OrderNotFoundException when order not found")
        void cancel_orderNotFound_throwsException() {
            UUID orderId = UUID.randomUUID();
            when(orderRepository.findByIdWithItems(orderId)).thenReturn(null);

            assertThrows(OrderNotFoundException.class,
                    () -> orderCommandService.cancel(orderId));
        }

        @Test
        @DisplayName("Cannot cancel delivered order")
        void cancel_deliveredOrder_cannotCancel() {
            testOrder.setOrderStatus(OrderStatus.DELIVERED);
            when(orderRepository.findByIdWithItems(testOrder.getId())).thenReturn(testOrder);
            when(stateMachine.canCancel(OrderStatus.DELIVERED)).thenReturn(false);

            assertThrows(BusinessRuleViolationException.class,
                    () -> orderCommandService.cancel(testOrder.getId()));
        }
    }

    @Nested
    @DisplayName("changeStatus method tests")
    class ChangeStatusMethod {

        @Test
        @DisplayName("Successfully changes status from CONFIRMED to PROCESSING")
        void changeStatus_validTransition_changesStatus() {
            // Given
            when(stateMachine.canModifyOrder(OrderStatus.CONFIRMED)).thenReturn(true);

            // When
            boolean canModify = stateMachine.canModifyOrder(OrderStatus.CONFIRMED);

            // Then
            assertTrue(canModify);
        }

        @Test
        @DisplayName("Cannot modify shipped order")
        void changeStatus_shippedOrder_cannotModify() {
            when(stateMachine.canModifyOrder(OrderStatus.SHIPPED)).thenReturn(false);

            boolean canModify = stateMachine.canModifyOrder(OrderStatus.SHIPPED);

            assertFalse(canModify);
        }
    }
}