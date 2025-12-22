package com.mobilesec.reportservice.repository;

import com.mobilesec.reportservice.entity.ReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReportRepository extends JpaRepository<ReportEntity, Long> {
    Optional<ReportEntity> findByScanId(String scanId);
}
