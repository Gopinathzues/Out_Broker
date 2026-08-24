package outbroker_backend.inquiry.repository;

import outbroker_backend.inquiry.entity.Inquiry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InquiryRepository extends JpaRepository<Inquiry, UUID> {
    
    List<Inquiry> findByTenantId(UUID tenantId);
    
    List<Inquiry> findByOwnerId(UUID ownerId);
}