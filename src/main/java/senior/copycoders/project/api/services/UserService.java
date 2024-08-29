package senior.copycoders.project.api.services;


import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import senior.copycoders.project.store.entities.PersonEntity;
import senior.copycoders.project.store.repositories.PersonRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Transactional
public class UserService implements UserDetailsService {
    PersonRepository personRepository;

    public List<PersonEntity> getAll() {
        return personRepository.findAll();
    }

    public Optional<PersonEntity> getByUsername(String username) {
        return personRepository.findByUsername(username);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<PersonEntity> person = getByUsername(username);
        if (!person.isPresent()) {
            throw new UsernameNotFoundException(String.format("User %s is not found", username));
        }
        return new org.springframework.security.core.userdetails.User(person.get().getUsername(), person.get().getPassword(), true, true, true, true, new HashSet<>());
    }
}
