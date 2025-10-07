package com.gearfirst.backend.api.branch.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "branch")
public class Branch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "branch_id")
    private Long id;

    @Column(name="branch_name", nullable = false, length = 100)
    private String branchName;

    @Column(nullable = false, length = 50)
    private String region;
    @Column(nullable = false, length = 50)
    private String contact;
    @Column(name = "manager_name", length = 50)
    private String managerName;

    // --- 정적 팩토리 메서드 ---
    public static Branch create(String branchName, String region, String contact, String managerName) {
        validateBranchInfo(branchName, region, contact);
        return new Branch(branchName, region, contact, managerName);
    }


    // --- private 생성자 ---
    private Branch(String branchName, String region, String contact, String managerName) {
        this.branchName = branchName;
        this.region = region;
        this.contact = contact;
        this.managerName = managerName;
    }

    //유효성 검증
    private static void validateBranchInfo(String branchName, String region, String contact) {
        if (branchName == null || branchName.isBlank()) {
            throw new IllegalArgumentException("대리점 이름은 필수입니다.");
        }
        if (region == null || region.isBlank()) {
            throw new IllegalArgumentException("지역은 필수입니다.");
        }
        if (contact == null || contact.isBlank()) {
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
        if (newManagerName == null || newManagerName.isBlank()) {
            throw new IllegalArgumentException("담당자 이름을 입력해주세요.");
        }
        this.managerName = newManagerName;
    }

}
