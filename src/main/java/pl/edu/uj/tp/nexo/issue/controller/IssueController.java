package pl.edu.uj.tp.nexo.issue.controller;

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
import pl.edu.uj.tp.nexo.exception.ErrorInfo;
import pl.edu.uj.tp.nexo.exception.annotation.ApiErrors;
import pl.edu.uj.tp.nexo.issue.dto.CreateIssueRequest;
import pl.edu.uj.tp.nexo.issue.dto.IssueResponse;
import pl.edu.uj.tp.nexo.issue.dto.UpdateIssueRequest;
import pl.edu.uj.tp.nexo.issue.service.IssueService;
import pl.edu.uj.tp.nexo.user.entity.User;

import java.util.List;

@RestController
@RequestMapping("/issues")
@RequiredArgsConstructor
@Tag(name = "IssueController", description = "Issue and task management endpoints")
public class IssueController {

    private final IssueService issueService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Search and filter issues", description = "Retrieves issues strictly for the authenticated user's organization.")
    @ApiErrors({
            ErrorInfo.INVALID_AUTH_TOKEN,
            ErrorInfo.USER_HAS_UNAUTHORIZED_ROLE
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of issues retrieved successfully")
    })
    public List<IssueResponse> getIssues(
            @RequestParam(required = false) Long boardId,
            @RequestParam(required = false) Long stageId,
            @RequestParam(required = false) Long assigneeId,
            @RequestParam(required = false) String search,
            @AuthenticationPrincipal User currentUser
    ) {
        return issueService.searchIssues(currentUser.getOrganization().getId(), boardId, stageId, assigneeId, search);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Get issue by ID")
    @ApiErrors({
            ErrorInfo.ISSUE_NOT_FOUND,
            ErrorInfo.INVALID_AUTH_TOKEN,
            ErrorInfo.USER_HAS_UNAUTHORIZED_ROLE
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Issue retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = IssueResponse.class)))
    })
    public IssueResponse getIssueById(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        return issueService.getIssueByIdAndOrganization(id, currentUser.getOrganization().getId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Create a new issue")
    @ApiErrors({
            ErrorInfo.USER_NOT_FOUND,
            ErrorInfo.BOARD_NOT_FOUND,
            ErrorInfo.STAGE_NOT_FOUND,
            ErrorInfo.ORGANIZATION_NOT_FOUND,
            ErrorInfo.ISSUE_NOT_FOUND,
            ErrorInfo.INVALID_AUTH_TOKEN,
            ErrorInfo.USER_HAS_UNAUTHORIZED_ROLE
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Issue created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = IssueResponse.class)))
    })
    public IssueResponse createIssue(@RequestBody CreateIssueRequest request, @AuthenticationPrincipal User currentUser) {
        return issueService.createIssue(request, currentUser.getOrganization().getId());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Update an issue")
    @ApiErrors({
            ErrorInfo.ISSUE_NOT_FOUND,
            ErrorInfo.USER_NOT_FOUND,
            ErrorInfo.STAGE_NOT_FOUND,
            ErrorInfo.INVALID_AUTH_TOKEN,
            ErrorInfo.USER_HAS_UNAUTHORIZED_ROLE
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Issue updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = IssueResponse.class)))
    })
    public IssueResponse updateIssue(@PathVariable Long id, @RequestBody UpdateIssueRequest request, @AuthenticationPrincipal User currentUser) {
        return issueService.updateIssue(id, request, currentUser.getOrganization().getId());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Delete an issue")
    @ApiErrors({
            ErrorInfo.ISSUE_NOT_FOUND,
            ErrorInfo.INVALID_AUTH_TOKEN,
            ErrorInfo.USER_HAS_UNAUTHORIZED_ROLE
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Issue deleted successfully")
    })
    public void deleteIssue(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        issueService.deleteIssue(id, currentUser.getOrganization().getId());
    }
}