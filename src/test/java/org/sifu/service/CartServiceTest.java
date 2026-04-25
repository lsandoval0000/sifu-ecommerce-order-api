package org.sifu.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.sifu.clients.FinancialApiRestClient;
import org.sifu.clients.model.ItfModel;
import org.sifu.domain.OrderStateMachine;
import org.sifu.dto.AddItemRequest;
import org.sifu.dto.OrderItemDTO;
import org.sifu.entities.Order;
import org.sifu.entities.OrderItem;
import org.sifu.entities.OrderStatus;
import org.sifu.entities.Product;
import org.sifu.exceptions.BusinessRuleViolationException;
import org.sifu.exceptions.ErrorMessages;
import org.sifu.exceptions.OrderItemNotFoundException;
import org.sifu.exceptions.OrderNotFoundException;
import org.sifu.exceptions.ProductNotFoundException;
import org.sifu.mapper.OrderItemMapper;
import org.sifu.repository.OrderItemRepository;
import org.sifu.repository.OrderRepository;
import org.sifu.repository.ProductRepository;
import org.sifu.service.impl.CartServiceImpl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CartServiceImpl
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CartServiceTest {

    @Mock
    OrderRepository orderRepository;

    @Mock
    OrderItemRepository orderItemRepository;

    @Mock
    ProductRepository productRepository;

    @Mock
    OrderItemMapper orderItemMapper;

    @Mock
    OrderStateMachine stateMachine;

    @Mock
    FinancialApiRestClient financialApiRestClient;

    @InjectMocks
    CartServiceImpl cartService;

    private Order testOrder;
    private Product testProduct;
    private OrderItem testOrderItem;
    private OrderItemDTO testOrderItemDTO;
    private AddItemRequest addItemRequest;
    private UUID orderId;
    private UUID productId;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        productId = UUID.randomUUID();
        
        ItfModel itfModel = new ItfModel(100, "0.00");
        when(financialApiRestClient.getItf(anyInt())).thenReturn(itfModel);

        testOrder = new Order();
        testOrder.setId(orderId);
        testOrder.setOrderStatus(OrderStatus.PENDING);
        testOrder.setOrderItems(new ArrayList<>());
        testOrder.setTotalAmount(BigDecimal.ZERO);

        testProduct = new Product();
        testProduct.setId(productId);
        testProduct.setName("Test Product");
        testProduct.setPrice(BigDecimal.valueOf(99.99));
        testProduct.setIsEnabled((short) 1);

        testOrderItem = new OrderItem();
        testOrderItem.setId(UUID.randomUUID());
        testOrderItem.setProduct(testProduct);
        testOrderItem.setQuantity(2);
        testOrderItem.setUnitPrice(testProduct.getPrice());
        testOrderItem.setOrder(testOrder);

        testOrderItemDTO = new OrderItemDTO();
        testOrderItemDTO.setId(testOrderItem.getId());
        testOrderItemDTO.setProductId(productId.toString());
        testOrderItemDTO.setQuantity(2);
        testOrderItemDTO.setUnitPrice(BigDecimal.valueOf(99.99));

        addItemRequest = new AddItemRequest(productId, 2);
    }

    @Nested
    @DisplayName("getItems method tests")
    class GetItemsMethod {

        @Test
        @DisplayName("Returns order items when order exists")
        void getItems_existingOrder_returnsItems() {
            // Given
            testOrder.setOrderItems(List.of(testOrderItem));
            when(orderRepository.findById(orderId)).thenReturn(testOrder);
            when(orderItemMapper.toDTO(testOrderItem)).thenReturn(testOrderItemDTO);

            // When
            List<OrderItemDTO> result = cartService.getItems(orderId);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(testOrderItemDTO.getId(), result.get(0).getId());
        }

        @Test
        @DisplayName("Throws OrderNotFoundException when order does not exist")
        void getItems_nonExistentOrder_throwsException() {
            // Given
            when(orderRepository.findById(orderId)).thenReturn(null);

            // When/Then
            assertThrows(OrderNotFoundException.class,
                    () -> cartService.getItems(orderId));
        }

        @Test
        @DisplayName("Returns empty list when order has no items")
        void getItems_emptyOrder_returnsEmptyList() {
            // Given
            testOrder.setOrderItems(new ArrayList<>());
            when(orderRepository.findById(orderId)).thenReturn(testOrder);

            // When
            List<OrderItemDTO> result = cartService.getItems(orderId);

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("addItem method tests")
    class AddItemMethod {

        @Test
        @DisplayName("Adds item to pending order successfully")
        void addItem_pendingOrder_addsItem() {
            // Given
            when(orderRepository.findById(orderId)).thenReturn(testOrder);
            when(stateMachine.canAddItems(OrderStatus.PENDING)).thenReturn(true);
            when(productRepository.findById(productId)).thenReturn(testProduct);
            when(orderItemMapper.toDTO(any(OrderItem.class))).thenReturn(testOrderItemDTO);

            // When
            OrderItemDTO result = cartService.addItem(orderId, addItemRequest);

            // Then
            assertNotNull(result);
            verify(orderItemRepository).persist(any(OrderItem.class));
            verify(orderRepository).persist(testOrder);
        }

        @Test
        @DisplayName("Throws OrderNotFoundException when order does not exist")
        void addItem_nonExistentOrder_throwsException() {
            // Given
            when(orderRepository.findById(orderId)).thenReturn(null);

            // When/Then
            assertThrows(OrderNotFoundException.class,
                    () -> cartService.addItem(orderId, addItemRequest));
        }

        @Test
        @DisplayName("Throws BusinessRuleViolationException when order is not pending")
        void addItem_confirmedOrder_throwsException() {
            // Given
            testOrder.setOrderStatus(OrderStatus.CONFIRMED);
            when(orderRepository.findById(orderId)).thenReturn(testOrder);
            when(stateMachine.canAddItems(OrderStatus.CONFIRMED)).thenReturn(false);

            // When/Then
            BusinessRuleViolationException ex = assertThrows(BusinessRuleViolationException.class,
                    () -> cartService.addItem(orderId, addItemRequest));
            assertEquals(ErrorMessages.CANNOT_ADD_ITEMS, ex.getMessage());
        }

        @Test
        @DisplayName("Throws ProductNotFoundException when product does not exist")
        void addItem_nonExistentProduct_throwsException() {
            // Given
            UUID nonExistentProductId = UUID.randomUUID();
            AddItemRequest request = new AddItemRequest(nonExistentProductId, 1);
            when(orderRepository.findById(orderId)).thenReturn(testOrder);
            when(stateMachine.canAddItems(OrderStatus.PENDING)).thenReturn(true);
            when(productRepository.findById(nonExistentProductId)).thenReturn(null);

            // When/Then
            assertThrows(ProductNotFoundException.class,
                    () -> cartService.addItem(orderId, request));
        }

        @Test
        @DisplayName("Throws ProductNotFoundException when product is disabled")
        void addItem_disabledProduct_throwsException() {
            // Given
            testProduct.setIsEnabled((short) 0);
            when(orderRepository.findById(orderId)).thenReturn(testOrder);
            when(stateMachine.canAddItems(OrderStatus.PENDING)).thenReturn(true);
            when(productRepository.findById(productId)).thenReturn(testProduct);

            // When/Then
            assertThrows(ProductNotFoundException.class,
                    () -> cartService.addItem(orderId, addItemRequest));
        }

        @Test
        @DisplayName("Initializes order items list when null")
        void addItem_nullItemsList_initializesList() {
            // Given
            testOrder.setOrderItems(null);
            when(orderRepository.findById(orderId)).thenReturn(testOrder);
            when(stateMachine.canAddItems(OrderStatus.PENDING)).thenReturn(true);
            when(productRepository.findById(productId)).thenReturn(testProduct);
            when(orderItemMapper.toDTO(any(OrderItem.class))).thenReturn(testOrderItemDTO);

            // When
            cartService.addItem(orderId, addItemRequest);

            // Then
            assertNotNull(testOrder.getOrderItems());
            assertFalse(testOrder.getOrderItems().isEmpty());
        }

        @Test
        @DisplayName("Recalculates total amount after adding item")
        void addItem_recalculatesTotal() {
            // Given
            testOrder.setOrderItems(new ArrayList<>());
            when(orderRepository.findById(orderId)).thenReturn(testOrder);
            when(stateMachine.canAddItems(OrderStatus.PENDING)).thenReturn(true);
            when(productRepository.findById(productId)).thenReturn(testProduct);
            when(orderItemMapper.toDTO(any(OrderItem.class))).thenReturn(testOrderItemDTO);

            // When
            cartService.addItem(orderId, addItemRequest);

            // Then
            assertEquals(BigDecimal.valueOf(199.98), testOrder.getTotalAmount());
        }
    }

    @Nested
    @DisplayName("removeItem method tests")
    class RemoveItemMethod {

        @Test
        @DisplayName("Removes item from order successfully")
        void removeItem_existingItem_removesItem() {
            // Given
            testOrder.setOrderItems(new ArrayList<>());
            testOrder.getOrderItems().add(testOrderItem);
            testOrder.setTotalAmount(BigDecimal.valueOf(199.98));
            when(orderRepository.findById(orderId)).thenReturn(testOrder);
            when(orderItemRepository.findById(testOrderItem.getId())).thenReturn(testOrderItem);

            // When
            cartService.removeItem(orderId, testOrderItem.getId());

            // Then
            assertTrue(testOrder.getOrderItems().isEmpty());
            assertEquals(new BigDecimal("0.00"), testOrder.getTotalAmount());
            verify(orderItemRepository).delete(testOrderItem);
            verify(orderRepository).persist(testOrder);
        }

        @Test
        @DisplayName("Throws OrderNotFoundException when order does not exist")
        void removeItem_nonExistentOrder_throwsException() {
            // Given
            when(orderRepository.findById(orderId)).thenReturn(null);

            // When/Then
            assertThrows(OrderNotFoundException.class,
                    () -> cartService.removeItem(orderId, UUID.randomUUID()));
        }

        @Test
        @DisplayName("Throws OrderItemNotFoundException when item does not exist")
        void removeItem_nonExistentItem_throwsException() {
            // Given
            UUID nonExistentItemId = UUID.randomUUID();
            when(orderRepository.findById(orderId)).thenReturn(testOrder);
            when(orderItemRepository.findById(nonExistentItemId)).thenReturn(null);

            // When/Then
            assertThrows(OrderItemNotFoundException.class,
                    () -> cartService.removeItem(orderId, nonExistentItemId));
        }

        @Test
        @DisplayName("Throws OrderItemNotFoundException when item belongs to different order")
        void removeItem_itemFromDifferentOrder_throwsException() {
            // Given
            UUID differentOrderId = UUID.randomUUID();
            OrderItem itemFromDifferentOrder = new OrderItem();
            itemFromDifferentOrder.setId(testOrderItem.getId());
            Order differentOrder = new Order();
            differentOrder.setId(differentOrderId);
            itemFromDifferentOrder.setOrder(differentOrder);

            when(orderRepository.findById(orderId)).thenReturn(testOrder);
            when(orderItemRepository.findById(testOrderItem.getId())).thenReturn(itemFromDifferentOrder);

            // When/Then
            assertThrows(OrderItemNotFoundException.class,
                    () -> cartService.removeItem(orderId, testOrderItem.getId()));
        }

        @Test
        @DisplayName("Recalculates total amount after removing item")
        void removeItem_recalculatesTotal() {
            // Given
            OrderItem item2 = new OrderItem();
            item2.setId(UUID.randomUUID());
            item2.setQuantity(3);
            item2.setUnitPrice(BigDecimal.valueOf(50.00));
            item2.setOrder(testOrder);

            testOrder.setOrderItems(new ArrayList<>());
            testOrder.getOrderItems().add(testOrderItem);
            testOrder.getOrderItems().add(item2);
            testOrder.setTotalAmount(BigDecimal.valueOf(299.98));

            when(orderRepository.findById(orderId)).thenReturn(testOrder);
            when(orderItemRepository.findById(testOrderItem.getId())).thenReturn(testOrderItem);

            // When
            cartService.removeItem(orderId, testOrderItem.getId());

            // Then
            assertEquals(new BigDecimal("150.00"), testOrder.getTotalAmount());
        }

        @Test
        @DisplayName("Sets total to zero when removing last item")
        void removeItem_lastItem_setsTotalToZero() {
            // Given
            testOrder.setOrderItems(new ArrayList<>());
            testOrder.getOrderItems().add(testOrderItem);
            testOrder.setTotalAmount(BigDecimal.valueOf(199.98));
            when(orderRepository.findById(orderId)).thenReturn(testOrder);
            when(orderItemRepository.findById(testOrderItem.getId())).thenReturn(testOrderItem);

            // When
            cartService.removeItem(orderId, testOrderItem.getId());

            // Then
            assertEquals(new BigDecimal("0.00"), testOrder.getTotalAmount());
        }
    }
}