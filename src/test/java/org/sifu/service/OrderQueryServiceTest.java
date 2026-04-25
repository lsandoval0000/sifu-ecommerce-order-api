package org.sifu.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sifu.dto.OrderDTO;
import org.sifu.dto.OrderItemDTO;
import org.sifu.dto.PageResponse;
import org.sifu.entities.Order;
import org.sifu.entities.OrderItem;
import org.sifu.entities.OrderStatus;
import org.sifu.exceptions.OrderNotFoundException;
import org.sifu.mapper.OrderItemMapper;
import org.sifu.mapper.OrderMapper;
import org.sifu.repository.OrderRepository;
import org.sifu.service.impl.OrderQueryServiceImpl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OrderQueryService implementations
 */
@ExtendWith(MockitoExtension.class)
class OrderQueryServiceTest {

    @Mock
    OrderRepository orderRepository;

    @Mock
    OrderMapper orderMapper;

    @Mock
    OrderItemMapper orderItemMapper;

    @InjectMocks
    OrderQueryServiceImpl orderQueryService;

    private Order testOrder;
    private OrderDTO testOrderDTO;
    private UUID orderId;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        testOrder = new Order();
        testOrder.setId(orderId);
        testOrder.setCustomerName("Test Customer");
        testOrder.setCustomerEmail("test@example.com");
        testOrder.setOrderStatus(OrderStatus.PENDING);
        testOrder.setTotalAmount(BigDecimal.valueOf(100.00));
        testOrder.setOrderItems(new ArrayList<>());

        testOrderDTO = new OrderDTO();
        testOrderDTO.setId(orderId);
        testOrderDTO.setCustomerName("Test Customer");
        testOrderDTO.setCustomerEmail("test@example.com");
        testOrderDTO.setOrderStatus(OrderStatus.PENDING);
        testOrderDTO.setTotalAmount(BigDecimal.valueOf(100.00));
    }

    @Nested
    @DisplayName("findAll method tests")
    class FindAllMethod {

        @Test
        @DisplayName("Returns paginated orders")
        void findAll_withPagination_returnsPageResponse() {
            // Given
            List<Order> orders = List.of(testOrder);
            long total = 1;
            when(orderRepository.countActive()).thenReturn(total);
            when(orderRepository.findAll(0, 20)).thenReturn(orders);
            when(orderMapper.toDTO(testOrder)).thenReturn(testOrderDTO);

            // When
            PageResponse<OrderDTO> result = orderQueryService.findAll(0, 20);

            // Then
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertEquals(0, result.getPage());
            assertEquals(20, result.getSize());
        }

        @Test
        @DisplayName("Returns correct totalPages calculation")
        void findAll_calculatesTotalPagesCorrectly() {
            // Given
            List<Order> orders = List.of(testOrder);
            long total = 25;
            when(orderRepository.countActive()).thenReturn(total);
            when(orderRepository.findAll(0, 10)).thenReturn(orders);
            when(orderMapper.toDTO(testOrder)).thenReturn(testOrderDTO);

            // When
            PageResponse<OrderDTO> result = orderQueryService.findAll(0, 10);

            // Then
            assertEquals(3, result.getTotalPages());
        }

        @Test
        @DisplayName("Handles empty result")
        void findAll_emptyResult_returnsEmptyList() {
            // Given
            List<Order> emptyOrders = new ArrayList<>();
            long total = 0;
            when(orderRepository.countActive()).thenReturn(total);
            when(orderRepository.findAll(0, 20)).thenReturn(emptyOrders);

            // When
            PageResponse<OrderDTO> result = orderQueryService.findAll(0, 20);

            // Then
            assertNotNull(result);
            assertEquals(0, result.getTotalElements());
            assertTrue(result.getContent().isEmpty());
        }

        @Test
        @DisplayName("First page is identified correctly")
        void findAll_firstPage_isFirstTrue() {
            // Given
            List<Order> orders = List.of(testOrder);
            when(orderRepository.countActive()).thenReturn(1L);
            when(orderRepository.findAll(0, 20)).thenReturn(orders);
            when(orderMapper.toDTO(testOrder)).thenReturn(testOrderDTO);

            // When
            PageResponse<OrderDTO> result = orderQueryService.findAll(0, 20);

            // Then
            assertTrue(result.isFirst());
        }

        @Test
        @DisplayName("Last page is identified correctly")
        void findAll_lastPage_isLastTrue() {
            // Given
            List<Order> orders = List.of(testOrder);
            when(orderRepository.countActive()).thenReturn(1L);
            when(orderRepository.findAll(0, 20)).thenReturn(orders);
            when(orderMapper.toDTO(testOrder)).thenReturn(testOrderDTO);

            // When
            PageResponse<OrderDTO> result = orderQueryService.findAll(0, 20);

            // Then
            assertTrue(result.isLast());
        }
    }

    @Nested
    @DisplayName("findById method tests")
    class FindByIdMethod {

        @Test
        @DisplayName("Returns order when found")
        void findById_existingOrder_returnsOrderDTO() {
            // Given
            when(orderRepository.findById(orderId)).thenReturn(testOrder);
            when(orderMapper.toDTO(testOrder)).thenReturn(testOrderDTO);

            // When
            OrderDTO result = orderQueryService.findById(orderId);

            // Then
            assertNotNull(result);
            assertEquals(orderId, result.getId());
            assertEquals("Test Customer", result.getCustomerName());
        }

        @Test
        @DisplayName("Throws OrderNotFoundException when not found")
        void findById_nonExistentOrder_throwsException() {
            UUID nonExistentId = UUID.randomUUID();
            when(orderRepository.findById(nonExistentId)).thenReturn(null);

            assertThrows(OrderNotFoundException.class,
                    () -> orderQueryService.findById(nonExistentId));
        }

        @Test
        @DisplayName("Returns correct order details")
        void findById_returnsCorrectDetails() {
            // Given
            when(orderRepository.findById(orderId)).thenReturn(testOrder);
            when(orderMapper.toDTO(testOrder)).thenReturn(testOrderDTO);

            // When
            OrderDTO result = orderQueryService.findById(orderId);

            // Then
            assertEquals("test@example.com", result.getCustomerEmail());
            assertEquals(OrderStatus.PENDING, result.getOrderStatus());
            assertEquals(BigDecimal.valueOf(100.00), result.getTotalAmount());
        }
    }

    @Nested
    @DisplayName("getItems method tests")
    class GetItemsMethod {

        @Test
        @DisplayName("Returns order items when order exists")
        void getItems_existingOrder_returnsItems() {
            // Given
            OrderItem item = new OrderItem();
            item.setId(UUID.randomUUID());
            item.setQuantity(2);
            item.setUnitPrice(BigDecimal.valueOf(50.00));
            testOrder.setOrderItems(List.of(item));

            OrderItemDTO itemDTO = new OrderItemDTO();
            itemDTO.setId(item.getId());
            itemDTO.setQuantity(2);
            itemDTO.setUnitPrice(BigDecimal.valueOf(50.00));

            when(orderRepository.findById(orderId)).thenReturn(testOrder);
            when(orderItemMapper.toDTO(item)).thenReturn(itemDTO);

            // When
            List<OrderItemDTO> result = orderQueryService.getItems(orderId);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Throws OrderNotFoundException when order not found")
        void getItems_nonExistentOrder_throwsException() {
            UUID nonExistentId = UUID.randomUUID();
            when(orderRepository.findById(nonExistentId)).thenReturn(null);

            assertThrows(OrderNotFoundException.class,
                    () -> orderQueryService.getItems(nonExistentId));
        }

        @Test
        @DisplayName("Returns empty list when order has no items")
        void getItems_orderWithNoItems_returnsEmptyList() {
            // Given
            testOrder.setOrderItems(new ArrayList<>());
            when(orderRepository.findById(orderId)).thenReturn(testOrder);

            // When
            List<OrderItemDTO> result = orderQueryService.getItems(orderId);

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }
}