package com.projectmanagementsaas.board.controller;

import com.projectmanagementsaas.auth.dto.MessageResponse;
import com.projectmanagementsaas.auth.security.AuthenticatedUser;
import com.projectmanagementsaas.board.dto.BoardColumnResponse;
import com.projectmanagementsaas.board.dto.BoardResponse;
import com.projectmanagementsaas.board.dto.CreateBoardColumnRequest;
import com.projectmanagementsaas.board.dto.CreateBoardRequest;
import com.projectmanagementsaas.board.dto.MoveTaskRequest;
import com.projectmanagementsaas.board.dto.UpdateBoardColumnRequest;
import com.projectmanagementsaas.board.dto.UpdateBoardRequest;
import com.projectmanagementsaas.board.service.BoardService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/boards")
public class BoardController {
    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    @PostMapping
    ResponseEntity<BoardResponse> createBoard(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody CreateBoardRequest request
    ) {
        return ResponseEntity.ok(boardService.createBoard(currentUser.id(), request));
    }

    @GetMapping
    ResponseEntity<List<BoardResponse>> listBoards(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam UUID projectId
    ) {
        return ResponseEntity.ok(boardService.listBoards(currentUser.id(), projectId));
    }

    @GetMapping("/{boardId}")
    ResponseEntity<BoardResponse> getBoard(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID boardId
    ) {
        return ResponseEntity.ok(boardService.getBoard(currentUser.id(), boardId));
    }

    @PutMapping("/{boardId}")
    ResponseEntity<BoardResponse> updateBoard(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID boardId,
            @Valid @RequestBody UpdateBoardRequest request
    ) {
        return ResponseEntity.ok(boardService.updateBoard(currentUser.id(), boardId, request));
    }

    @DeleteMapping("/{boardId}")
    ResponseEntity<MessageResponse> deleteBoard(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID boardId
    ) {
        boardService.deleteBoard(currentUser.id(), boardId);
        return ResponseEntity.ok(new MessageResponse("Board deleted"));
    }

    @PostMapping("/{boardId}/columns")
    ResponseEntity<BoardColumnResponse> createColumn(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID boardId,
            @Valid @RequestBody CreateBoardColumnRequest request
    ) {
        return ResponseEntity.ok(boardService.createColumn(currentUser.id(), boardId, request));
    }

    @PutMapping("/{boardId}/columns/{columnId}")
    ResponseEntity<BoardColumnResponse> updateColumn(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID boardId,
            @PathVariable UUID columnId,
            @Valid @RequestBody UpdateBoardColumnRequest request
    ) {
        return ResponseEntity.ok(boardService.updateColumn(currentUser.id(), boardId, columnId, request));
    }

    @DeleteMapping("/{boardId}/columns/{columnId}")
    ResponseEntity<MessageResponse> deleteColumn(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID boardId,
            @PathVariable UUID columnId
    ) {
        boardService.deleteColumn(currentUser.id(), boardId, columnId);
        return ResponseEntity.ok(new MessageResponse("Board column deleted"));
    }

    @PatchMapping("/{boardId}/tasks/move")
    ResponseEntity<BoardResponse> moveTask(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID boardId,
            @Valid @RequestBody MoveTaskRequest request
    ) {
        return ResponseEntity.ok(boardService.moveTask(currentUser.id(), boardId, request));
    }
}
