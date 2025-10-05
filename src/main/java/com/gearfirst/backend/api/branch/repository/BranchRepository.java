package com.gearfirst.backend.api.branch.repository;

import com.gearfirst.backend.api.branch.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BranchRepository extends JpaRepository<Branch, Long> {
    //발주 요청시, 대리점 실제 존재하는지 확인
    boolean existsByBranchName(String branchName);
    //로그인이나 관리자페이지에서 대리점 상세조회
    Optional<Branch> findByBranchName(String branchName);
}
