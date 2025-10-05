package com.gearfirst.backend.api.order.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name="order_item")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="order_id")
    private Long orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private PurchaseOrder purchaseOrder;

    private int quantity;           //요청 수량
    @Column(name="inventory_id")
    private long inventoryId;       //재고 id
    @Column(name="inventory_name")
    private String inventoryName;   //부품 이름
    private int price;              //가격
    @Column(name="total_price")
    private int totalPrice;         //총 가격


    @Builder
    public OrderItem(
            PurchaseOrder purchaseOrder,
            Long inventoryId,
            String inventoryName,
            int price,
            int quantity)
    {
        validate(price,quantity);

        this.purchaseOrder = purchaseOrder;
        this.inventoryId = inventoryId;
        this.inventoryName = inventoryName;
        this.price = price;
        this.quantity = quantity;
        this.totalPrice = calculateTotalPrice();
    }

    //총액 재계산(수량이 변경됐을 경우)
    public void changeQuantity(int newQuantity){
        if(newQuantity <= 0){
            throw new IllegalArgumentException("수량은 1개 이상이여야 합니다.");
        }
        this.quantity = newQuantity;
        this.totalPrice = calculateTotalPrice();
    }
    //총액 계산
    private int calculateTotalPrice(){
        return price * quantity;
    }

    //수량, 가격 유효성 검사
    private void validate(int price, int quantity){
        if(quantity <= 0){
            throw new IllegalArgumentException("수량은 1개 이상이여야 합니다.");
        }
        if(price < 0){
            throw new IllegalArgumentException("단가는 음수일 수 없습니다.");
        }
    }
}
