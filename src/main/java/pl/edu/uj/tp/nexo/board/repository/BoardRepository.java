package pl.edu.uj.tp.nexo.board.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.uj.tp.nexo.board.entity.Board;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {
    List<Board> findAllByOrganizationId(Long organizationId);

    Optional<Board> findByIdAndOrganizationId(Long id, Long organizationId);
}