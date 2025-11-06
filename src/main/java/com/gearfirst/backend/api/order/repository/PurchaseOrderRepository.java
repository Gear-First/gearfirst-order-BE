package com.gearfirst.backend.api.order.repository;

import com.gearfirst.backend.api.order.entity.PurchaseOrder;
import com.gearfirst.backend.common.enums.OrderStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

//Spring Data JPA 기본 Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder,Long> {
    //본사 발주 내역 상세 조회
    //Optional<PurchaseOrder> findById(Long orderId);

    //엔지니어용 발주 내역 조회
    Slice<PurchaseOrder> findByRequesterCodeAndRequesterIdOrderByRequestDateDesc(String requesterCode, Long requesterId, Pageable pageable);
    Slice<PurchaseOrder> findByRequesterCodeAndRequesterIdAndRequestDateBetweenOrderByRequestDateDesc(String requesterCode, Long requesterId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    //대리점 상태 그룹별로 목록 조회
    Slice<PurchaseOrder> findByRequesterCodeAndRequesterIdAndStatusInOrderByRequestDateDesc(String requesterCode, Long requesterId, List<OrderStatus> statuses, Pageable pageable);
    Slice<PurchaseOrder> findByRequesterCodeAndRequesterIdAndStatusInAndRequestDateBetweenOrderByRequestDateDesc(String requesterCode, Long requesterId, List<OrderStatus> statuses, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    //본사 상태별 발주 목록 확인
    List<PurchaseOrder> findByStatusOrderByRequestDateDesc(OrderStatus status);

    //차량 번호와 상태로 발주 내역 조회
    Optional<PurchaseOrder> findByVehicleNumberAndRequesterCodeAndRequesterIdAndStatusAndReceiptNum(String vehicleNumber, String requesterCode, Long requesterId, OrderStatus orderStatus,String receiptNum);
    Optional<PurchaseOrder> findByVehicleNumberAndRequesterCodeAndRequesterIdAndReceiptNum(String vehicleNumber, String requesterCode, Long requesterId, String receiptNum);

    //대리점 발주 내역 상세 조회
    Optional<PurchaseOrder> findByIdAndRequesterCodeAndRequesterId(Long id, String requesterCode, Long requesterId);

    //수리 접수 번호 중복체크
    Optional<PurchaseOrder> findByReceiptNum(String receiptNum);




}
