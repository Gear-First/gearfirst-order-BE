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
import com.gearfirst.backend.api.order.repository.OrderItemRepository;
import com.gearfirst.backend.api.order.repository.PurchaseOrderRepository;
import com.gearfirst.backend.common.exception.NotFoundException;
import com.gearfirst.backend.common.response.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional              //내부의 모든 public 메서드에 자동으로 트랜잭션 적용
public class PurchaseOrderServiceImpl implements PurchaseOrderService{
    private final BranchRepository branchRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final OrderItemRepository orderItemRepository;
    private final InventoryClient inventoryClient;

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
    @Transactional(readOnly = true)     //트랜잭션 읽기 전용-엔티티 수정 감지(Dirty checking)를 비활성화
    public List<PurchaseOrderResponse> getAllPurchaseOrders(Long branchId) {
        List<PurchaseOrder> orders =  purchaseOrderRepository.findByBranch_Id(branchId);
        return orders.stream().map(order ->
                new PurchaseOrderResponse(
                        order.getId(),
                        order.getBranch().getBranchName(),
                        null,   //리스트 조회에서는 상세 품목 생량
                        order.getTotalPrice(),
                        order.getStatus().name(),
                        order.getCreatedAt()
                )
        ).toList();
    }
    @Override
    @Transactional(readOnly = true)     //트랜잭션 읽기 전용-엔티티 수정 감지(Dirty checking)를 비활성화
    public List<PurchaseOrderResponse> getPurchaseOrdersByStatus(Long branchId, String status) {
        List<PurchaseOrder> orders = purchaseOrderRepository.findByBranch_Id(branchId).stream().filter(o-> o.getStatus().name().equalsIgnoreCase(status))
                .toList();
        return orders.stream().map(order ->
                new PurchaseOrderResponse(
                        order.getId(),
                        order.getBranch().getBranchName(),
                        null,   //리스트 조회에서는 상세 품목 생량
                        order.getTotalPrice(),
                        order.getStatus().name(),
                        order.getCreatedAt()
                )
        ).toList();
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
}
