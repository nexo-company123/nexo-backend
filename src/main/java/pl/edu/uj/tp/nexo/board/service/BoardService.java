package pl.edu.uj.tp.nexo.board.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.uj.tp.nexo.board.dto.BoardResponse;
import pl.edu.uj.tp.nexo.board.dto.CreateBoardRequest;
import pl.edu.uj.tp.nexo.board.dto.StageResponse;
import pl.edu.uj.tp.nexo.board.dto.UpdateStageRequest;
import pl.edu.uj.tp.nexo.board.entity.Board;
import pl.edu.uj.tp.nexo.board.entity.Stage;
import pl.edu.uj.tp.nexo.board.entity.StageType;
import pl.edu.uj.tp.nexo.board.repository.BoardRepository;
import pl.edu.uj.tp.nexo.board.repository.StageRepository;
import pl.edu.uj.tp.nexo.organization.repository.OrganizationRepository;
import pl.edu.uj.tp.nexo.organization.service.OrganizationNotFoundException;
import pl.edu.uj.tp.nexo.user.entity.User;
import pl.edu.uj.tp.nexo.user.repository.UserRepository;
import pl.edu.uj.tp.nexo.user.service.UserNotFoundException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final StageRepository stageRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;

    public List<BoardResponse> getBoardsByOrganization(Long organizationId) {
        if (!organizationRepository.existsById(organizationId)) {
            throw new OrganizationNotFoundException(organizationId);
        }
        return boardRepository.findAllByOrganizationId(organizationId).stream()
                .map(this::toBoardResponse)
                .collect(Collectors.toList());
    }

    public BoardResponse getBoardByIdAndOrganization(Long id, Long organizationId) {
        return boardRepository.findByIdAndOrganizationId(id, organizationId)
                .map(this::toBoardResponse)
                .orElseThrow(() -> new BoardNotFoundException(id));
    }

    @Transactional
    public BoardResponse createBoard(CreateBoardRequest request, Long organizationId) {
        var organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new OrganizationNotFoundException(organizationId));

        List<User> users = new ArrayList<>();
        if (request.getUserIds() != null) {
            // Security check: Only add users that actually belong to the authenticated user's organization
            users = userRepository.findAllById(request.getUserIds()).stream()
                    .filter(user -> user.getOrganization() != null && user.getOrganization().getId().equals(organizationId))
                    .collect(Collectors.toList());
        }

        Board board = Board.builder()
                .name(request.getName())
                .organization(organization)
                .users(users)
                .build();

        board = boardRepository.save(board);

        List<Stage> defaultStages = createDefaultStages(board);
        board.setStages(defaultStages);

        return toBoardResponse(board);
    }

    @Transactional
    public BoardResponse updateStage(Long boardId, Long stageId, UpdateStageRequest request, Long organizationId) {
        // Strictly fetch the board by organization ID
        var board = boardRepository.findByIdAndOrganizationId(boardId, organizationId)
                .orElseThrow(() -> new BoardNotFoundException(boardId));

        var stage = stageRepository.findById(stageId)
                .orElseThrow(() -> new StageNotFoundException(stageId));

        if (!stage.getBoard().getId().equals(board.getId())) {
            throw new IllegalArgumentException("Stage does not belong to the specified board");
        }

        if (request.getName() != null) {
            stage.setName(request.getName());
        }
        if (request.getIsActive() != null) {
            stage.setActive(request.getIsActive());
        }

        stageRepository.save(stage);
        return toBoardResponse(board);
    }

    @Transactional
    public BoardResponse addUserToBoard(Long boardId, Long userId, Long organizationId) {
        var board = boardRepository.findByIdAndOrganizationId(boardId, organizationId)
                .orElseThrow(() -> new BoardNotFoundException(boardId));

        var user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (user.getOrganization() == null || !user.getOrganization().getId().equals(organizationId)) {
            throw new UserNotFoundException(userId); // Treat it as not found for security purposes
        }

        if (!board.getUsers().contains(user)) {
            board.getUsers().add(user);
            boardRepository.save(board);
        }

        return toBoardResponse(board);
    }

    private List<Stage> createDefaultStages(Board board) {
        return Arrays.stream(StageType.values())
                .map(type -> Stage.builder()
                        .name(type.name().replace("_", " "))
                        .type(type)
                        .isActive(true)
                        .board(board)
                        .build())
                .map(stageRepository::save)
                .collect(Collectors.toList());
    }

    private BoardResponse toBoardResponse(Board board) {
        return BoardResponse.builder()
                .id(board.getId())
                .name(board.getName())
                .organizationId(board.getOrganization().getId())
                .userIds(board.getUsers().stream().map(User::getId).collect(Collectors.toList()))
                .stages(board.getStages().stream().map(this::toStageResponse).collect(Collectors.toList()))
                .createdAt(board.getCreatedAt())
                .build();
    }

    private StageResponse toStageResponse(Stage stage) {
        return StageResponse.builder()
                .id(stage.getId())
                .name(stage.getName())
                .type(stage.getType())
                .isActive(stage.isActive())
                .build();
    }
}