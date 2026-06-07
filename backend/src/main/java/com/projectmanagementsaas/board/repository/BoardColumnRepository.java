package com.projectmanagementsaas.board.repository;

import com.projectmanagementsaas.board.entity.BoardColumn;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardColumnRepository extends JpaRepository<BoardColumn, UUID> {
    List<BoardColumn> findByBoard_IdOrderByPositionAsc(UUID boardId);

    Optional<BoardColumn> findByIdAndBoard_Id(UUID id, UUID boardId);
}
