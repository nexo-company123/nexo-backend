package pl.edu.uj.tp.nexo.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.uj.tp.nexo.user.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    List<User> findAllByOrganizationId(Long organizationId);

    Optional<User> findByIdAndOrganizationId(Long id, Long organizationId);
}