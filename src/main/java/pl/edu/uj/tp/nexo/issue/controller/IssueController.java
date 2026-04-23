package pl.edu.uj.tp.nexo.issue.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
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
    public IssueResponse getIssueById(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        return issueService.getIssueByIdAndOrganization(id, currentUser.getOrganization().getId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Create a new issue")
    public IssueResponse createIssue(@RequestBody CreateIssueRequest request, @AuthenticationPrincipal User currentUser) {
        return issueService.createIssue(request, currentUser.getOrganization().getId());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Update an issue")
    public IssueResponse updateIssue(@PathVariable Long id, @RequestBody UpdateIssueRequest request, @AuthenticationPrincipal User currentUser) {
        return issueService.updateIssue(id, request, currentUser.getOrganization().getId());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Delete an issue")
    public void deleteIssue(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        issueService.deleteIssue(id, currentUser.getOrganization().getId());
    }
}