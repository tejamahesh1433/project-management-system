package com.projectmanagementsaas.board.repository;

import com.projectmanagementsaas.board.entity.BoardTask;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardTaskRepository extends JpaRepository<BoardTask, UUID> {
    List<BoardTask> findByColumn_IdOrderByPositionAsc(UUID columnId);

    List<BoardTask> findByBoard_IdAndTask_Id(UUID boardId, UUID taskId);

    Optional<BoardTask> findFirstByBoard_IdAndTask_Id(UUID boardId, UUID taskId);

    void deleteByColumn_Id(UUID columnId);
}
