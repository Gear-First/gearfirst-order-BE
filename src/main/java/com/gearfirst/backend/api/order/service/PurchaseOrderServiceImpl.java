package com.gearfirst.backend.api.order.service;

import com.gearfirst.backend.api.order.dto.NotificationDto;
import com.gearfirst.backend.api.order.dto.request.PurchaseOrderRequest;
import com.gearfirst.backend.api.order.dto.response.HeadPurchaseOrderDetailResponse;
import com.gearfirst.backend.api.order.dto.response.HeadPurchaseOrderResponse;
import com.gearfirst.backend.api.order.dto.response.PurchaseOrderDetailResponse;
import com.gearfirst.backend.api.order.dto.response.PurchaseOrderResponse;
import com.gearfirst.backend.api.order.entity.OrderItem;
import com.gearfirst.backend.api.order.entity.PurchaseOrder;
import com.gearfirst.backend.api.order.infra.client.WarehouseClient;
import com.gearfirst.backend.api.order.infra.dto.ReceivingCreateNoteRequest;
import com.gearfirst.backend.api.order.infra.dto.WarehouseShippingRequest;
import com.gearfirst.backend.api.order.repository.OrderItemRepository;
import com.gearfirst.backend.api.order.repository.PurchaseOrderQueryRepository;
import com.gearfirst.backend.api.order.repository.PurchaseOrderRepository;
import com.gearfirst.backend.common.context.UserContext;
import com.gearfirst.backend.common.dto.response.PageResponse;
import com.gearfirst.backend.common.enums.OrderStatus;
import com.gearfirst.backend.common.exception.*;
import com.gearfirst.backend.common.response.ErrorStatus;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional              //내부의 모든 public 메서드에 자동으로 트랜잭션 적용
public class PurchaseOrderServiceImpl implements PurchaseOrderService{

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final OrderItemRepository orderItemRepository;
    private final WarehouseClient warehouseClient; // Feign Client (warehouse-service 연결)
    private final PurchaseOrderQueryRepository purchaseOrderQueryRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;


    /**
     * 대리점과 창고에서 발주 요청 생성
     */
    @Override
    public PurchaseOrderResponse createPurchaseOrder(UserContext user,PurchaseOrderRequest request) {
        validUser(user);
        Long userId = Long.parseLong(user.getUserId());
        String userName = user.getUsername();
        String role = user.getRank();
        String workType = user.getWorkType();
        String region = user.getRegion();
        String requesterCode = region + workType;

         //차량 정보 검증
        validateVehicleInfo(request);
        //발주 엔티티 생성
        PurchaseOrder order = PurchaseOrder.builder()
                .vehicleNumber(request.getVehicleNumber())
                .vehicleModel(request.getVehicleModel())
                .requesterId(userId)
                .requesterName(userName)
                .requesterRole(role)
                .requesterCode(requesterCode)
                .receiptNum(request.getReceiptNum())
                .build();

        //부품 정보
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

        String topic = "notification";
        NotificationDto n = NotificationDto.builder()
                .id(1L)
                .eventId(UUID.randomUUID().toString())
                .type("발주 요청 등록")
                .message(user.getRegion() + " " + user.getWorkType() + "의 발주 요청이 등록되었습니다.")
                .receiver("본사")
                .build();

        kafkaTemplate.send(topic, n);

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
            UserContext user,
            LocalDate startDate, LocalDate endDate,
            String searchKeyword,
            Pageable pageable
    ) {
        validUser(user);
        if(!"본사".equals(user.getWorkType())){
            throw new UnAuthorizedException(ErrorStatus.NOT_ALLOW_ACCESS.getMessage());
        }
        List<OrderStatus> status = List.of(OrderStatus.PENDING);
        Page<PurchaseOrder> page = purchaseOrderQueryRepository.searchOrders(
                startDate, endDate, searchKeyword, status, pageable);
        return mapToPageResponse(page);
    }
    /**
     * 본사용 승인 대기 상태를 제외한 발주 전체 조회(모든 대리점)
     * -> 출고중, 승인완료, 납품완료
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<HeadPurchaseOrderResponse> getProcessedOrders(
            UserContext user,
            LocalDate startDate, LocalDate endDate,
            String searchKeyword,
            String status,
            Pageable pageable
    ) {
        validUser(user);
        if(!"본사".equals(user.getWorkType())){
            throw new UnAuthorizedException(ErrorStatus.NOT_ALLOW_ACCESS.getMessage());
        }
        // 기본 상태 세 가지
        List<OrderStatus> statuses = parseStatusOrDefault(status, List.of(OrderStatus.APPROVED, OrderStatus.SHIPPED, OrderStatus.COMPLETED));

        Page<PurchaseOrder> page = purchaseOrderQueryRepository.searchOrders(
                startDate, endDate, searchKeyword, statuses, pageable);
        return mapToPageResponse(page);
    }
    /**
     * 본사용 승인 대기 상태를 제외한 발주 전체 조회(모든 대리점)
     * -> 반려,취소
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<HeadPurchaseOrderResponse> getCancelOrders(
            UserContext user,
            LocalDate startDate, LocalDate endDate,
            String searchKeyword,
            String status,
            Pageable pageable
    ) {
        validUser(user);
        if(!"본사".equals(user.getWorkType())){
            throw new UnAuthorizedException(ErrorStatus.NOT_ALLOW_ACCESS.getMessage());
        }
        List<OrderStatus> statuses = parseStatusOrDefault(status, List.of(OrderStatus.REJECTED, OrderStatus.CANCELLED));

        Page<PurchaseOrder> page = purchaseOrderQueryRepository.searchOrders(
                startDate, endDate, searchKeyword, statuses, pageable);
        return mapToPageResponse(page);
    }
    //공통 검증 로직
    private List<OrderStatus> parseStatusOrDefault(String status, List<OrderStatus> defaultStatues) {
        if (status != null && !status.isBlank()) {
            // status 문자열을 enum으로 안전하게 변환
            return Arrays.stream(OrderStatus.values())
                    .filter(s -> s.name().equalsIgnoreCase(status))
                    .findFirst()
                    .map(List::of) // 매칭된 enum을 리스트로 변환
                    .orElseThrow(() -> new BadRequestException(ErrorStatus.INVALID_STATUS_EXCEPTION.getMessage()));
        }return defaultStatues;
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
    public HeadPurchaseOrderDetailResponse getHeadPurchaseOrderDetail(UserContext user, Long orderId) {
        validUser(user);
        if(!"본사".equals(user.getWorkType())){
            throw new UnAuthorizedException(ErrorStatus.NOT_ALLOW_ACCESS.getMessage());
        }
        PurchaseOrder order = purchaseOrderRepository.findById(orderId)
                .orElseThrow(()-> new NotFoundException(ErrorStatus.NOT_FOUND_ORDER_EXCEPTION.getMessage()));
        List<OrderItem> items = orderItemRepository.findByPurchaseOrder_Id(orderId);
        return HeadPurchaseOrderDetailResponse.from(order, items);
    }

    //대리점, 창고 발주 목록 전체 조회
    //날짜별로 필터링 되는 목록 전체 조회
    @Override
    @Transactional(readOnly = true)
    public PageResponse<PurchaseOrderDetailResponse> getBranchPurchaseOrders(
            UserContext user,
            LocalDate startDate, LocalDate endDate,
            Pageable pageable
    ) {
        validUser(user);
        Long userId = Long.parseLong(user.getUserId());
        String workType = user.getWorkType();
        String region = user.getRegion();
        String requesterCode = region + workType;

        Slice<PurchaseOrder> orders;

        if(startDate != null && endDate != null){
            orders = purchaseOrderRepository.findByRequesterCodeAndRequesterIdAndRequestDateBetweenOrderByRequestDateDesc(
                    requesterCode, userId, startDate.atStartOfDay(), endDate.atTime(23,59,59), pageable);
        } else {
            orders = purchaseOrderRepository.findByRequesterCodeAndRequesterIdOrderByRequestDateDesc(requesterCode, userId, pageable);
        }

        List<PurchaseOrderDetailResponse> content = orders.getContent().stream()
                .map(order -> {
                    List<OrderItem> items = orderItemRepository.findByPurchaseOrder_Id(order.getId());
                    return PurchaseOrderDetailResponse.from(order, items);
                })
                .toList();
        return new PageResponse<>(new SliceImpl<>(content, pageable, orders.hasNext()));

    }

    //대리점 대리점 상태 그룹별 조회(준비/ 완료 / 취소)
    //페이지네이션
    @Override
    @Transactional(readOnly = true)
    public PageResponse<PurchaseOrderDetailResponse> getBranchPurchaseOrdersByFilter(
            UserContext user,String filterType,
            LocalDate startDate, LocalDate endDate, Pageable pageable
    ) {
        validUser(user);
        Long userId = Long.parseLong(user.getUserId());
        String workType = user.getWorkType();
        String region = user.getRegion();
        String requesterCode = region + workType;

        Slice<PurchaseOrder> orders;

        List<OrderStatus> statusList = switch (filterType.toLowerCase()){
            case "ready" -> List.of(OrderStatus.PENDING, OrderStatus.APPROVED, OrderStatus.SHIPPED);
            case "completed" -> List.of(OrderStatus.COMPLETED);
            case "cancelled" -> List.of(OrderStatus.REJECTED,OrderStatus.CANCELLED);
            default -> throw new IllegalArgumentException("유효하지 않은 필터 타입입니다. (ready, completed, cancelled 중하나여야 합니다.)");
        };
        if(startDate != null && endDate != null){
            orders = purchaseOrderRepository.findByRequesterCodeAndRequesterIdAndStatusInAndRequestDateBetweenOrderByRequestDateDesc(
                    requesterCode, userId, statusList, startDate.atStartOfDay(), endDate.atTime(23,59,59), pageable);
        } else {
            orders = purchaseOrderRepository.findByRequesterCodeAndRequesterIdAndStatusInOrderByRequestDateDesc(requesterCode, userId, statusList, pageable);
        }
        List<PurchaseOrderDetailResponse> content = orders.getContent().stream()
                .map(order -> {
                    List<OrderItem> items = orderItemRepository.findByPurchaseOrder_Id(order.getId());
                    return PurchaseOrderDetailResponse.from(order, items);
                })
                .toList();
        return new PageResponse<>(new SliceImpl<>(content, pageable, orders.hasNext()));
    }


    /**
     + 대리점 수리 접수내역 조회 시 발주 부품 조회
     + */
    @Transactional(readOnly = true)
    @Override
    public PurchaseOrderResponse getCompleteRepairPartsList(
            UserContext user,String receiptNum, String vehicleNumber
    ){
        validUser(user);
        Long userId = Long.parseLong(user.getUserId());
        String workType = user.getWorkType();
        String region = user.getRegion();
        String requesterCode = region + workType;

        PurchaseOrder order = purchaseOrderRepository
                .findByVehicleNumberAndRequesterCodeAndRequesterIdAndReceiptNum(
                        vehicleNumber, requesterCode, userId,  receiptNum
                )
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_ORDER_EXCEPTION.getMessage()));
        // 부품 목록 조회
        List<OrderItem> items = orderItemRepository.findByPurchaseOrder_Id(order.getId());

        return PurchaseOrderResponse.from(order, items);
    }

    /**
     * 대리점 발주 상세 조회
     */
    @Override
    @Transactional(readOnly = true)
    public PurchaseOrderDetailResponse getPurchaseOrderDetail(
            UserContext user, Long orderId
    ) {
        validUser(user);
        Long userId = Long.parseLong(user.getUserId());
        String workType = user.getWorkType();
        String region = user.getRegion();
        String requesterCode = region + workType;

        PurchaseOrder order = purchaseOrderRepository.findByIdAndRequesterCodeAndRequesterId(orderId,requesterCode,userId)
                .orElseThrow(()-> new NotFoundException(ErrorStatus.NOT_FOUND_ORDER_EXCEPTION.getMessage()));

        List<OrderItem> items = orderItemRepository.findByPurchaseOrder_Id(orderId);
        return PurchaseOrderDetailResponse.from(order,items);
    }

    /**
     * 대리점과 창고에서 발주 취소
     */
    @Override
    public void cancelBranchOrder(UserContext user, Long orderId){
        validUser(user);
        Long userId = Long.parseLong(user.getUserId());
        String workType = user.getWorkType();
        String region = user.getRegion();
        String requesterCode = region + workType;

        PurchaseOrder order = purchaseOrderRepository.findByIdAndRequesterCodeAndRequesterId(orderId,requesterCode,userId)
                .orElseThrow(()-> new NotFoundException(ErrorStatus.NOT_FOUND_ORDER_EXCEPTION.getMessage()));
        //상태 변경
        order.cancel();

        String topic = "notification";
        NotificationDto n = NotificationDto.builder()
                .id(1L)
                .eventId(UUID.randomUUID().toString())
                .type("발주 요청 취소")
                .message(user.getRegion() + " " + user.getWorkType() + "의 발주 요청이 취소되었습니다.")
                .receiver("본사")
                .build();

        kafkaTemplate.send(topic, n);
    }

    /**
     * 발주 승인(대리점 -> 출고요청 또는 창고 -> 입고요청)
     */
    @Override
    @Transactional
    public void approveOrder(UserContext user, Long orderId,String note) {
        validUser(user);
        if(!"본사".equals(user.getWorkType())){
            throw new UnAuthorizedException(ErrorStatus.NOT_ALLOW_ACCESS.getMessage());
        }
        //발주서 조회
        PurchaseOrder order = purchaseOrderRepository.findById(orderId)
                .orElseThrow(()-> new NotFoundException(ErrorStatus.NOT_FOUND_ORDER_EXCEPTION.getMessage()));
        String code = order.getRequesterCode();
        String warehouseCode = code.replace("대리점", "창고");
        //발주 품목 조회
        List<OrderItem> items = orderItemRepository.findByPurchaseOrder_Id(orderId);

        try {
            //비고 저장
            order.updateNote(note);
            //상태 변경
            order.decide(OrderStatus.APPROVED);
            if(order.getRequesterCode().contains("대리점")){
                //창고지정
                order.assignWarehouse(warehouseCode);
                order.createShipmentCommand();

                //출고 명령 생성 요청(warehouse 서비스로 전송)
                WarehouseShippingRequest request = WarehouseShippingRequest.from(order,items);

                //warehouse 서비스로 전달
                warehouseClient.create(request);
            }
            else if(order.getRequesterCode().contains("창고")) {
                ReceivingCreateNoteRequest request = ReceivingCreateNoteRequest.from(order, items);
                //warehouse 서비스로 전달
                warehouseClient.create(request);
            }

            String topic = "notification";
            NotificationDto n = NotificationDto.builder()
                    .id(1L)
                    .eventId(UUID.randomUUID().toString())
                    .type("발주 요청 승인")
                    .message(user.getRegion() + " " + user.getWorkType() + "의 발주 요청이 승인되었습니다.")
                    .receiver("본사")
                    .build();

            kafkaTemplate.send(topic, n);
        } catch (FeignException e) {
            log.error(" Warehouse 서버 호출 실패!");
            log.error("상태 코드: {}", e.status());
            log.error("응답 본문: {}", e.contentUTF8()); // warehouse 서버가 보낸 에러 메시지 body
            String warehouseErrorMessage = e.contentUTF8();
            throw new ExternalServerException(String.format("warehouse 서버 처리 실패: %s", warehouseErrorMessage));
        } catch (Exception e) {
            log.error("예상치 못한 오류 발생: {}", e.getMessage());
            throw new InternalServerException(e.getMessage());
        }
    }

    /**
     * 발주 출고날짜 업데이트
     */
    @Override
    public void ship(UserContext user, Long orderId) {
        validUser(user);
        if(!"창고".equals(user.getWorkType())){
            throw new UnAuthorizedException(ErrorStatus.NOT_ALLOW_ACCESS.getMessage());
        }
        //발주서 조회
        PurchaseOrder order = purchaseOrderRepository.findById(orderId)
                .orElseThrow(()-> new NotFoundException(ErrorStatus.NOT_FOUND_ORDER_EXCEPTION.getMessage()));
        //상태변경
        order.ship();
    }

    /**
     * 발주 반려
     */
    @Override
    public void rejectOrder(UserContext user, Long orderId, String note) {
        validUser(user);
        if(!"본사".equals(user.getWorkType())){
            throw new UnAuthorizedException(ErrorStatus.NOT_ALLOW_ACCESS.getMessage());
        }
        //발주서 조회
        PurchaseOrder order = purchaseOrderRepository.findById(orderId)
                .orElseThrow(()-> new NotFoundException(ErrorStatus.NOT_FOUND_ORDER_EXCEPTION.getMessage()));
        order.updateNote(note);
        //상태변경
        order.decide(OrderStatus.REJECTED);

        String topic = "notification";
        NotificationDto n = NotificationDto.builder()
                .id(1L)
                .eventId(UUID.randomUUID().toString())
                .type("발주 요청 반려")
                .message(user.getRegion() + " " + user.getWorkType() + "의 발주 요청이 반려되었습니다.")
                .receiver("본사")
                .build();

        kafkaTemplate.send(topic, n);
    }

    private void validUser(UserContext user) {
        if (user == null || user.getUserId() == null) {
            throw new UnAuthorizedException(ErrorStatus.USER_UNAUTHORIZED.getMessage());
        }
    }
}
