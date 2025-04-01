package dbi.vaadin.todo.service;

import dbi.vaadin.todo.domain.Todo;
import dbi.vaadin.todo.domain.TodoRepository;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class TodoService {

    private final TodoRepository todoRepository;
    private final Clock clock;

    TodoService(TodoRepository todoRepository, Clock clock) {
        this.todoRepository = todoRepository;
        this.clock = clock;
    }

    @Transactional
    public void createTodo(String description, @Nullable LocalDate dueDate) {
        if ("fail".equals(description)) {
            throw new RuntimeException("This is for testing the error handler");
        }
        var todo = new Todo();
        todo.setDescription(description);
        todo.setCreationDate(clock.instant());
        todo.setDueDate(dueDate);
        todoRepository.saveAndFlush(todo);
    }


    @Transactional
    public Todo updateTodo(Todo todo) {
        if (!todoRepository.existsById(todo.getId())) {
            throw new IllegalArgumentException("Todo not found with id: " + todo.getId());
        }
        return todoRepository.save(todo);
    }

    @Transactional
    public void deleteTodo(Long id) {
        if ("fail".equals(id)) {
            throw new RuntimeException("This is for testing the error handler");
        }
        todoRepository.deleteById(id);
    }

    public List<Todo> list(Pageable pageable) {
        return todoRepository.findAllBy(pageable).toList();
    }
}