package pl.edu.uj.tp.nexo.board.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pl.edu.uj.tp.nexo.board.dto.BoardResponse;
import pl.edu.uj.tp.nexo.board.dto.CreateBoardRequest;
import pl.edu.uj.tp.nexo.board.dto.UpdateStageRequest;
import pl.edu.uj.tp.nexo.board.service.BoardService;
import pl.edu.uj.tp.nexo.exception.ErrorInfo;
import pl.edu.uj.tp.nexo.exception.annotation.ApiErrors;
import pl.edu.uj.tp.nexo.user.entity.User;

import java.util.List;

@RestController
@RequestMapping("/boards")
@RequiredArgsConstructor
@Tag(name = "Boards", description = "Board management endpoints")
public class BoardController {

    private final BoardService boardService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List boards for an organization", description = "Retrieves all boards belonging to the authenticated user's organization.")
    @ApiErrors({
            ErrorInfo.INVALID_AUTH_TOKEN,
            ErrorInfo.USER_HAS_UNAUTHORIZED_ROLE
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of boards retrieved successfully")
    })
    public List<BoardResponse> getBoards(@AuthenticationPrincipal User currentUser) {
        return boardService.getBoardsByOrganization(currentUser.getOrganization().getId());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get detailed board info", description = "Retrieves information about a specific board. Strictly checks if the board belongs to the user's organization.")
    @ApiErrors({
            ErrorInfo.BOARD_NOT_FOUND,
            ErrorInfo.INVALID_AUTH_TOKEN,
            ErrorInfo.USER_HAS_UNAUTHORIZED_ROLE
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Board retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BoardResponse.class)))
    })
    public BoardResponse getBoardById(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        return boardService.getBoardByIdAndOrganization(id, currentUser.getOrganization().getId());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new board", description = "Creates a new board linked directly to the authenticated user's organization.")
    @ApiErrors({
            ErrorInfo.ORGANIZATION_NOT_FOUND,
            ErrorInfo.INVALID_AUTH_TOKEN,
            ErrorInfo.USER_HAS_UNAUTHORIZED_ROLE
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Board created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BoardResponse.class)))
    })
    public BoardResponse createBoard(@RequestBody CreateBoardRequest request, @AuthenticationPrincipal User currentUser) {
        return boardService.createBoard(request, currentUser.getOrganization().getId());
    }

    @PutMapping("/{id}/stages/{stageId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update stage status (active/inactive)", description = "Updates a stage within a specific board.")
    @ApiErrors({
            ErrorInfo.BOARD_NOT_FOUND,
            ErrorInfo.STAGE_NOT_FOUND,
            ErrorInfo.INVALID_AUTH_TOKEN,
            ErrorInfo.USER_HAS_UNAUTHORIZED_ROLE
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stage status updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BoardResponse.class)))
    })
    public BoardResponse updateStage(
            @PathVariable Long id,
            @PathVariable Long stageId,
            @RequestBody UpdateStageRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        return boardService.updateStage(id, stageId, request, currentUser.getOrganization().getId());
    }

    @PostMapping("/{id}/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add a user to the board", description = "Assigns a user to a specific board.")
    @ApiErrors({
            ErrorInfo.BOARD_NOT_FOUND,
            ErrorInfo.USER_NOT_FOUND,
            ErrorInfo.INVALID_AUTH_TOKEN,
            ErrorInfo.USER_HAS_UNAUTHORIZED_ROLE
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User added to board successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BoardResponse.class)))
    })
    public BoardResponse addUserToBoard(@PathVariable Long id, @PathVariable Long userId, @AuthenticationPrincipal User currentUser) {
        return boardService.addUserToBoard(id, userId, currentUser.getOrganization().getId());
    }
}