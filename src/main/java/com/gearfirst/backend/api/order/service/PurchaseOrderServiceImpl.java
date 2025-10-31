package com.gearfirst.backend.api.order.service;

import com.gearfirst.backend.api.order.dto.request.PurchaseOrderRequest;
import com.gearfirst.backend.api.order.dto.response.PurchaseOrderResponse;
import com.gearfirst.backend.api.order.dto.response.RepairPartResponse;
import com.gearfirst.backend.api.order.entity.OrderItem;
import com.gearfirst.backend.api.order.entity.PurchaseOrder;
import com.gearfirst.backend.api.order.infra.client.InventoryClient;
import com.gearfirst.backend.api.order.infra.client.dto.OutboundRequest;
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


    /**
     * 발주 요청 생성
     */
    @Override
    public void createPurchaseOrder(PurchaseOrderRequest request) {
        if(purchaseOrderRepository.findByReceiptNum(request.getReceiptNum()).isPresent()){
            throw new NotFoundException(ErrorStatus.DUPLICATE_RECEIPT_NUM_EXCEPTION.getMessage());
        }
        //발주 엔티티 생성
        PurchaseOrder order = PurchaseOrder.builder()
                .vehicleNumber(request.getVehicleNumber())
                .vehicleModel(request.getVehicleModel())
                .engineerId(request.getEngineerId())
                .branchId(request.getBranchId())
                .receiptNum(request.getReceiptNum())
                .build();

        //부품 정보 조회
        List<OrderItem> orderItems = request.getItems().stream()
                .map(i -> {
                    int price = i.getPrice(); //가격정보 받아오게 되면 수정 필요
                    String name = i.getPartName();
                    String code = i.getPartCode();

                    // OrderItem 생성
                    return new OrderItem(order, i.getPartId(), name, code, price, i.getQuantity());
                })
                .toList();

        // 총금액 계산
        order.calculateTotalPrice(orderItems);
        //  저장
        purchaseOrderRepository.save(order);
        orderItemRepository.saveAll(orderItems);

   }

    /**
     * TODO:본사용 전체 조회(모든 대리점)
     */
//    @Override
//    @Transactional(readOnly = true)
//    public List<HeadquarterPurchaseOrderResponse> getAllPurchaseOrders(){
//        List<PurchaseOrder> orders = purchaseOrderRepository.findAllByOrderByRequestDateDesc();
//
//        return orders.stream()
//                .map(order -> {
//                    List<OrderItem> items = orderItemRepository.findByPurchaseOrder_Id(order.getId());
//                    return HeadquarterPurchaseOrderResponse.from(order, items);
//                })
//                .toList();
//    }

    //엔지니어용 발주 목록 전체 조회
    //TODO: 날짜 필터링 필요 예) 최근 3개월 발주 내역, 올해 완료된 주문
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
    public List<PurchaseOrderResponse> getBranchPurchaseOrdersByFilter(Long branchId, Long engineerId, String filterType) {
        List<OrderStatus> statusList = switch (filterType.toLowerCase()){
            case "ready" -> List.of(OrderStatus.PENDING, OrderStatus.APPROVED, OrderStatus.SHIPPED);
            case "completed" -> List.of(OrderStatus.COMPLETED,OrderStatus.USED_IN_REPAIR);
            case "cancelled" -> List.of(OrderStatus.REJECTED,OrderStatus.CANCELLED);
            default -> throw new IllegalArgumentException("유효하지 않은 필터 타입입니다. (ready, completed, cancelled 중하나여야 합니다.)");
        };
        List<PurchaseOrder> orders = purchaseOrderRepository.findByBranchIdAndEngineerIdAndStatusInOrderByRequestDateDesc(branchId, engineerId, statusList);

        return orders.stream()
                .map(order -> {
                    List<OrderItem> items = orderItemRepository.findByPurchaseOrder_Id(order.getId());
                    return PurchaseOrderResponse.from(order, items);
                })
                .toList();
    }

    /**
     * TODO: 본사 상태별 조회
     */
//    @Override
//    public List<HeadquarterPurchaseOrderResponse> getHeadPurchaseOrdersByStatus(String status){
//        OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
//        List<PurchaseOrder> orders =
//                purchaseOrderRepository.findByStatusOrderByRequestDateDesc(orderStatus);
//
//        return orders.stream()
//                .map(order -> {
//                    List<OrderItem> items = orderItemRepository.findByPurchaseOrder_Id(order.getId());
//                    return HeadquarterPurchaseOrderResponse.from(order, items);
//                })
//                .toList();
//    }

    /**
     + 수리 완료 처리 및 발주 부품 조회
     + 이 메서드는 주문 상태를 USED_IN_REPAIR로 변경합니다.
     + */
    @Transactional
    @Override
    public List<RepairPartResponse> completeRepairAndGetParts(String receiptNum, String vehicleNumber, Long branchId, Long engineerId){
        PurchaseOrder order = purchaseOrderRepository.findByVehicleNumberAndBranchIdAndEngineerIdAndStatusAndReceiptNum(vehicleNumber, branchId, engineerId, OrderStatus.COMPLETED, receiptNum)
                .orElseThrow(()-> new NotFoundException(ErrorStatus.NOT_FOUND_ORDER_EXCEPTION.getMessage()));

        //상태변경
        order.completeRepair();
        // 부품 목록 조회
        List<OrderItem> items = orderItemRepository.findByPurchaseOrder_Id(order.getId());

        //  응답 변환
        List<RepairPartResponse> partResponses = items.stream()
                .map(i -> new RepairPartResponse(i.getPartName(), i.getPartCode(), i.getQuantity(), i.getPrice()))
                .toList();
        return partResponses;
    }

    /**
     * 발주 상세 조회
     */
    @Override
    @Transactional(readOnly = true)
    public PurchaseOrderResponse getPurchaseOrderDetail(Long orderId,Long branchId, Long engineerId) {
        PurchaseOrder order = purchaseOrderRepository.findByIdAndBranchIdAndEngineerId(orderId,branchId,engineerId)
                .orElseThrow(()-> new NotFoundException(ErrorStatus.NOT_FOUND_ORDER_EXCEPTION.getMessage()));

        List<OrderItem> items = orderItemRepository.findByPurchaseOrder_Id(orderId);
        return PurchaseOrderResponse.from(order,items);
    }

    /**
     * 대리점에서 발주 취소
     */
    @Override
    public void cancelBranchOrder(Long orderId, Long branchId, Long engineerId){
        PurchaseOrder order = purchaseOrderRepository.findByIdAndBranchIdAndEngineerId(orderId,branchId,engineerId)
                .orElseThrow(()-> new NotFoundException(ErrorStatus.NOT_FOUND_ORDER_EXCEPTION.getMessage()));
        //상태 변경
        order.cancel();
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
