package senior.copycoders.project.store.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import senior.copycoders.project.store.entities.PaymentEntity;

public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {
}
