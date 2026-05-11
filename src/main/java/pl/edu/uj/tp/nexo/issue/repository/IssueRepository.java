package pl.edu.uj.tp.nexo.issue.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import pl.edu.uj.tp.nexo.issue.entity.Issue;

import java.util.List;
import java.util.Optional;

@Repository
public interface IssueRepository extends JpaRepository<Issue, Long>, JpaSpecificationExecutor<Issue> {
    List<Issue> findAllByOrganizationId(Long organizationId);

    Optional<Issue> findByIdAndOrganizationId(Long id, Long organizationId);

    List<Issue> findAllByEpicIdAndOrganizationId(Long epicId, Long organizationId);
}