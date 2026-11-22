package com.example.taskmanager.controller;

import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.TaskStatus;
import com.example.taskmanager.repository.TaskRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository repository;

    @BeforeEach
    void cleanDatabase() {
        repository.deleteAll();
    }

    @Test
    void shouldCreateTask() throws Exception {
        mockMvc.perform(post("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "title": "Estudar testes",
                        "description": "Praticar JUnit",
                        "dueDate": "2026-11-25"
                    }
                    """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Estudar testes"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void shouldFindAllTasks() throws Exception {
        repository.save(new Task("Tarefa 1", "Desc 1", TaskStatus.PENDING, LocalDate.now()));
        repository.save(new Task("Tarefa 2", "Desc 2", TaskStatus.IN_PROGRESS, LocalDate.now()));

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void shouldFindTaskById() throws Exception {
        Task saved = repository.save(new Task(
                "Consultar tarefa",
                "Buscar pelo id",
                TaskStatus.PENDING,
                LocalDate.of(2026, 11, 25)
        ));

        mockMvc.perform(get("/tasks/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.title").value("Consultar tarefa"));
    }

    @Test
    void shouldUpdateTask() throws Exception {
        Task saved = repository.save(new Task(
                "Título antigo",
                "Descrição antiga",
                TaskStatus.PENDING,
                LocalDate.of(2026, 11, 20)
        ));

        mockMvc.perform(put("/tasks/{id}", saved.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "title": "Título atualizado",
                        "description": "Descrição atualizada",
                        "dueDate": "2026-11-30"
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Título atualizado"))
                .andExpect(jsonPath("$.description").value("Descrição atualizada"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void shouldUpdateTaskStatus() throws Exception {
        Task saved = repository.save(new Task(
                "Alterar status",
                "De pendente para concluída",
                TaskStatus.PENDING,
                LocalDate.now()
        ));

        mockMvc.perform(patch("/tasks/{id}/status", saved.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "status": "COMPLETED"
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void shouldDeleteTask() throws Exception {
        Task saved = repository.save(new Task(
                "Excluir tarefa",
                "Remover do banco",
                TaskStatus.PENDING,
                LocalDate.now()
        ));

        mockMvc.perform(delete("/tasks/{id}", saved.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/tasks/{id}", saved.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnNotFoundWhenTaskDoesNotExist() throws Exception {
        mockMvc.perform(get("/tasks/{id}", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectTaskWithoutTitle() throws Exception {
        mockMvc.perform(post("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "title": "",
                        "description": "Sem título",
                        "dueDate": "2026-11-25"
                    }
                    """))
                .andExpect(status().isBadRequest());
    }
}
