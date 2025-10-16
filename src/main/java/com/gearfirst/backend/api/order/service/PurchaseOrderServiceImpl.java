package com.gearfirst.backend.api.order.service;

import com.gearfirst.backend.api.order.dto.request.PurchaseOrderRequest;
import com.gearfirst.backend.api.order.dto.response.PurchaseOrderResponse;
import com.gearfirst.backend.api.order.dto.response.ReceiptResponse;
import com.gearfirst.backend.api.order.entity.OrderItem;
import com.gearfirst.backend.api.order.entity.PurchaseOrder;
import com.gearfirst.backend.api.order.infra.client.InventoryClient;
import com.gearfirst.backend.api.order.infra.client.RepairClient;
import com.gearfirst.backend.api.order.infra.client.dto.InventoryResponse;
import com.gearfirst.backend.api.order.infra.client.dto.OutboundRequest;
import com.gearfirst.backend.api.order.dto.response.RepairResponse;
import com.gearfirst.backend.api.order.infra.client.dto.ReceiptCarResponse;
import com.gearfirst.backend.api.order.repository.OrderItemRepository;
import com.gearfirst.backend.api.order.repository.PurchaseOrderRepository;
import com.gearfirst.backend.common.enums.OrderStatus;
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

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final OrderItemRepository orderItemRepository;
    private final InventoryClient inventoryClient; // Feign Client (inventory-service 연결)
    private final RepairClient repairClient;
    //private final UserClient userClient;

    /**
     * 발주 요청 시 엔지니어가 접수한 차량 리스트 조회
     */
    @Override
    public List<ReceiptCarResponse> findReceiptsByEngineer(Long engineerId){
        List<ReceiptCarResponse> repairs = repairClient.getRepairsByEngineer(engineerId);

        return repairs.stream()
                .map(r -> new ReceiptCarResponse(r.getReceiptNumber(),r.getVehicleNumber(),r.getVehicleModel(),r.getStatus()))
                .toList();
    }
    /**
     * 발주 요청 시 엔지니어가 접수한 차량 검색("12가","가3456","3456","12")
     */
    @Override
    public List<ReceiptCarResponse> searchReceiptsByEngineer(Long engineerId, String keyword){
        List<ReceiptCarResponse> repairs = repairClient.searchRepairsByEngineer(engineerId, keyword);

        return repairs.stream()
                .map(r -> new ReceiptCarResponse(r.getReceiptNumber(),r.getVehicleNumber(),r.getVehicleModel(),r.getStatus()))
                .toList();
    }

    /**
     * 차량에 맞는 부품 검색
     */
    @Override
    public List<InventoryResponse> findInventoriesByCarModel(Long carModelId, String keyword){
        List<InventoryResponse> inventories = inventoryClient.getInventoriesByCarModel(carModelId, keyword);

        return inventories.stream()
                .map(i -> new InventoryResponse(i.getInventoryId(),i.getInventoryName(),i.getInventoryCode(),i.getPrice()))
                .toList();
    }

    /**
     * 발주 요청 생성
     */
    @Override
    public void createPurchaseOrder(PurchaseOrderRequest request) {
        //발주 엔티티 생성
        PurchaseOrder order = PurchaseOrder.builder()
                .vehicleNumber(request.getVehicleNumber())
                .engineerId(request.getEngineerId())
                .branchId(request.getBranchId())
                .build();

        //부품 정보 조회
        List<OrderItem> orderItems = request.getItems().stream()
                .map(i -> {
                    //재고 데이터 조회
                    InventoryResponse inventory = inventoryClient.getInventoryById(i.getInventoryId());
                    //단가/이름을 db 에서 가져온 값으로 대체
                    int price = inventory.getPrice();
                    String name = inventory.getInventoryName();
                    String code = inventory.getInventoryCode();

                    // OrderItem 생성
                    return new OrderItem(order, i.getInventoryId(), name, code, price, i.getQuantity());
                })
                .toList();

            // 총금액 계산
            order.calculateTotalPrice(orderItems);

            //  저장
            purchaseOrderRepository.save(order);
            orderItemRepository.saveAll(orderItems);

            //  응답 DTO 변환
   }

    //본사용 전체 조회(모든 대리점)
    @Override
    @Transactional(readOnly = true)
    public List<PurchaseOrderResponse> getAllPurchaseOrders(){
        List<PurchaseOrder> orders = purchaseOrderRepository.findAllByOrderByRequestDateDesc();

        return orders.stream()
                .map(order -> {
                    List<OrderItem> items = orderItemRepository.findByPurchaseOrder_Id(order.getId());
                    return PurchaseOrderResponse.from(order, items);
                })
                .toList();
    }

    //엔지니어용 발주 목록 전체 조회
    @Override
    @Transactional(readOnly = true)
    public List<PurchaseOrderResponse> getBranchPurchaseOrders(Long branchId, Long engineerId) {
        List<PurchaseOrder> orders = purchaseOrderRepository.findByBranchIdAndEngineerIdOrderByRequestDateDesc(branchId, engineerId);
        return orders.stream()
                .map(order -> {
                    List<OrderItem> items = orderItemRepository.findByPurchaseOrder_Id(order.getId());
                            return PurchaseOrderResponse.from(order, items);
                })
                .toList();

    }

    //대리점 상태 그룹별 조회(준비/ 완료 / 취소)
    @Override
    @Transactional(readOnly = true)
    public List<PurchaseOrderResponse> getBranchPurchaseOrdersByFilter(Long branchId, String filterType) {
        List<OrderStatus> statusList = switch (filterType.toLowerCase()){
            case "ready" -> List.of(OrderStatus.PENDING, OrderStatus.APPROVED, OrderStatus.SHIPPED);
            case "completed" -> List.of(OrderStatus.COMPLETED);
            case "cancelled" -> List.of(OrderStatus.REJECTED,OrderStatus.CANCELLED);
            default -> throw new IllegalArgumentException("유효하지 않은 필터 타입입니다. (ready, completed, cancelled 중하나여야 합니다.)");
        };
        List<PurchaseOrder> orders = purchaseOrderRepository.findByBranchIdAndStatusInOrderByRequestDateDesc(branchId, statusList);

        return orders.stream()
                .map(order -> {
                    List<OrderItem> items = orderItemRepository.findByPurchaseOrder_Id(order.getId());
                    return PurchaseOrderResponse.from(order, items);
                })
                .toList();
    }

    //본사 상태별 조회
    @Override
    public List<PurchaseOrderResponse> getHeadPurchaseOrdersByStatus(String status){
        OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
        List<PurchaseOrder> orders =
                purchaseOrderRepository.findByStatusOrderByRequestDateDesc(orderStatus);

        return orders.stream()
                .map(order -> {
                    List<OrderItem> items = orderItemRepository.findByPurchaseOrder_Id(order.getId());
                    return PurchaseOrderResponse.from(order, items);
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
        return PurchaseOrderResponse.from(order,items);
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
