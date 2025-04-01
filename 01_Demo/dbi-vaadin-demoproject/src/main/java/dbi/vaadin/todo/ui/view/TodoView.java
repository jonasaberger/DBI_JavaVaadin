package dbi.vaadin.todo.ui.view;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import dbi.vaadin.base.ui.component.ViewToolbar;
import dbi.vaadin.todo.domain.Todo;
import dbi.vaadin.todo.service.TodoService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.time.Clock;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Optional;

import static com.vaadin.flow.spring.data.VaadinSpringDataHelpers.toSpringPageRequest;

@Route("")
@PageTitle("Task List")
@Menu(order = 0, icon = "vaadin:clipboard-check", title = "Task List")
public class TodoView extends Main {

    private final TodoService todoService;
    private final DateTimeFormatter dateTimeFormatter;
    private final DateTimeFormatter dateFormatter;

    final TextField description;
    final DatePicker dueDate;
    final Button createBtn;
    final Grid<Todo> todoGrid;

    public TodoView(TodoService todoService, Clock clock) {
        this.todoService = todoService;

        // Initialize formatters
        dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                .withZone(clock.getZone())
                .withLocale(getLocale());
        dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                .withLocale(getLocale());

        // Form components
        description = new TextField();
        description.setPlaceholder("What do you want to do?");
        description.setAriaLabel("Task description");
        description.setMaxLength(Todo.DESCRIPTION_MAX_LENGTH);
        description.setMinWidth("20em");

        dueDate = new DatePicker();
        dueDate.setPlaceholder("Due date");
        dueDate.setAriaLabel("Due date");

        // Create Button
        createBtn = new Button("Create", new Icon(VaadinIcon.PLUS));
        createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createBtn.addClickListener(event -> createTodo());

        // Grid setup
        todoGrid = new Grid<>();
        configureGrid();

        setSizeFull();
        addClassNames(
                LumoUtility.BoxSizing.BORDER,
                LumoUtility.Display.FLEX,
                LumoUtility.FlexDirection.COLUMN,
                LumoUtility.Padding.MEDIUM,
                LumoUtility.Gap.SMALL
        );

        add(new ViewToolbar("Task List",
                ViewToolbar.group(description, dueDate, createBtn)));
        add(todoGrid);
    }

    private void configureGrid() {
        todoGrid.setItems(query -> todoService.list(toSpringPageRequest(query)).stream());

        // Description column
        todoGrid.addColumn(Todo::getDescription)
                .setHeader("Description")
                .setAutoWidth(true);

        // Due Date column
        todoGrid.addColumn(todo -> Optional.ofNullable(todo.getDueDate())
                        .map(dateFormatter::format)
                        .orElse("Never"))
                .setHeader("Due Date");

        // Creation Date column
        todoGrid.addColumn(todo -> dateTimeFormatter.format(todo.getCreationDate()))
                .setHeader("Created");

        // Actions column
        todoGrid.addComponentColumn(todo -> {
            // Edit Button
            Button editBtn = new Button(new Icon(VaadinIcon.EDIT));
            editBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            editBtn.setTooltipText("Edit task");
            editBtn.addClickListener(e -> openEditDialog(todo));

            // Delete Button
            Button deleteBtn = new Button(new Icon(VaadinIcon.TRASH));
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            deleteBtn.setTooltipText("Delete task");
            deleteBtn.addClickListener(e -> deleteTodo(todo));

            return new HorizontalLayout(editBtn, deleteBtn);
        }).setHeader("Actions").setAutoWidth(true).setFlexGrow(0);

        todoGrid.setSizeFull();
    }

    private void createTodo() {
        if (description.isEmpty()) {
            showErrorNotification("Description cannot be empty");
            return;
        }

        todoService.createTodo(description.getValue(), dueDate.getValue());
        refreshGrid();
        clearForm();
        showSuccessNotification("Task added");
    }

    private void deleteTodo(Todo todo) {
        todoService.deleteTodo(todo.getId());
        refreshGrid();
        showErrorNotification("Task deleted");
    }

    private void openEditDialog(Todo todo) {
        Dialog editDialog = new Dialog();
        editDialog.setHeaderTitle("Edit Task");
        editDialog.setCloseOnEsc(true);
        editDialog.setCloseOnOutsideClick(false);

        // Create form fields with current values
        TextField editDescription = new TextField("Description");
        editDescription.setValue(todo.getDescription());
        editDescription.setWidthFull();
        editDescription.setMaxLength(Todo.DESCRIPTION_MAX_LENGTH);

        DatePicker editDueDate = new DatePicker("Due Date");
        editDueDate.setValue(todo.getDueDate());
        editDueDate.setWidthFull();


        // Save button action
        Button saveButton = new Button("Save", e -> {
            if (editDescription.isEmpty()) {
                showErrorNotification("Description cannot be empty");
                return;
            }

            try {
                // Update the todo object
                todo.setDescription(editDescription.getValue());
                todo.setDueDate(editDueDate.getValue());

                // Call service to update
                Todo updatedTodo = todoService.updateTodo(todo);

                // Refresh UI
                refreshGrid();
                editDialog.close();
                showSuccessNotification("Task updated successfully");
            } catch (Exception ex) {
                showErrorNotification("Error updating task: " + ex.getMessage());
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // Cancel button
        Button cancelButton = new Button("Cancel", e -> editDialog.close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        // Layout
        VerticalLayout dialogLayout = new VerticalLayout(
                editDescription,
                editDueDate
        );
        dialogLayout.setPadding(false);
        dialogLayout.setSpacing(true);

        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancelButton);
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        editDialog.add(dialogLayout);
        editDialog.getFooter().add(buttonLayout);
        editDialog.open();
    }

    private void refreshGrid() {
        todoGrid.getDataProvider().refreshAll();
    }

    private void clearForm() {
        description.clear();
        dueDate.clear();
        description.focus();
    }

    private void showSuccessNotification(String message) {
        Notification.show(message, 3000, Notification.Position.BOTTOM_END)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void showErrorNotification(String message) {
        Notification.show(message, 3000, Notification.Position.BOTTOM_END)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}