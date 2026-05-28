package pl.edu.uj.tp.nexo.board.controller;

import org.junit.jupiter.api.Test;
import pl.edu.uj.tp.nexo.board.dto.BoardResponse;
import pl.edu.uj.tp.nexo.board.dto.CreateBoardRequest;
import pl.edu.uj.tp.nexo.board.dto.UpdateStageRequest;
import pl.edu.uj.tp.nexo.board.service.BoardService;
import pl.edu.uj.tp.nexo.organization.entity.Organization;
import pl.edu.uj.tp.nexo.user.entity.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class BoardControllerTest {

    private final BoardService boardService = mock(BoardService.class);
    private final BoardController boardController = new BoardController(boardService);

    private User makeUser(Long organizationId) {
        Organization organization = new Organization();
        organization.setId(organizationId);
        User user = new User();
        user.setOrganization(organization);
        return user;
    }

    @Test
    void getBoards_delegatesToServiceWithCurrentUserOrganization() {
        User user = makeUser(10L);
        List<BoardResponse> expected = List.of(BoardResponse.builder().id(1L).name("B").build());

        when(boardService.getBoardsByOrganization(10L)).thenReturn(expected);

        List<BoardResponse> result = boardController.getBoards(user);

        assertSame(expected, result);
        verify(boardService).getBoardsByOrganization(10L);
        verifyNoMoreInteractions(boardService);
    }

    @Test
    void getBoardById_delegatesToService() {
        User user = makeUser(10L);
        BoardResponse expected = BoardResponse.builder().id(5L).name("X").build();

        when(boardService.getBoardByIdAndOrganization(5L, 10L)).thenReturn(expected);

        BoardResponse result = boardController.getBoardById(5L, user);

        assertSame(expected, result);
    }

    @Test
    void createBoard_delegatesToService() {
        User user = makeUser(10L);
        CreateBoardRequest request = CreateBoardRequest.builder().name("New").build();
        BoardResponse expected = BoardResponse.builder().id(7L).name("New").build();

        when(boardService.createBoard(request, 10L)).thenReturn(expected);

        BoardResponse result = boardController.createBoard(request, user);

        assertSame(expected, result);
        verify(boardService).createBoard(request, 10L);
    }

    @Test
    void updateStage_passesAllParametersToService() {
        User user = makeUser(10L);
        UpdateStageRequest request = new UpdateStageRequest("New stage name", true);
        BoardResponse expected = BoardResponse.builder().id(1L).build();

        when(boardService.updateStage(1L, 50L, request, 10L)).thenReturn(expected);

        BoardResponse result = boardController.updateStage(1L, 50L, request, user);

        assertSame(expected, result);
        verify(boardService).updateStage(1L, 50L, request, 10L);
    }

    @Test
    void addUserToBoard_passesAllParametersToService() {
        User user = makeUser(10L);
        BoardResponse expected = BoardResponse.builder().id(1L).build();

        when(boardService.addUserToBoard(1L, 5L, 10L)).thenReturn(expected);

        BoardResponse result = boardController.addUserToBoard(1L, 5L, user);

        assertEquals(expected, result);
        verify(boardService).addUserToBoard(1L, 5L, 10L);
    }
}
