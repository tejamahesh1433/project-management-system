package com.projectmanagementsaas.board.mapper;

import com.projectmanagementsaas.board.dto.BoardColumnResponse;
import com.projectmanagementsaas.board.dto.BoardResponse;
import com.projectmanagementsaas.board.dto.BoardTaskResponse;
import com.projectmanagementsaas.board.entity.Board;
import com.projectmanagementsaas.board.entity.BoardColumn;
import com.projectmanagementsaas.board.entity.BoardTask;
import com.projectmanagementsaas.board.repository.BoardColumnRepository;
import com.projectmanagementsaas.board.repository.BoardTaskRepository;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class BoardMapper {
    private final BoardColumnRepository boardColumnRepository;
    private final BoardTaskRepository boardTaskRepository;

    public BoardMapper(BoardColumnRepository boardColumnRepository, BoardTaskRepository boardTaskRepository) {
        this.boardColumnRepository = boardColumnRepository;
        this.boardTaskRepository = boardTaskRepository;
    }

    public BoardResponse toBoardResponse(Board board) {
        List<BoardColumnResponse> columns = boardColumnRepository.findByBoard_IdOrderByPositionAsc(board.getId()).stream()
                .map(this::toColumnResponse)
                .toList();
        return new BoardResponse(
                board.getId(),
                board.getProject().getId(),
                board.getName(),
                board.getTemplate(),
                columns,
                board.getCreatedAt(),
                board.getUpdatedAt());
    }

    public BoardColumnResponse toColumnResponse(BoardColumn column) {
        List<BoardTaskResponse> tasks = boardTaskRepository.findByColumn_IdOrderByPositionAsc(column.getId()).stream()
                .map(this::toBoardTaskResponse)
                .toList();
        return new BoardColumnResponse(column.getId(), column.getBoard().getId(), column.getName(), column.getPosition(), tasks);
    }

    public BoardTaskResponse toBoardTaskResponse(BoardTask boardTask) {
        return new BoardTaskResponse(
                boardTask.getId(),
                boardTask.getTask().getId(),
                boardTask.getTask().getTitle(),
                boardTask.getColumn().getId(),
                boardTask.getPosition());
    }
}
