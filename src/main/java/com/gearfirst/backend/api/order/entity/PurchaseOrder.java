package com.gearfirst.backend.api.order.entity;

import com.gearfirst.backend.api.branch.entity.Branch;
import com.gearfirst.backend.api.order.command.ShipmentCommand;
import com.gearfirst.backend.common.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name ="purchase_order")
public class PurchaseOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="order_id")
    private Long id;

    @Column(name="request_date")
    private LocalDateTime requestDate;   // 발주 요청일
    @Column(name="approved_date")
    private LocalDateTime approvedDate;  // 승인일
    @Column(name="transfer_date")
    private LocalDateTime transferDate;  // 창고 이관일
    @Column(name="completed_date")
    private LocalDateTime completedDate; // 납품 완료일

    @Column(name="order_number", nullable = false)
    private String orderNumber;         //발주 번호

    @Column(name="vehicle_number", nullable = false)
    private String vehicleNumber;        //차량 번호

    @Column(name="vehicle_model", nullable = false)
    private String vehicleModel;        //차량 모델

    @Column(name="engineer_id", nullable = false)
    private Long engineerId;            //엔지니어 id

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(name="total_price", nullable = false)
    private int totalPrice;

    @Column(name = "branch_id",nullable = false)
    private Long branchId;


    @Builder
    public PurchaseOrder(String vehicleNumber, String vehicleModel, Long engineerId, Long branchId){
        this.requestDate = LocalDateTime.now();
        this.orderNumber = generateOrderNumber(this.requestDate);
        this.vehicleNumber = vehicleNumber;
        this.vehicleModel = vehicleModel;
        this.engineerId = engineerId;
        this.branchId = branchId;
        this.status = OrderStatus.PENDING; //기본 상태 승인 대기
        this.totalPrice = 0;
    }

    private static String generateOrderNumber(LocalDateTime requestDate){
        return "PO-" + requestDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                +"-" + UUID.randomUUID().toString().substring(0,4).toUpperCase();
    }

    //본사 승인
    public void approve(){
        if (this.approvedDate != null) throw new IllegalStateException("이미 승인된 주문입니다.");
        validateStateTransition(OrderStatus.PENDING, OrderStatus.APPROVED);
        this.status = OrderStatus.APPROVED;
        this.approvedDate = LocalDateTime.now();
    }
    //반려
    public void reject(){
        validateStateTransition(OrderStatus.PENDING, OrderStatus.REJECTED);
        this.status = OrderStatus.REJECTED;
    }
    //출고
    public void ship(){
        validateStateTransition(OrderStatus.APPROVED, OrderStatus.SHIPPED);
        this.status = OrderStatus.SHIPPED;
        this.transferDate = LocalDateTime.now();
    }
    //납품 완료
    public void complete(){
        validateStateTransition(OrderStatus.SHIPPED, OrderStatus.COMPLETED);
        this.status = OrderStatus.COMPLETED;
        this.completedDate = LocalDateTime.now();
    }
    public void cancel(){
        validateStateTransitionCancel(this.status);
        this.status = OrderStatus.CANCELLED;
    }
    //상태 전이 검증
    private void validateStateTransition(OrderStatus expected, OrderStatus next){
        if(this.status != expected){
            throw new IllegalStateException(
                    String.format("현재 상태(%s)에서는 %s 상태로 전환할 수 없습니다.", this.status,next)
            );
        }
    }
    private void validateStateTransitionCancel(OrderStatus status){
        if(status == OrderStatus.SHIPPED ||status == OrderStatus.COMPLETED){
            throw new IllegalStateException("출고 이후에는 취소할 수 없습니다.");
        }
    }
    //총 금액 계산
    public void calculateTotalPrice(List<OrderItem> items){
        this.totalPrice = items.stream()
                .mapToInt(OrderItem::getTotalPrice)
                .sum();
    }
    //금액 업데이트
    public void updateTotalPrice(int totalPrice){
        this.totalPrice = totalPrice;
    }

    //창고 출고 명령 객체 생성
    public ShipmentCommand createShipmentCommand(){
        if(this.status != OrderStatus.APPROVED){
            throw new IllegalArgumentException("승인된 주문만 출고 지시를 생성할 수 있습니다.");
        }
        return new ShipmentCommand(this.id, this.branchId,this.vehicleNumber,this.vehicleModel);
    }

}
