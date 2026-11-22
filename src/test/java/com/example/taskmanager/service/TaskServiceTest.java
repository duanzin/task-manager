package com.example.taskmanager.service;

import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.TaskStatus;
import com.example.taskmanager.repository.TaskRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository repository;

    @InjectMocks
    private TaskService service;

    @Test
    void shouldCreateTaskWithPendingStatus() {
        Task task = new Task(
                "Estudar Java",
                "Revisar conceitos",
                null,
                null
        );

        when(repository.save(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Task result = service.create(task);

        assertEquals(TaskStatus.PENDING, result.getStatus());
        verify(repository).save(task);
    }

    @Test
    void shouldFindAllTasks() {
        Task first = new Task("Tarefa 1", "Desc 1", TaskStatus.PENDING, LocalDate.now());
        Task second = new Task("Tarefa 2", "Desc 2", TaskStatus.IN_PROGRESS, LocalDate.now());

        when(repository.findAll()).thenReturn(List.of(first, second));

        List<Task> result = service.findAll();

        assertEquals(2, result.size());
        verify(repository).findAll();
    }

    @Test
    void shouldFindExistingTaskById() {
        Task task = new Task("Estudar testes", "JUnit", TaskStatus.PENDING, LocalDate.now());

        when(repository.findById(1L)).thenReturn(Optional.of(task));

        Task result = service.findById(1L);

        assertEquals("Estudar testes", result.getTitle());
        verify(repository).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenTaskDoesNotExist() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> service.findById(99L)
        );

        assertEquals("Tarefa não encontrada", exception.getMessage());
    }

    @Test
    void shouldUpdateTaskData() {
        Task existing = new Task(
                "Título antigo",
                "Descrição antiga",
                TaskStatus.PENDING,
                LocalDate.of(2026, 11, 20)
        );
        Task updated = new Task(
                "Título novo",
                "Descrição nova",
                TaskStatus.COMPLETED,
                LocalDate.of(2026, 11, 25)
        );

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Task result = service.update(1L, updated);

        assertEquals("Título novo", result.getTitle());
        assertEquals("Descrição nova", result.getDescription());
        assertEquals(LocalDate.of(2026, 11, 25), result.getDueDate());
        assertEquals(TaskStatus.PENDING, result.getStatus());
        verify(repository).save(existing);
    }

    @Test
    void shouldUpdateTaskStatus() {
        Task existing = new Task(
                "Estudar arquitetura",
                "Revisar camadas",
                TaskStatus.PENDING,
                LocalDate.now()
        );

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Task result = service.updateStatus(1L, TaskStatus.COMPLETED);

        assertEquals(TaskStatus.COMPLETED, result.getStatus());
        verify(repository).save(existing);
    }

    @Test
    void shouldDeleteTask() {
        Task existing = new Task(
                "Tarefa a excluir",
                "Será removida",
                TaskStatus.PENDING,
                LocalDate.now()
        );

        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        service.delete(1L);

        verify(repository).delete(existing);
    }
}
