package com.gearfirst.backend.api.order.service;

import com.gearfirst.backend.api.branch.entity.Branch;
import com.gearfirst.backend.api.branch.repository.BranchRepository;
import com.gearfirst.backend.api.order.dto.OrderItemResponse;
import com.gearfirst.backend.api.order.dto.PurchaseOrderRequest;
import com.gearfirst.backend.api.order.dto.PurchaseOrderResponse;
import com.gearfirst.backend.api.order.entity.OrderItem;
import com.gearfirst.backend.api.order.entity.PurchaseOrder;
import com.gearfirst.backend.api.order.infra.client.InventoryClient;
import com.gearfirst.backend.api.order.infra.dto.InventoryResponse;
import com.gearfirst.backend.api.order.infra.dto.OutboundRequest;
import com.gearfirst.backend.api.order.repository.OrderItemRepository;
import com.gearfirst.backend.api.order.repository.PurchaseOrderRepository;
import com.gearfirst.backend.common.enums.OrderStatus;
import com.gearfirst.backend.common.exception.NotFoundException;
import com.gearfirst.backend.common.response.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional              //내부의 모든 public 메서드에 자동으로 트랜잭션 적용
public class PurchaseOrderServiceImpl implements PurchaseOrderService{
    private final BranchRepository branchRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final OrderItemRepository orderItemRepository;
    private final InventoryClient inventoryClient; // Feign Client (inventory-service 연결)

    /**
     * 발주 요청 생성
     */
    @Override
    public PurchaseOrderResponse createPurchaseOrder(PurchaseOrderRequest request) {
        //대리점 존재여부 확인
        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(()-> new NotFoundException(ErrorStatus.NOT_FOUND_BRANCH_EXCEPTION.getMessage()));
        //발주 엔티티 생성
        PurchaseOrder order = new PurchaseOrder(branch);
        purchaseOrderRepository.save(order);

        //품목 생성
        List<OrderItem> orderItems = request.getItems().stream()
                .map(i -> {
                    //재고 데이터 조회
                    InventoryResponse inventory = inventoryClient.getInventoryById(i.getInventoryId());
                    //단가/이름을 db 에서 가져온 값으로 대체
                    int price = inventory.getPrice();
                    String name = inventory.getInventoryName();

                    // OrderItem 생성
                    return new OrderItem(order, i.getInventoryId(), name, price, i.getQuantity());
                })
                .toList();

            // 총금액 계산 및 저장
            int totalPrice = orderItems.stream()
                    .mapToInt(OrderItem::getTotalPrice)
                    .sum();
            order.updateTotalPrice(totalPrice);

            //  주문/아이템 저장
            orderItems.forEach(orderItemRepository::save);

            //  응답 DTO 변환
            List<OrderItemResponse> itemResponses = orderItems.stream()
                    .map(item -> new OrderItemResponse(
                            item.getInventoryName(),
                            item.getQuantity(),
                            item.getPrice(),
                            item.getTotalPrice()
                    ))
                    .toList();

            return new PurchaseOrderResponse(
                    order.getId(),
                    branch.getBranchName(),
                    itemResponses,
                    totalPrice,
                    order.getStatus().name(),
                    order.getCreatedAt()
            );
   }

    /**
     * 발주 목록 조회
     */
    @Override
    @Transactional(readOnly = true)
    public List<PurchaseOrderResponse> getAllPurchaseOrders(Long branchId) {

        // branchId로 모든 OrderItem + PurchaseOrder + Branch를 한 번에 가져옴
        List<OrderItem> orderItems = orderItemRepository.findAllItemsByBranchId(branchId);

        // 주문 ID 기준으로 그룹화
        Map<Long, List<OrderItem>> orderGroup = orderItems.stream()
                .collect(Collectors.groupingBy(OrderItem::getId));

        // 주문 ID로 PurchaseOrder를 찾아서 DTO 매핑
        return orderGroup.entrySet().stream()
                .map(entry -> {
                    Long orderId = entry.getKey();
                    List<OrderItem> items = entry.getValue();

                    //주문 조회
                    PurchaseOrder order = purchaseOrderRepository.findById(orderId)
                            .orElseThrow(() -> new NotFoundException("해당 주문을 찾을 수 없습니다."));

                    // 품목 이름 & 수량만 담은 DTO 리스트
                    List<OrderItemResponse> itemResponses = items.stream()
                            .map(i -> new OrderItemResponse(
                                    i.getInventoryName(),
                                    i.getQuantity(),
                                    0, // 가격은 목록에서는 생략
                                    0
                            ))
                            .toList();

                    // PurchaseOrderResponse 생성
                    return new PurchaseOrderResponse(
                            order.getId(),
                            order.getBranch().getBranchName(),
                            itemResponses,
                            order.getTotalPrice(),
                            order.getStatus().name(),
                            order.getCreatedAt()
                    );
                })
                .toList();
    }

    /**
     * 발주 상태별 조회
     */
    @Override
    @Transactional(readOnly = true)
    public List<PurchaseOrderResponse> getPurchaseOrdersByStatus(Long branchId, String status) {

        // 상태 문자열을 enum으로 변환 (없으면 null)
        OrderStatus orderStatus = null;
        if (status != null && !status.isBlank()) {
            orderStatus = OrderStatus.valueOf(status.toUpperCase());
        }

        // branchId + status 기준 발주 조회
        List<PurchaseOrder> orders = purchaseOrderRepository.findByBranchAndStatus(branchId, orderStatus);

        // 모든 주문 ID 수집
        List<Long> orderIds = orders.stream()
                .map(PurchaseOrder::getId)
                .toList();

        // 주문별 품목 조회
        List<OrderItem> orderItems = orderItemRepository.findByIdIn(orderIds);

        // orderId별로 품목 그룹화
        Map<Long, List<OrderItem>> groupedItems = orderItems.stream()
                .collect(Collectors.groupingBy(OrderItem::getId));

        //  DTO 변환
        return orders.stream()
                .map(order -> {
                    List<OrderItem> items = groupedItems.getOrDefault(order.getId(), List.of());

                    List<OrderItemResponse> itemResponses = items.stream()
                            .map(i -> new OrderItemResponse(
                                    i.getInventoryName(),
                                    i.getQuantity(),
                                    0, // 목록에서는 단가 제외
                                    0
                            ))
                            .toList();

                    return new PurchaseOrderResponse(
                            order.getId(),
                            order.getBranch().getBranchName(),
                            itemResponses,
                            order.getTotalPrice(),
                            order.getStatus().name(),
                            order.getCreatedAt()
                    );
                })
                .toList();
    }


    /**
     * 발주 상세 조회
     */
    @Override
    @Transactional(readOnly = true)
    public PurchaseOrderResponse getPurchaseOrderDetail(Long orderId) {
        PurchaseOrder order = purchaseOrderRepository.findById(orderId)
                .orElseThrow(()-> new NotFoundException(ErrorStatus.NOT_FOUND_ORDER_EXCEPTION.getMessage()));

        List<OrderItem> items = orderItemRepository.findByPurchaseOrder_Id(orderId);
        List<OrderItemResponse> itemResponses = items.stream()
                .map(item -> new OrderItemResponse(item.getInventoryName(),item.getQuantity(), item.getPrice(), item.getTotalPrice()))
                .toList();
        return new PurchaseOrderResponse(order.getId(), order.getBranch().getBranchName(),itemResponses, order.getTotalPrice(), order.getStatus().name(), order.getCreatedAt());
    }

    /**
     * 발주 승인
     */
    @Override
    public void approveOrder(Long orderId, Long warehouseId) {
        //발주서 조회
        PurchaseOrder order = purchaseOrderRepository.findById(orderId)
                .orElseThrow(()-> new NotFoundException(ErrorStatus.NOT_FOUND_ORDER_EXCEPTION.getMessage()));
        //상태 변경
        order.approve();
        //발주 품목 조회
        List<OrderItem> items = orderItemRepository.findByPurchaseOrder_Id(orderId);
        //출고 명령 생성 요청(Inventory 서비스로 전송)
        OutboundRequest request = OutboundRequest.from(order,items,warehouseId);
        //Inventory 서비스로 전달
        inventoryClient.createOutboundOrder(request);
    }

    /**
     * 발주 반려
     */
    @Override
    public void rejectOrder(Long orderId) {
        //발주서 조회
        PurchaseOrder order = purchaseOrderRepository.findById(orderId)
                .orElseThrow(()-> new NotFoundException(ErrorStatus.NOT_FOUND_ORDER_EXCEPTION.getMessage()));
        //상태변경
        order.reject();
    }
}
