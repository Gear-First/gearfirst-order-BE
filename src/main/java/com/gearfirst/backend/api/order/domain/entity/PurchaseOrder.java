package com.gearfirst.backend.api.order.domain.entity;

import com.gearfirst.backend.api.branch.domain.entity.Branch;
import com.gearfirst.backend.common.enums.OrderStatus;
import com.gearfirst.backend.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name ="purchase_order")
public class PurchaseOrder extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="order_id")
    private Long orderId;

    //@Column(name="request_date", nullable = false)
    //private LocalDate requestDate;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Column(name="total_price")
    private int totalPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private Branch branch;


    @Builder
    public PurchaseOrder(Branch branch){
        this.status = OrderStatus.PENDING; //기본 상태 승인 대기
        this.branch = branch;
    }

    public void updateTotalPrice(int totalPrice){
        this.totalPrice = totalPrice;
    }

    //본사 승인
    public void approve(){
        validateStateTransition(OrderStatus.PENDING, OrderStatus.APPROVED);
        this.status = OrderStatus.APPROVED;
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
    }
    //납품 완료
    public void complete(){
        validateStateTransition(OrderStatus.SHIPPED, OrderStatus.COMPLETED);
        this.status = OrderStatus.COMPLETED;
    }
    //상태 전이 검증
    private void validateStateTransition(OrderStatus expected, OrderStatus next){
        if(this.status != expected){
            throw new IllegalStateException(
                    String.format("현재 상태(%s)에서는 %s 상태로 전환할 수 없습니다.", this.status,next)
            );
        }
    }


    /**
     * TODO: 출고 지시를 창고에 전송 - public ShipmentCommand createShipmentCommand()
     * TODO: 주문 총 금액 계산 - public int calculateTotalPrice()
     * TODO: 발주 취소 - public void cancel()
     */
}
