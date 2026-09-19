package outbroker_backend.savedsearch.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import outbroker_backend.savedsearch.entity.SavedSearch;

import java.util.List;
import java.util.UUID;

@Repository
public interface SavedSearchRepository
        extends JpaRepository<SavedSearch, UUID> {

    List<SavedSearch> findByUserIdOrderByCreatedAtDesc(UUID userId);

    void deleteByUserId(UUID userId);
}