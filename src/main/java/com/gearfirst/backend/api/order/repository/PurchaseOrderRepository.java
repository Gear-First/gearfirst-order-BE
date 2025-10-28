package com.gearfirst.backend.api.order.repository;

import com.gearfirst.backend.api.order.entity.PurchaseOrder;
import com.gearfirst.backend.common.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder,Long> {
    //본사 발주 내역 전체 조회
    List<PurchaseOrder> findAllByOrderByRequestDateDesc();

    //엔지니어용 발주 내역 조회
    List<PurchaseOrder> findByBranchIdAndEngineerIdOrderByRequestDateDesc(Long branchId, Long engineerId);

    //대리점 상태 그룹별로 목록 조회
     List<PurchaseOrder> findByBranchIdAndEngineerIdAndStatusInOrderByRequestDateDesc(Long branchId, Long engineerId, List<OrderStatus> statuses);

    //본사 상태별 발주 목록 확인
    List<PurchaseOrder> findByStatusOrderByRequestDateDesc(OrderStatus status);

    //차량 번호로 발주 내역 조회
    Optional<PurchaseOrder> findByVehicleNumberAndBranchIdAndEngineerId(String vehicleNumber, Long branchId, Long engineerId);
    Optional<PurchaseOrder> findByVehicleNumberAndBranchIdAndEngineerIdAndStatus(String vehicleNumber, Long branchId, Long engineerId, OrderStatus orderStatus);

    //대리점 발주 내역 상세 조회
    Optional<PurchaseOrder> findByIdAndBranchIdAndEngineerId(Long id, Long branchId, Long engineerId);


}
