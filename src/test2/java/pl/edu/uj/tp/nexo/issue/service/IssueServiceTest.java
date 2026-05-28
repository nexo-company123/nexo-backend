package pl.edu.uj.tp.nexo.issue.service;

import org.junit.jupiter.api.Test;
import pl.edu.uj.tp.nexo.board.entity.Board;
import pl.edu.uj.tp.nexo.board.entity.Stage;
import pl.edu.uj.tp.nexo.board.entity.StageType;
import pl.edu.uj.tp.nexo.board.repository.BoardRepository;
import pl.edu.uj.tp.nexo.board.repository.StageRepository;
import pl.edu.uj.tp.nexo.board.service.BoardNotFoundException;
import pl.edu.uj.tp.nexo.exception.AppException;
import pl.edu.uj.tp.nexo.exception.ErrorInfo;
import pl.edu.uj.tp.nexo.issue.dto.CreateIssueRequest;
import pl.edu.uj.tp.nexo.issue.dto.IssueResponse;
import pl.edu.uj.tp.nexo.issue.dto.UpdateIssueRequest;
import pl.edu.uj.tp.nexo.issue.entity.Issue;
import pl.edu.uj.tp.nexo.issue.entity.IssueType;
import pl.edu.uj.tp.nexo.issue.entity.Priority;
import pl.edu.uj.tp.nexo.issue.repository.IssueRepository;
import pl.edu.uj.tp.nexo.organization.entity.Organization;
import pl.edu.uj.tp.nexo.organization.repository.OrganizationRepository;
import pl.edu.uj.tp.nexo.organization.service.OrganizationNotFoundException;
import pl.edu.uj.tp.nexo.user.entity.Role;
import pl.edu.uj.tp.nexo.user.entity.User;
import pl.edu.uj.tp.nexo.user.repository.UserRepository;
import pl.edu.uj.tp.nexo.user.service.UserNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IssueServiceTest {

    private final IssueRepository issueRepository = mock(IssueRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final BoardRepository boardRepository = mock(BoardRepository.class);
    private final StageRepository stageRepository = mock(StageRepository.class);
    private final OrganizationRepository organizationRepository = mock(OrganizationRepository.class);

    private final IssueService issueService = new IssueService(
            issueRepository,
            userRepository,
            boardRepository,
            stageRepository,
            organizationRepository
    );

    private Organization makeOrg(Long id) {
        Organization org = new Organization();
        org.setId(id);
        org.setName("Nexo Org");
        return org;
    }

    private User makeUser(Long id, Organization org) {
        return User.builder()
                .id(id)
                .email("user" + id + "@example.com")
                .firstName("Anna")
                .lastName("Nowak")
                .password("pw")
                .role(Role.USER)
                .organization(org)
                .build();
    }

    private Stage makeStage(Long id, StageType type) {
        Board board = Board.builder().id(1L).name("B").build();
        return Stage.builder()
                .id(id)
                .name(type.name())
                .type(type)
                .isActive(true)
                .board(board)
                .build();
    }

    private CreateIssueRequest validCreateRequest(Long orgId) {
        CreateIssueRequest request = new CreateIssueRequest();
        request.setTitle("Fix login");
        request.setDescription("Login is broken");
        request.setAcceptanceCriteria("User can log in");
        request.setReporterId(1L);
        request.setAssigneeId(null);
        request.setStoryPoints(5);
        request.setPriority(Priority.HIGH);
        request.setType(IssueType.TASK);
        request.setEpicId(null);
        request.setBoardId(7L);
        request.setStageId(70L);
        request.setOrganizationId(orgId);
        return request;
    }

    @Test
    void getIssuesByOrganization_whenOrgDoesNotExist_throwsOrganizationNotFoundException() {
        when(organizationRepository.existsById(99L)).thenReturn(false);

        assertThrows(OrganizationNotFoundException.class,
                () -> issueService.getIssuesByOrganization(99L));

        verify(issueRepository, never()).findAllByOrganizationId(any());
    }

    @Test
    void getIssueByIdAndOrganization_whenNotFound_throwsIssueNotFoundException() {
        when(issueRepository.findByIdAndOrganizationId(5L, 10L)).thenReturn(Optional.empty());

        assertThrows(IssueNotFoundException.class,
                () -> issueService.getIssueByIdAndOrganization(5L, 10L));
    }

    @Test
    void createIssue_whenReporterDoesNotExist_throwsUserNotFoundException() {
        CreateIssueRequest request = validCreateRequest(10L);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> issueService.createIssue(request, 10L));
    }

    @Test
    void createIssue_whenBoardDoesNotExist_throwsBoardNotFoundException() {
        Organization org = makeOrg(10L);
        User reporter = makeUser(1L, org);

        CreateIssueRequest request = validCreateRequest(10L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(reporter));
        when(boardRepository.findById(7L)).thenReturn(Optional.empty());

        assertThrows(BoardNotFoundException.class,
                () -> issueService.createIssue(request, 10L));
    }

    @Test
    void createIssue_epicOnNonAllowedStage_throwsAppException() {
        Organization org = makeOrg(10L);
        User reporter = makeUser(1L, org);
        Board board = Board.builder().id(7L).name("B").organization(org).build();
        Stage stage = makeStage(70L, StageType.CODE_REVIEW);

        CreateIssueRequest request = validCreateRequest(10L);
        request.setType(IssueType.EPIC);

        when(userRepository.findById(1L)).thenReturn(Optional.of(reporter));
        when(boardRepository.findById(7L)).thenReturn(Optional.of(board));
        when(stageRepository.findById(70L)).thenReturn(Optional.of(stage));
        when(organizationRepository.findById(10L)).thenReturn(Optional.of(org));

        AppException ex = assertThrows(AppException.class,
                () -> issueService.createIssue(request, 10L));
        assertEquals(ErrorInfo.EPIC_INVALID_STAGE, ex.getErrorInfo());
    }

    @Test
    void createIssue_validRequest_savesIssueAndReturnsResponse() {
        Organization org = makeOrg(10L);
        User reporter = makeUser(1L, org);
        Board board = Board.builder().id(7L).name("B").organization(org).build();
        Stage stage = makeStage(70L, StageType.TO_DO);

        CreateIssueRequest request = validCreateRequest(10L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(reporter));
        when(boardRepository.findById(7L)).thenReturn(Optional.of(board));
        when(stageRepository.findById(70L)).thenReturn(Optional.of(stage));
        when(organizationRepository.findById(10L)).thenReturn(Optional.of(org));
        when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> {
            Issue issue = invocation.getArgument(0);
            issue.setId(123L);
            return issue;
        });

        IssueResponse response = issueService.createIssue(request, 10L);

        assertEquals(123L, response.getId());
        assertEquals("Fix login", response.getTitle());
        assertEquals(Priority.HIGH, response.getPriority());
        assertEquals(IssueType.TASK, response.getType());
        assertEquals(7L, response.getBoardId());
        assertEquals(70L, response.getStageId());
        assertEquals(10L, response.getOrganizationId());
    }

    @Test
    void updateIssue_whenIssueNotInOrg_throwsIssueNotFoundException() {
        when(issueRepository.findByIdAndOrganizationId(1L, 10L)).thenReturn(Optional.empty());

        UpdateIssueRequest request = UpdateIssueRequest.builder().title("X").build();

        assertThrows(IssueNotFoundException.class,
                () -> issueService.updateIssue(1L, request, 10L));
    }

    @Test
    void deleteIssue_whenIssueExists_deletesIt() {
        Organization org = makeOrg(10L);
        Issue issue = Issue.builder().id(1L).title("T").priority(Priority.LOW).type(IssueType.TASK).organization(org).build();

        when(issueRepository.findByIdAndOrganizationId(1L, 10L)).thenReturn(Optional.of(issue));

        issueService.deleteIssue(1L, 10L);

        verify(issueRepository).delete(issue);
    }

    @Test
    void getIssuesByEpic_whenIssueIsNotEpic_throwsAppExceptionInvalidEpicType() {
        Organization org = makeOrg(10L);
        Issue notEpic = Issue.builder()
                .id(1L)
                .title("Just a task")
                .priority(Priority.MEDIUM)
                .type(IssueType.TASK)
                .organization(org)
                .build();

        when(issueRepository.findByIdAndOrganizationId(1L, 10L)).thenReturn(Optional.of(notEpic));

        AppException ex = assertThrows(AppException.class,
                () -> issueService.getIssuesByEpic(1L, 10L));
        assertEquals(ErrorInfo.INVALID_EPIC_TYPE, ex.getErrorInfo());
    }

    @Test
    void getIssuesByEpic_whenIssueIsEpic_returnsAllLinkedIssues() {
        Organization org = makeOrg(10L);
        Issue epic = Issue.builder()
                .id(1L)
                .title("Epic 1")
                .priority(Priority.HIGH)
                .type(IssueType.EPIC)
                .organization(org)
                .build();

        Issue linked = Issue.builder()
                .id(2L)
                .title("Linked task")
                .priority(Priority.LOW)
                .type(IssueType.TASK)
                .organization(org)
                .epic(epic)
                .build();

        when(issueRepository.findByIdAndOrganizationId(1L, 10L)).thenReturn(Optional.of(epic));
        when(issueRepository.findAllByEpicIdAndOrganizationId(1L, 10L)).thenReturn(List.of(linked));

        List<IssueResponse> responses = issueService.getIssuesByEpic(1L, 10L);

        assertEquals(1, responses.size());
        assertEquals(2L, responses.get(0).getId());
        assertEquals(1L, responses.get(0).getEpicId());
    }
}
