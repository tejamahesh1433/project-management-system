package com.projectmanagementsaas.board.service;

import com.projectmanagementsaas.board.dto.BoardColumnResponse;
import com.projectmanagementsaas.board.dto.BoardResponse;
import com.projectmanagementsaas.board.dto.CreateBoardColumnRequest;
import com.projectmanagementsaas.board.dto.CreateBoardRequest;
import com.projectmanagementsaas.board.dto.MoveTaskRequest;
import com.projectmanagementsaas.board.dto.UpdateBoardColumnRequest;
import com.projectmanagementsaas.board.dto.UpdateBoardRequest;
import com.projectmanagementsaas.board.entity.Board;
import com.projectmanagementsaas.board.entity.BoardColumn;
import com.projectmanagementsaas.board.entity.BoardTask;
import com.projectmanagementsaas.board.entity.BoardTemplate;
import com.projectmanagementsaas.board.mapper.BoardMapper;
import com.projectmanagementsaas.board.repository.BoardColumnRepository;
import com.projectmanagementsaas.board.repository.BoardRepository;
import com.projectmanagementsaas.board.repository.BoardTaskRepository;
import com.projectmanagementsaas.board.validator.BoardTaskValidator;
import com.projectmanagementsaas.common.exception.BadRequestException;
import com.projectmanagementsaas.common.exception.NotFoundException;
import com.projectmanagementsaas.events.model.BoardColumnCreatedEvent;
import com.projectmanagementsaas.events.model.BoardColumnDeletedEvent;
import com.projectmanagementsaas.events.model.BoardColumnUpdatedEvent;
import com.projectmanagementsaas.events.model.BoardCreatedEvent;
import com.projectmanagementsaas.events.model.BoardDeletedEvent;
import com.projectmanagementsaas.events.model.BoardTaskMovedEvent;
import com.projectmanagementsaas.events.model.BoardUpdatedEvent;
import com.projectmanagementsaas.project.entity.Project;
import com.projectmanagementsaas.project.repository.ProjectRepository;
import com.projectmanagementsaas.task.entity.Task;
import com.projectmanagementsaas.task.service.TaskAccessService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BoardService {
    private final BoardRepository boardRepository;
    private final BoardColumnRepository boardColumnRepository;
    private final BoardTaskRepository boardTaskRepository;
    private final ProjectRepository projectRepository;
    private final TaskAccessService taskAccessService;
    private final BoardTaskValidator boardTaskValidator;
    private final BoardMapper boardMapper;
    private final ApplicationEventPublisher eventPublisher;

    public BoardService(
            BoardRepository boardRepository,
            BoardColumnRepository boardColumnRepository,
            BoardTaskRepository boardTaskRepository,
            ProjectRepository projectRepository,
            TaskAccessService taskAccessService,
            BoardTaskValidator boardTaskValidator,
            BoardMapper boardMapper,
            ApplicationEventPublisher eventPublisher
    ) {
        this.boardRepository = boardRepository;
        this.boardColumnRepository = boardColumnRepository;
        this.boardTaskRepository = boardTaskRepository;
        this.projectRepository = projectRepository;
        this.taskAccessService = taskAccessService;
        this.boardTaskValidator = boardTaskValidator;
        this.boardMapper = boardMapper;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public BoardResponse createBoard(UUID currentUserId, CreateBoardRequest request) {
        Project project = projectRepository.findByIdAndDeletedAtIsNull(request.projectId())
                .orElseThrow(() -> new NotFoundException("Project not found"));
        taskAccessService.requireManager(project.getId(), currentUserId);
        Board board = new Board();
        board.setProject(project);
        board.setName(request.name().trim());
        board.setTemplate(request.template());
        Board savedBoard = boardRepository.save(board);
        createTemplateColumns(savedBoard);
        eventPublisher.publishEvent(new BoardCreatedEvent(savedBoard.getId(), project.getId(), currentUserId));
        return boardMapper.toBoardResponse(savedBoard);
    }

    @Transactional(readOnly = true)
    public List<BoardResponse> listBoards(UUID currentUserId, UUID projectId) {
        taskAccessService.requireReadableTaskForProject(projectId, currentUserId);
        return boardRepository.findByProject_IdAndDeletedAtIsNullOrderByCreatedAtAsc(projectId).stream()
                .map(boardMapper::toBoardResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public BoardResponse getBoard(UUID currentUserId, UUID boardId) {
        Board board = requireBoard(boardId);
        taskAccessService.requireReadableTaskForProject(board.getProject().getId(), currentUserId);
        return boardMapper.toBoardResponse(board);
    }

    @Transactional
    public BoardResponse updateBoard(UUID currentUserId, UUID boardId, UpdateBoardRequest request) {
        Board board = requireBoard(boardId);
        taskAccessService.requireManager(board.getProject().getId(), currentUserId);
        board.setName(request.name().trim());
        board.setUpdatedAt(Instant.now());
        Board savedBoard = boardRepository.save(board);
        eventPublisher.publishEvent(new BoardUpdatedEvent(boardId, board.getProject().getId(), currentUserId));
        return boardMapper.toBoardResponse(savedBoard);
    }

    @Transactional
    public void deleteBoard(UUID currentUserId, UUID boardId) {
        Board board = requireBoard(boardId);
        taskAccessService.requireManager(board.getProject().getId(), currentUserId);
        board.softDelete();
        boardRepository.save(board);
        eventPublisher.publishEvent(new BoardDeletedEvent(boardId, board.getProject().getId(), currentUserId));
    }

    @Transactional
    public BoardColumnResponse createColumn(UUID currentUserId, UUID boardId, CreateBoardColumnRequest request) {
        Board board = requireBoard(boardId);
        taskAccessService.requireManager(board.getProject().getId(), currentUserId);
        BoardColumn column = new BoardColumn();
        column.setBoard(board);
        column.setName(request.name().trim());
        column.setPosition(request.position());
        BoardColumn savedColumn = boardColumnRepository.save(column);
        compactColumnPositions(boardId);
        eventPublisher.publishEvent(new BoardColumnCreatedEvent(savedColumn.getId(), boardId, currentUserId));
        return boardMapper.toColumnResponse(savedColumn);
    }

    @Transactional
    public BoardColumnResponse updateColumn(UUID currentUserId, UUID boardId, UUID columnId, UpdateBoardColumnRequest request) {
        Board board = requireBoard(boardId);
        taskAccessService.requireManager(board.getProject().getId(), currentUserId);
        BoardColumn column = requireColumn(boardId, columnId);
        column.setName(request.name().trim());
        column.setPosition(request.position());
        column.setUpdatedAt(Instant.now());
        BoardColumn savedColumn = boardColumnRepository.save(column);
        compactColumnPositions(boardId);
        eventPublisher.publishEvent(new BoardColumnUpdatedEvent(columnId, boardId, currentUserId));
        return boardMapper.toColumnResponse(savedColumn);
    }

    @Transactional
    public void deleteColumn(UUID currentUserId, UUID boardId, UUID columnId) {
        Board board = requireBoard(boardId);
        taskAccessService.requireManager(board.getProject().getId(), currentUserId);
        boardTaskRepository.deleteByColumn_Id(columnId);
        boardColumnRepository.delete(requireColumn(boardId, columnId));
        compactColumnPositions(boardId);
        eventPublisher.publishEvent(new BoardColumnDeletedEvent(columnId, boardId, currentUserId));
    }

    @Transactional
    public BoardResponse moveTask(UUID currentUserId, UUID boardId, MoveTaskRequest request) {
        Board board = requireBoard(boardId);
        taskAccessService.requireContributor(board.getProject().getId(), currentUserId);
        BoardColumn targetColumn = requireColumn(boardId, request.columnId());
        Task task = taskAccessService.requireTask(request.taskId());
        boardTaskValidator.validateTaskBelongsToBoardProject(board, task);
        boardTaskValidator.validateColumnBelongsToBoard(board, targetColumn);

        boardTaskRepository.findByBoard_IdAndTask_Id(boardId, task.getId()).stream()
                .filter(boardTask -> !boardTask.getColumn().getId().equals(targetColumn.getId()))
                .forEach(boardTaskRepository::delete);

        BoardTask boardTask = boardTaskRepository.findFirstByBoard_IdAndTask_Id(boardId, task.getId())
                .orElseGet(() -> {
                    BoardTask created = new BoardTask();
                    created.setBoard(board);
                    created.setTask(task);
                    return created;
                });
        boardTask.setColumn(targetColumn);
        boardTask.setPosition(request.position());
        boardTask.setUpdatedAt(Instant.now());
        boardTaskRepository.save(boardTask);
        compactTaskPositions(targetColumn.getId());
        eventPublisher.publishEvent(new BoardTaskMovedEvent(boardId, task.getId(), targetColumn.getId(), request.position(), currentUserId));
        return boardMapper.toBoardResponse(board);
    }

    private void createTemplateColumns(Board board) {
        List<String> names = board.getTemplate() == BoardTemplate.SCRUM
                ? List.of("Backlog", "Selected", "In Progress", "Done")
                : List.of("To Do", "In Progress", "Done");
        for (int index = 0; index < names.size(); index++) {
            BoardColumn column = new BoardColumn();
            column.setBoard(board);
            column.setName(names.get(index));
            column.setPosition(index);
            boardColumnRepository.save(column);
        }
    }

    private Board requireBoard(UUID boardId) {
        return boardRepository.findByIdAndDeletedAtIsNull(boardId)
                .orElseThrow(() -> new NotFoundException("Board not found"));
    }

    private BoardColumn requireColumn(UUID boardId, UUID columnId) {
        return boardColumnRepository.findByIdAndBoard_Id(columnId, boardId)
                .orElseThrow(() -> new NotFoundException("Board column not found"));
    }

    private void compactColumnPositions(UUID boardId) {
        List<BoardColumn> columns = boardColumnRepository.findByBoard_IdOrderByPositionAsc(boardId);
        for (int index = 0; index < columns.size(); index++) {
            columns.get(index).setPosition(index);
        }
        boardColumnRepository.saveAll(columns);
    }

    private void compactTaskPositions(UUID columnId) {
        List<BoardTask> tasks = boardTaskRepository.findByColumn_IdOrderByPositionAsc(columnId);
        for (int index = 0; index < tasks.size(); index++) {
            tasks.get(index).setPosition(index);
        }
        boardTaskRepository.saveAll(tasks);
    }
}
