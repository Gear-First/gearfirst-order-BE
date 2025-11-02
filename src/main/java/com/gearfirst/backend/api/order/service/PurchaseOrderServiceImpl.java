package com.gearfirst.backend.api.order.service;

import com.gearfirst.backend.api.order.dto.request.PurchaseOrderRequest;
import com.gearfirst.backend.api.order.dto.response.PurchaseOrderResponse;
import com.gearfirst.backend.api.order.dto.response.PurchaseOrderDetailResponse;
import com.gearfirst.backend.api.order.entity.OrderItem;
import com.gearfirst.backend.api.order.entity.PurchaseOrder;
import com.gearfirst.backend.api.order.infra.client.InventoryClient;
import com.gearfirst.backend.api.order.infra.client.dto.OutboundRequest;
import com.gearfirst.backend.api.order.repository.OrderItemRepository;
import com.gearfirst.backend.api.order.repository.PurchaseOrderRepository;
import com.gearfirst.backend.common.enums.OrderStatus;
import com.gearfirst.backend.common.exception.BadRequestException;
import com.gearfirst.backend.common.exception.ConflictException;
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
    public PurchaseOrderResponse createPurchaseOrder(PurchaseOrderRequest request) {
        boolean hasVehicleInfo =
                request.getVehicleModel() != null && !request.getVehicleModel().isBlank() &&
                request.getVehicleNumber() != null && !request.getVehicleNumber().isBlank() &&
                request.getReceiptNum() != null && !request.getReceiptNum().isBlank();
        boolean hasNoVehicleInfo =
                (request.getVehicleModel() == null || request.getVehicleModel().isBlank()) &&
                (request.getVehicleNumber() == null || request.getVehicleNumber().isBlank()) &&
                (request.getReceiptNum() == null || request.getReceiptNum().isBlank());

        if(!(hasVehicleInfo || hasNoVehicleInfo)){
            throw new BadRequestException(ErrorStatus.INVALID_VEHICLE_INFO_EXCEPTION.getMessage());
        }

        if(hasVehicleInfo) {
            if(purchaseOrderRepository.findByReceiptNum(request.getReceiptNum()).isPresent()){
                throw new ConflictException(ErrorStatus.DUPLICATE_RECEIPT_NUM_EXCEPTION.getMessage());
            }
        }
        //발주 엔티티 생성
        PurchaseOrder order = PurchaseOrder.builder()
                .vehicleNumber(request.getVehicleNumber())
                .vehicleModel(request.getVehicleModel())
                .engineerId(request.getEngineerId())
                .branchCode(request.getBranchCode())
                .receiptNum(request.getReceiptNum())
                .build();

        //부품 정보 조회
        List<OrderItem> orderItems = request.getItems().stream()
                .map(i -> {
                    int price = i.getPrice();
                    String name = i.getPartName();
                    String code = i.getPartCode();

                    // OrderItem 생성
                    return new OrderItem(order, i.getPartId(), name, code, price, i.getQuantity());
                })
                .toList();

        // 총금액, 총 건수 계산
        order.calculateTotalPrice(orderItems);
        order.calculateTotalQuantity(orderItems);
        //  저장
        purchaseOrderRepository.save(order);
        orderItemRepository.saveAll(orderItems);

        return PurchaseOrderResponse.from(order, orderItems);
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
    public List<PurchaseOrderDetailResponse> getBranchPurchaseOrders(String branchCode, Long engineerId) {
        List<PurchaseOrder> orders = purchaseOrderRepository.findByBranchCodeAndEngineerIdOrderByRequestDateDesc(branchCode, engineerId);
        return orders.stream()
                .map(order -> {
                    List<OrderItem> items = orderItemRepository.findByPurchaseOrder_Id(order.getId());
                            return PurchaseOrderDetailResponse.from(order, items);
                })
                .toList();

    }

    //대리점 상태 그룹별 조회(준비/ 완료 / 취소)
    @Override
    @Transactional(readOnly = true)
    public List<PurchaseOrderDetailResponse> getBranchPurchaseOrdersByFilter(String branchCode, Long engineerId, String filterType) {
        List<OrderStatus> statusList = switch (filterType.toLowerCase()){
            case "ready" -> List.of(OrderStatus.PENDING, OrderStatus.APPROVED, OrderStatus.SHIPPED);
            case "completed" -> List.of(OrderStatus.COMPLETED,OrderStatus.USED_IN_REPAIR);
            case "cancelled" -> List.of(OrderStatus.REJECTED,OrderStatus.CANCELLED);
            default -> throw new IllegalArgumentException("유효하지 않은 필터 타입입니다. (ready, completed, cancelled 중하나여야 합니다.)");
        };
        List<PurchaseOrder> orders = purchaseOrderRepository.findByBranchCodeAndEngineerIdAndStatusInOrderByRequestDateDesc(branchCode, engineerId, statusList);

        return orders.stream()
                .map(order -> {
                    List<OrderItem> items = orderItemRepository.findByPurchaseOrder_Id(order.getId());
                    return PurchaseOrderDetailResponse.from(order, items);
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
     + 수리 완료 버튼 클릭 시 발주 부품 조회
     + */
    @Transactional(readOnly = true)
    @Override
    public PurchaseOrderResponse getCompleteRepairPartsList(String receiptNum, String vehicleNumber, String branchCode, Long engineerId){
        PurchaseOrder order = findCompletedOrder(receiptNum, vehicleNumber, branchCode, engineerId);
        // 부품 목록 조회
        List<OrderItem> items = getOrderItems(order);

        return PurchaseOrderResponse.from(order, items);
    }
    /**
     * 수리 완료 처리
     */
    @Transactional
    @Override
    public PurchaseOrderResponse completeRepairPartsList(String receiptNum, String vehicleNumber, String branchCode, Long engineerId){
        PurchaseOrder order = findCompletedOrder(receiptNum, vehicleNumber, branchCode, engineerId);
        //상태변경
        order.completeRepair();
        // 부품 목록 조회
        List<OrderItem> items = getOrderItems(order);

        return PurchaseOrderResponse.from(order, items);
    }
    /**
     * 공통: 발주 조회 메서드
     */
    private PurchaseOrder findCompletedOrder(String receiptNum, String vehicleNumber, String branchCode, Long engineerId) {
        return purchaseOrderRepository
                .findByVehicleNumberAndBranchCodeAndEngineerIdAndStatusAndReceiptNum(
                        vehicleNumber, branchCode, engineerId, OrderStatus.COMPLETED, receiptNum
                )
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_ORDER_EXCEPTION.getMessage()));
    }

    /**
     *  공통: 부품 목록 조회 메서드
     */
    private List<OrderItem> getOrderItems(PurchaseOrder order) {
        return orderItemRepository.findByPurchaseOrder_Id(order.getId());
    }

    /**
     * 발주 상세 조회
     */
    @Override
    @Transactional(readOnly = true)
    public PurchaseOrderDetailResponse getPurchaseOrderDetail(Long orderId, String branchCode, Long engineerId) {
        PurchaseOrder order = purchaseOrderRepository.findByIdAndBranchCodeAndEngineerId(orderId,branchCode,engineerId)
                .orElseThrow(()-> new NotFoundException(ErrorStatus.NOT_FOUND_ORDER_EXCEPTION.getMessage()));

        List<OrderItem> items = orderItemRepository.findByPurchaseOrder_Id(orderId);
        return PurchaseOrderDetailResponse.from(order,items);
    }

    /**
     * 대리점에서 발주 취소
     */
    @Override
    public void cancelBranchOrder(Long orderId, String branchCode, Long engineerId){
        PurchaseOrder order = purchaseOrderRepository.findByIdAndBranchCodeAndEngineerId(orderId,branchCode,engineerId)
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
