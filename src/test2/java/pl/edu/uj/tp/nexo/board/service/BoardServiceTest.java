package pl.edu.uj.tp.nexo.board.service;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import pl.edu.uj.tp.nexo.board.dto.BoardResponse;
import pl.edu.uj.tp.nexo.board.dto.CreateBoardRequest;
import pl.edu.uj.tp.nexo.board.dto.UpdateStageRequest;
import pl.edu.uj.tp.nexo.board.entity.Board;
import pl.edu.uj.tp.nexo.board.entity.Stage;
import pl.edu.uj.tp.nexo.board.entity.StageType;
import pl.edu.uj.tp.nexo.board.repository.BoardRepository;
import pl.edu.uj.tp.nexo.board.repository.StageRepository;
import pl.edu.uj.tp.nexo.organization.entity.Organization;
import pl.edu.uj.tp.nexo.organization.repository.OrganizationRepository;
import pl.edu.uj.tp.nexo.organization.service.OrganizationNotFoundException;
import pl.edu.uj.tp.nexo.user.entity.Role;
import pl.edu.uj.tp.nexo.user.entity.User;
import pl.edu.uj.tp.nexo.user.repository.UserRepository;
import pl.edu.uj.tp.nexo.user.service.UserNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BoardServiceTest {

    private final BoardRepository boardRepository = mock(BoardRepository.class);
    private final StageRepository stageRepository = mock(StageRepository.class);
    private final OrganizationRepository organizationRepository = mock(OrganizationRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);

    private final BoardService boardService = new BoardService(
            boardRepository,
            stageRepository,
            organizationRepository,
            userRepository
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

    private Board makeBoard(Long id, Organization org) {
        Board board = Board.builder()
                .id(id)
                .name("Sprint board")
                .organization(org)
                .users(new ArrayList<>())
                .stages(new ArrayList<>())
                .build();
        return board;
    }

    @Test
    void getBoardsByOrganization_whenOrgDoesNotExist_throwsOrganizationNotFoundException() {
        when(organizationRepository.existsById(99L)).thenReturn(false);

        assertThrows(OrganizationNotFoundException.class,
                () -> boardService.getBoardsByOrganization(99L));

        verify(boardRepository, never()).findAllByOrganizationId(any());
    }

    @Test
    void getBoardByIdAndOrganization_whenNotFound_throwsBoardNotFoundException() {
        when(boardRepository.findByIdAndOrganizationId(5L, 10L)).thenReturn(Optional.empty());

        assertThrows(BoardNotFoundException.class,
                () -> boardService.getBoardByIdAndOrganization(5L, 10L));
    }

    @Test
    void createBoard_createsBoardAndDefaultStagesForEachStageType() {
        Organization org = makeOrg(10L);
        CreateBoardRequest request = CreateBoardRequest.builder()
                .name("New Board")
                .userIds(null)
                .build();

        when(organizationRepository.findById(10L)).thenReturn(Optional.of(org));
        when(boardRepository.save(any(Board.class))).thenAnswer(invocation -> {
            Board b = invocation.getArgument(0);
            b.setId(1L);
            return b;
        });
        when(stageRepository.save(any(Stage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BoardResponse response = boardService.createBoard(request, 10L);

        assertEquals("New Board", response.getName());
        assertEquals(10L, response.getOrganizationId());
        assertEquals(StageType.values().length, response.getStages().size());

        ArgumentCaptor<Stage> stageCaptor = ArgumentCaptor.forClass(Stage.class);
        verify(stageRepository, org.mockito.Mockito.times(StageType.values().length)).save(stageCaptor.capture());
        List<StageType> savedTypes = stageCaptor.getAllValues().stream().map(Stage::getType).toList();
        for (StageType type : StageType.values()) {
            assertTrue(savedTypes.contains(type));
        }
    }

    @Test
    void createBoard_filtersOutUsersFromOtherOrganizations() {
        Organization org = makeOrg(10L);
        Organization otherOrg = makeOrg(20L);

        User mineFirst = makeUser(1L, org);
        User mineSecond = makeUser(2L, org);
        User intruder = makeUser(3L, otherOrg);

        CreateBoardRequest request = CreateBoardRequest.builder()
                .name("Board with users")
                .userIds(List.of(1L, 2L, 3L))
                .build();

        when(organizationRepository.findById(10L)).thenReturn(Optional.of(org));
        when(userRepository.findAllById(List.of(1L, 2L, 3L)))
                .thenReturn(List.of(mineFirst, mineSecond, intruder));
        when(boardRepository.save(any(Board.class))).thenAnswer(invocation -> {
            Board b = invocation.getArgument(0);
            b.setId(7L);
            return b;
        });
        when(stageRepository.save(any(Stage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BoardResponse response = boardService.createBoard(request, 10L);

        assertEquals(2, response.getUserIds().size());
        assertTrue(response.getUserIds().contains(1L));
        assertTrue(response.getUserIds().contains(2L));
        assertFalse(response.getUserIds().contains(3L));
    }

    @Test
    void updateStage_updatesNameAndActiveFlag() {
        Organization org = makeOrg(10L);
        Board board = makeBoard(1L, org);

        Stage stage = Stage.builder()
                .id(50L)
                .name("TO DO")
                .type(StageType.TO_DO)
                .isActive(true)
                .board(board)
                .build();
        board.getStages().add(stage);

        UpdateStageRequest request = new UpdateStageRequest("Renamed stage", false);

        when(boardRepository.findByIdAndOrganizationId(1L, 10L)).thenReturn(Optional.of(board));
        when(stageRepository.findById(50L)).thenReturn(Optional.of(stage));

        boardService.updateStage(1L, 50L, request, 10L);

        assertEquals("Renamed stage", stage.getName());
        assertEquals(false, stage.isActive());
        verify(stageRepository).save(stage);
    }

    @Test
    void updateStage_whenStageBelongsToAnotherBoard_throwsIllegalArgument() {
        Organization org = makeOrg(10L);
        Board board = makeBoard(1L, org);
        Board otherBoard = makeBoard(2L, org);

        Stage stage = Stage.builder()
                .id(50L)
                .name("TO DO")
                .type(StageType.TO_DO)
                .isActive(true)
                .board(otherBoard)
                .build();

        when(boardRepository.findByIdAndOrganizationId(1L, 10L)).thenReturn(Optional.of(board));
        when(stageRepository.findById(50L)).thenReturn(Optional.of(stage));

        assertThrows(IllegalArgumentException.class,
                () -> boardService.updateStage(1L, 50L, new UpdateStageRequest("X", true), 10L));
    }

    @Test
    void addUserToBoard_whenUserBelongsToAnotherOrg_throwsUserNotFoundException() {
        Organization myOrg = makeOrg(10L);
        Organization otherOrg = makeOrg(20L);

        Board board = makeBoard(1L, myOrg);
        User intruder = makeUser(5L, otherOrg);

        when(boardRepository.findByIdAndOrganizationId(1L, 10L)).thenReturn(Optional.of(board));
        when(userRepository.findById(5L)).thenReturn(Optional.of(intruder));

        assertThrows(UserNotFoundException.class,
                () -> boardService.addUserToBoard(1L, 5L, 10L));

        verify(boardRepository, never()).save(any(Board.class));
    }

    @Test
    void addUserToBoard_addsUserOnlyOnceEvenIfCalledTwice() {
        Organization myOrg = makeOrg(10L);
        Board board = makeBoard(1L, myOrg);
        User user = makeUser(5L, myOrg);

        when(boardRepository.findByIdAndOrganizationId(1L, 10L)).thenReturn(Optional.of(board));
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));

        boardService.addUserToBoard(1L, 5L, 10L);
        boardService.addUserToBoard(1L, 5L, 10L);

        assertEquals(1, board.getUsers().size());
        verify(boardRepository, org.mockito.Mockito.times(1)).save(board);
    }
}
