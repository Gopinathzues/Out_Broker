package outbroker_backend.report.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import outbroker_backend.report.entity.Report;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReportRepository extends JpaRepository<Report, UUID> {
    List<Report> findByStatus(String status);
    List<Report> findByPropertyId(UUID propertyId);
}