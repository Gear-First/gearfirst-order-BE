package com.gearfirst.backend.api.branch.domain.entity;

import com.gearfirst.backend.api.order.domain.entity.OrderItem;
import com.gearfirst.backend.api.order.domain.entity.PurchaseOrder;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "branch")
public class Branch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "branch_id")
    private Long branchId;

    @Column(name="branch_name")
    private String branchName;

    private String region;
    private String contact;
    @Column(name = "manager_name")
    private String managerName;

    //유효성 검증
    public void validateBranchInfo(String branchName, String region, String contact){
        if(branchName == null || branchName.isBlank()){
            throw new IllegalArgumentException("대리점 이름은 필수입니다.");
        }
        if (region == null || region.isBlank()) {
            throw new IllegalArgumentException("지역은 필수입니다.");
        }
        if(contact == null || contact.isBlank()){
            throw new IllegalArgumentException("연락처는 필수입니다.");
        }
    }

    //연락처 변경
    public void updateContact(String newContact){
        if(newContact == null || newContact.isBlank()){
            throw new IllegalArgumentException("연락처를 입력해주세요.");
        }
        this.contact = newContact;
    }

    //담당자 변경
    public void updateManager(String newManagerName) {
        this.managerName = newManagerName;
    }

    public Branch(String branchName, String region, String contact, String managerName) {
        validateBranchInfo(branchName, region, contact);
        this.branchName = branchName;
        this.region = region;
        this.contact = contact;
        this.managerName = managerName;
    }
}
