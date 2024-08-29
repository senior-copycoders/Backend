package senior.copycoders.project.store.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import senior.copycoders.project.store.entities.PersonEntity;

import java.util.Optional;

public interface PersonRepository extends JpaRepository<PersonEntity, Long> {
    Optional<PersonEntity> findByUsername(String username);
}
