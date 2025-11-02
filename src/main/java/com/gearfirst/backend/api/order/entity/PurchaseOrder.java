package com.gearfirst.backend.api.order.entity;


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
    @Column(name="processed_date")
    private LocalDateTime processedDate;  // 승인일
    @Column(name="transfer_date")
    private LocalDateTime transferDate;  // 창고 이관일
    @Column(name="completed_date")
    private LocalDateTime completedDate; // 납품 완료일

    @Column(name="order_number", nullable = false)
    private String orderNumber;         //발주 번호

    @Column(name="vehicle_number")
    private String vehicleNumber;        //차량 번호

    @Column(name="vehicle_model")
    private String vehicleModel;        //차량 모델

    @Column(name = "branch_code",nullable = false)
    private String branchCode;

    @Column(name="engineer_id", nullable = false)
    private Long engineerId;            //엔지니어 id

    @Column(name="engineer_name", nullable = false)
    private String engineerName;            //엔지니어이름

    @Column(name="engineer_role", nullable = false)
    private String engineerRole;            //엔지니어 직급

    @Column(name="receipt_num")
    private String receiptNum;              //수리 이력 Num

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(name="total_price", nullable = false)
    private int totalPrice;

    @Column(name="total_quantity", nullable = false)
    private int totalQuantity;

    @Column(columnDefinition = "text")
    private String note;                    //비고


    @Builder
    public PurchaseOrder(String vehicleNumber, String vehicleModel, String receiptNum, String branchCode,
                         Long engineerId,String engineerName, String engineerRole)
    {
        this.requestDate = LocalDateTime.now();
        this.orderNumber = generateOrderNumber(this.requestDate);
        this.vehicleNumber = vehicleNumber;
        this.vehicleModel = vehicleModel;
        this.engineerId = engineerId;
        this.engineerName = engineerName;
        this.engineerRole = engineerRole;
        this.branchCode = branchCode;
        this.receiptNum = receiptNum;
        this.status = OrderStatus.PENDING; //기본 상태 승인 대기
        this.totalPrice = 0;
    }

    //발주 번호 생성
    private static String generateOrderNumber(LocalDateTime requestDate){
        return "PO-" + requestDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                +"-" + UUID.randomUUID().toString().substring(0,4).toUpperCase();
    }
    //승인 또는 반려 처리
    public void decide(OrderStatus nextStatus) {
        if (this.processedDate != null) throw new IllegalStateException("이미 처리된 주문입니다.");
        if (nextStatus != OrderStatus.APPROVED && nextStatus != OrderStatus.REJECTED) {
            throw new IllegalArgumentException("승인 또는 반려 상태만 결정할 수 있습니다.");
        }

        validateStateTransition(OrderStatus.PENDING, nextStatus);
        this.status = nextStatus;
        this.processedDate = LocalDateTime.now();
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
    //발주 취소
    public void cancel(){
        validateStateTransitionCancel(this.status);
        this.status = OrderStatus.CANCELLED;
    }
    //수리용 부품 발주 완료
    public void completeRepair(){
        validateStateTransition(OrderStatus.COMPLETED, OrderStatus.USED_IN_REPAIR);
        this.status = OrderStatus.USED_IN_REPAIR;
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
        if(status != OrderStatus.PENDING && status != OrderStatus.APPROVED){
            throw new IllegalStateException("승인 대기 또는 승인 완료 상태의 발주만 취소할 수 있습니다.");
        }
    }
    //총 금액 계산
    public void calculateTotalPrice(List<OrderItem> items){
        this.totalPrice = items.stream()
                .mapToInt(OrderItem::getTotalPrice)
                .sum();
    }
    //총 수량 계산
    public void calculateTotalQuantity(List<OrderItem> orderItems){
        this.totalQuantity = orderItems.stream()
                .mapToInt(OrderItem::getQuantity)
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
        return new ShipmentCommand(this.id, this.branchCode,this.vehicleNumber,this.vehicleModel);
    }

}
