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
    Slice<PurchaseOrder> findByBranchCodeAndEngineerIdOrderByRequestDateDesc(String branchCode, Long engineerId, Pageable pageable);
    Slice<PurchaseOrder> findByBranchCodeAndEngineerIdAndRequestDateBetweenOrderByRequestDateDesc(String branchCode, Long engineerId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    //대리점 상태 그룹별로 목록 조회
    Slice<PurchaseOrder> findByBranchCodeAndEngineerIdAndStatusInOrderByRequestDateDesc(String branchCode, Long engineerId, List<OrderStatus> statuses, Pageable pageable);
    Slice<PurchaseOrder> findByBranchCodeAndEngineerIdAndStatusInAndRequestDateBetweenOrderByRequestDateDesc(String branchCode, Long engineerId, List<OrderStatus> statuses, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    //본사 상태별 발주 목록 확인
    List<PurchaseOrder> findByStatusOrderByRequestDateDesc(OrderStatus status);

    //차량 번호와 상태로 발주 내역 조회
    Optional<PurchaseOrder> findByVehicleNumberAndBranchCodeAndEngineerIdAndStatusAndReceiptNum(String vehicleNumber, String branchCode, Long engineerId, OrderStatus orderStatus,String receiptNum);
    Optional<PurchaseOrder> findByVehicleNumberAndBranchCodeAndEngineerIdAndReceiptNum(String vehicleNumber, String branchCode, Long engineerId, String receiptNum);

    //대리점 발주 내역 상세 조회
    Optional<PurchaseOrder> findByIdAndBranchCodeAndEngineerId(Long id, String branchCode, Long engineerId);

    //수리 접수 번호 중복체크
    Optional<PurchaseOrder> findByReceiptNum(String receiptNum);




}
