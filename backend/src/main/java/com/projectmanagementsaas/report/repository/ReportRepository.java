package com.projectmanagementsaas.report.repository;

import com.projectmanagementsaas.report.entity.Report;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, UUID> {
    List<Report> findByGeneratedBy_IdOrderByGeneratedAtDesc(UUID generatedById);

    Optional<Report> findByIdAndGeneratedBy_Id(UUID id, UUID generatedById);
}
