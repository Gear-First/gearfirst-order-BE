package com.gearfirst.backend.api.order.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 발주 항목 단위의 비지니스 규칙을 책임지는 엔티티
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name="order_item")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="order_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    @Column(nullable = false)
    private int quantity;           //요청 수량
    @Column(name="part_id",nullable = false)
    private long partId;            //부품 id
    @Column(name="part_name",nullable = false, length = 100)
    private String partName;   //부품 이름
    @Column(name="part_code",nullable = false, length = 50)
    private String partCode;        //부품 코드
    @Column(nullable = false)
    private int price;              //가격
    @Column(name="total_price",nullable = false)
    private int totalPrice;         //총 가격


    @Builder
    public OrderItem(
            PurchaseOrder purchaseOrder,
            Long partId,
            String partName,
            String partCode,
            int price,
            int quantity)
    {
        validate(price,quantity);
        validateInventoryInfo(partName, partCode);
        this.purchaseOrder = purchaseOrder;
        this.partId = partId;
        this.partName = partName;
        this.partCode = partCode;
        this.price = price;
        this.quantity = quantity;
        this.totalPrice = calculateTotalPrice();
    }

    //총액 계산
    private int calculateTotalPrice(){
        return price * quantity;
    }

    //총액 재계산(수량이 변경됐을 경우)
    public void changeQuantity(int newQuantity){
        if(newQuantity <= 0){
            throw new IllegalArgumentException("수량은 1개 이상이여야 합니다.");
        }
        this.quantity = newQuantity;
        this.totalPrice = calculateTotalPrice();
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
    //재고 정보 유효성 검사
    private void validateInventoryInfo(String name, String code) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("부품 이름은 필수입니다.");
        }
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("부품 코드는 필수입니다.");
        }
    }

}
