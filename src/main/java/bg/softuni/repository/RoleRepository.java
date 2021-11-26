package bg.softuni.repository;

import bg.softuni.model.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, String> {
    Role getByName(String name);

    Optional<Role> findByName(String name);
}
