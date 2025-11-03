package com.gearfirst.backend.api.order.service;

import com.gearfirst.backend.api.order.dto.request.PurchaseOrderRequest;
import com.gearfirst.backend.api.order.dto.response.HeadPurchaseOrderDetailResponse;
import com.gearfirst.backend.api.order.dto.response.HeadPurchaseOrderResponse;
import com.gearfirst.backend.api.order.dto.response.PurchaseOrderResponse;
import com.gearfirst.backend.api.order.dto.response.PurchaseOrderDetailResponse;
import com.gearfirst.backend.api.order.entity.OrderItem;
import com.gearfirst.backend.api.order.entity.PurchaseOrder;
import com.gearfirst.backend.api.order.infra.client.WarehouseClient;
import com.gearfirst.backend.api.order.repository.OrderItemRepository;
import com.gearfirst.backend.api.order.repository.PurchaseOrderQueryRepository;
import com.gearfirst.backend.api.order.repository.PurchaseOrderRepository;
import com.gearfirst.backend.common.dto.response.PageResponse;
import com.gearfirst.backend.common.enums.OrderStatus;
import com.gearfirst.backend.common.exception.ConflictException;
import com.gearfirst.backend.common.exception.NotFoundException;
import com.gearfirst.backend.common.response.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional              //내부의 모든 public 메서드에 자동으로 트랜잭션 적용
public class PurchaseOrderServiceImpl implements PurchaseOrderService{

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final OrderItemRepository orderItemRepository;
    private final WarehouseClient warehouseClient; // Feign Client (warehouse-service 연결)
    private final PurchaseOrderQueryRepository purchaseOrderQueryRepository;


    /**
     * 발주 요청 생성
     */
    @Override
    public PurchaseOrderResponse createPurchaseOrder(PurchaseOrderRequest request) {
         //차량 정보 검증
        validateVehicleInfo(request);
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
   //검증 로직을 별도 메서드로 분리
   private void validateVehicleInfo(PurchaseOrderRequest request){
       boolean hasVehicleInfo =
               request.getVehicleModel() != null && !request.getVehicleModel().isBlank() &&
                       request.getVehicleNumber() != null && !request.getVehicleNumber().isBlank() &&
                       request.getReceiptNum() != null && !request.getReceiptNum().isBlank();
       boolean hasNoVehicleInfo =
               (request.getVehicleModel() == null || request.getVehicleModel().isBlank()) &&
                       (request.getVehicleNumber() == null || request.getVehicleNumber().isBlank()) &&
                       (request.getReceiptNum() == null || request.getReceiptNum().isBlank());

       if(!(hasVehicleInfo || hasNoVehicleInfo)){
           throw new ConflictException(ErrorStatus.INVALID_VEHICLE_INFO_EXCEPTION.getMessage());
       }
       if(hasVehicleInfo) {
           if(purchaseOrderRepository.findByReceiptNum(request.getReceiptNum()).isPresent()){
               throw new ConflictException(ErrorStatus.DUPLICATE_RECEIPT_NUM_EXCEPTION.getMessage());
           }
       }
   }
    /**
     * 본사용 발주 승인 대기 상태 전체 조회(모든 대리점)
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<HeadPurchaseOrderResponse> getPendingOrders(
            LocalDate startDate, LocalDate endDate,
            String branchCode, String partName,
            Pageable pageable
    ) {
        Page<PurchaseOrder> page = purchaseOrderQueryRepository.searchByStatus(
                startDate, endDate, branchCode, partName, OrderStatus.PENDING, pageable);
        return mapToPageResponse(page);
    }
    /**
     * 본사용 승인 대기 상태를 제외한 발주 전체 조회(모든 대리점)
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<HeadPurchaseOrderResponse> getOtherOrders(
            LocalDate startDate, LocalDate endDate,
            String branchCode, String partName,
            Pageable pageable
    ) {
        Page<PurchaseOrder> page = purchaseOrderQueryRepository.searchByStatus(
                startDate, endDate, branchCode, partName, null, pageable);
        return mapToPageResponse(page);
    }
    // 공통 변환
    private PageResponse<HeadPurchaseOrderResponse> mapToPageResponse(Page<PurchaseOrder> page) {
        List<HeadPurchaseOrderResponse> content = page.getContent().stream()
                .map(HeadPurchaseOrderResponse::from)
                .toList();

        Page<HeadPurchaseOrderResponse> dtoPage =
                new PageImpl<>(content, page.getPageable(), page.getTotalElements());

        return new PageResponse<>(dtoPage);
    }
    /**
     * 본사용 발주내역 상세보기
     */
    @Override
    @Transactional(readOnly = true)
    public HeadPurchaseOrderDetailResponse getPurchaseOrderDetail(Long orderId) {
        PurchaseOrder order = purchaseOrderRepository.findById(orderId)
                .orElseThrow(()-> new NotFoundException(ErrorStatus.NOT_FOUND_ORDER_EXCEPTION.getMessage()));
        List<OrderItem> items = orderItemRepository.findByPurchaseOrder_Id(orderId);
        return HeadPurchaseOrderDetailResponse.from(order, items);
    }

    /**
     * 본사용 발주 승인 대기 상태 전체 조회(모든 대리점)
     * 발주 내역을 조회할 때마다 orderItem도 조회하므로 N+1 문제가 발생할 수 있음-> in 쿼리로 최적화
     */
//    @Override
//    @Transactional(readOnly = true)
//    public PageResponse<HeadPurchaseOrderResponse> searchPurchaseOrders(
//            LocalDate startDate, LocalDate endDate,
//            String branchCode, String partName,
//            Pageable pageable
//    ) {
//        Page<PurchaseOrder> page = purchaseOrderQueryRepository.searchByStatus(
//                startDate, endDate,
//                branchCode, partName,
//                pageable
//        );
//        List<Long> orderIds = page.getContent().stream()
//                .map(PurchaseOrder::getId)
//                .toList();
//        //OrderItem을 한번의 In 쿼리로 모두 조회
//        List<OrderItem> orderItems = orderItemRepository.findByPurchaseOrderIdIn(orderIds);
//
//        Map<Long, List<OrderItem>> itemMap = orderItems.stream()
//                .collect(Collectors.groupingBy(item -> item.getPurchaseOrder().getId()));
//        List<HeadPurchaseOrderResponse> content = page.getContent().stream()
//                .map(order -> {
//                    List<OrderItem> items = itemMap.getOrDefault(order.getId(),List.of());
//                    return HeadPurchaseOrderResponse.from(order, items);
//                })
//                .toList();
//
//        Page<HeadPurchaseOrderResponse> dtoPage = new PageImpl<>(content, pageable, page.getTotalElements());
//        return new PageResponse<>(dtoPage);
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
//    @Override
//    @Transactional
//    public void approveOrder(Long orderId,String note) {
//        //발주서 조회
//        PurchaseOrder order = purchaseOrderRepository.findById(orderId)
//                .orElseThrow(()-> new NotFoundException(ErrorStatus.NOT_FOUND_ORDER_EXCEPTION.getMessage()));
//        String code = order.getBranchCode();
//        //발주 품목 조회
//        List<OrderItem> items = orderItemRepository.findByPurchaseOrder_Id(orderId);
//        order.createShipmentCommand();
//        try {
//            //창고지정
//            order.assignWarehouse(code);
//            //비고 저장
//            order.updateNote(note);
//            //상태 변경
//            order.decide(OrderStatus.APPROVED);
//            //출고 명령 생성 요청(warehouse 서비스로 전송)
//            WarehouseShippingRequest request = WarehouseShippingRequest.from(order,items);
//
//            //warehouse 서비스로 전달
//            warehouseClient.create(request);
//        } catch (Exception e) {
//            //warehouseClient.cancel(orderId);
//        }
//    }

    /**
     * 발주 반려
     */
    @Override
    public void rejectOrder(Long orderId, String note) {
        //발주서 조회
        PurchaseOrder order = purchaseOrderRepository.findById(orderId)
                .orElseThrow(()-> new NotFoundException(ErrorStatus.NOT_FOUND_ORDER_EXCEPTION.getMessage()));
        order.updateNote(note);
        //상태변경
        order.decide(OrderStatus.REJECTED);
    }
}
