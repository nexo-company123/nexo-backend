package pl.edu.uj.tp.nexo.issue.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.edu.uj.tp.nexo.issue.dto.CreateIssueRequest;
import pl.edu.uj.tp.nexo.issue.dto.IssueResponse;
import pl.edu.uj.tp.nexo.issue.dto.UpdateIssueRequest;
import pl.edu.uj.tp.nexo.issue.service.IssueService;

import java.util.List;

@RestController
@RequestMapping("/issues")
@RequiredArgsConstructor
@Tag(name = "IssueController", description = "Issue and task management endpoints")
public class IssueController {

    private final IssueService issueService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Search and filter issues", description = "Retrieves all issues. Supports optional filtering by organizationId, boardId, stageId, assigneeId, and searching by title.")
    public List<IssueResponse> getIssues(
            @RequestParam(required = false) Long organizationId,
            @RequestParam(required = false) Long boardId,
            @RequestParam(required = false) Long stageId,
            @RequestParam(required = false) Long assigneeId,
            @RequestParam(required = false) String search
    ) {
        return issueService.searchIssues(organizationId, boardId, stageId, assigneeId, search);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Get issue by ID", description = "Retrieves a specific issue by its ID.")
    public IssueResponse getIssueById(@PathVariable Long id) {
        return issueService.getIssueById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Create a new issue", description = "Creates a new issue/task in the system.")
    public IssueResponse createIssue(@RequestBody CreateIssueRequest request) {
        return issueService.createIssue(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Update an issue", description = "Updates details of an existing issue, such as priority, story points, or description.")
    public IssueResponse updateIssue(@PathVariable Long id, @RequestBody UpdateIssueRequest request) {
        return issueService.updateIssue(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Delete an issue", description = "Deletes an issue by its ID.")
    public void deleteIssue(@PathVariable Long id) {
        issueService.deleteIssue(id);
    }
}