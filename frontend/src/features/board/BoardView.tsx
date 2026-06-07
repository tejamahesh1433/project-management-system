"use client";

import { DndContext, type DragEndEvent } from "@dnd-kit/core";
import { SortableContext, verticalListSortingStrategy } from "@dnd-kit/sortable";

type BoardTask = {
  id: string;
  title: string;
};

type BoardColumn = {
  id: string;
  name: string;
  tasks: BoardTask[];
};

type BoardViewProps = {
  columns: BoardColumn[];
  onMoveTask?: (taskId: string, columnId: string, position: number) => void;
};

export function BoardView({ columns, onMoveTask }: BoardViewProps) {
  function handleDragEnd(event: DragEndEvent) {
    const taskId = String(event.active.id);
    const columnId = event.over?.data.current?.columnId;
    const position = event.over?.data.current?.position;

    if (columnId && typeof position === "number") {
      onMoveTask?.(taskId, columnId, position);
    }
  }

  return (
    <DndContext onDragEnd={handleDragEnd}>
      <div>
        {columns.map((column) => (
          <section key={column.id}>
            <h2>{column.name}</h2>
            <SortableContext items={column.tasks.map((task) => task.id)} strategy={verticalListSortingStrategy}>
              {column.tasks.map((task) => (
                <article key={task.id}>{task.title}</article>
              ))}
            </SortableContext>
          </section>
        ))}
      </div>
    </DndContext>
  );
}
