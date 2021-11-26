package bg.softuni.repository;

import bg.softuni.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> getByUsername(String username);

    Optional<User> getByEmail(String email);

    User findByUsername(String username);

    List<User> getAllByUsernameOrUsername(String firstValue, String secondValue);
}
