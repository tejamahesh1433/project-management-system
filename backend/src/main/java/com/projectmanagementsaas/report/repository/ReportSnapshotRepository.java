package com.projectmanagementsaas.report.repository;

import com.projectmanagementsaas.report.entity.ReportSnapshot;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportSnapshotRepository extends JpaRepository<ReportSnapshot, UUID> {
    List<ReportSnapshot> findByReport_IdOrderByCreatedAtDesc(UUID reportId);

    Optional<ReportSnapshot> findFirstByReport_IdOrderByCreatedAtDesc(UUID reportId);
}
