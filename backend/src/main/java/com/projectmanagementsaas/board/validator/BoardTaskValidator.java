package com.projectmanagementsaas.board.validator;

import com.projectmanagementsaas.board.entity.Board;
import com.projectmanagementsaas.board.entity.BoardColumn;
import com.projectmanagementsaas.common.exception.BadRequestException;
import com.projectmanagementsaas.task.entity.Task;
import org.springframework.stereotype.Component;

@Component
public class BoardTaskValidator {
    public void validateTaskBelongsToBoardProject(Board board, Task task) {
        if (!task.getProject().getId().equals(board.getProject().getId())) {
            throw new BadRequestException("Task must belong to the board project");
        }
    }

    public void validateColumnBelongsToBoard(Board board, BoardColumn column) {
        if (!column.getBoard().getId().equals(board.getId())) {
            throw new BadRequestException("Column must belong to the board");
        }
    }
}
