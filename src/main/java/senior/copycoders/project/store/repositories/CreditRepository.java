package senior.copycoders.project.store.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import senior.copycoders.project.store.entities.CreditEntity;

public interface CreditRepository extends JpaRepository<CreditEntity, Long> {
}
