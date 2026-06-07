package com.projectmanagementsaas.board.repository;

import com.projectmanagementsaas.board.entity.Board;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<Board, UUID> {
    List<Board> findByProject_IdAndDeletedAtIsNullOrderByCreatedAtAsc(UUID projectId);

    Optional<Board> findByIdAndDeletedAtIsNull(UUID id);
}
