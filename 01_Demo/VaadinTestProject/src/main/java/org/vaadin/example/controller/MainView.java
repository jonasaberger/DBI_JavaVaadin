package org.vaadin.example.controller;

import org.vaadin.example.model.User;
import org.vaadin.example.service.UserService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import org.springframework.beans.factory.annotation.Autowired;

@Route("") // Root-Pfad der Anwendung
@SpringComponent
public class MainView extends VerticalLayout {

    private final UserService userService;

    private final TextField nameField = new TextField("Name");
    private final TextField emailField = new TextField("Email");
    private final TextField phoneField = new TextField("Phone");
    private final Button saveButton = new Button("Save");
    private final Grid<User> userGrid = new Grid<>(User.class);

    @Autowired
    public MainView(UserService userService) {
        this.userService = userService;

        // Layout für die Eingabefelder und den Button
        add(nameField, emailField, phoneField, saveButton, userGrid);

        // Button-Click-Listener zum Speichern des Benutzers
        saveButton.addClickListener(event -> saveUser());

        // Grid konfigurieren
        userGrid.setColumns("id", "name", "email", "phone");
        updateGrid();

        // Styling (optional)
        setAlignItems(Alignment.CENTER);
    }

    private void saveUser() {
        User user = new User();
        user.setName(nameField.getValue());
        user.setEmail(nameField.getValue());
        user.setPhone(nameField.getValue());

        userService.addUser(user);
        updateGrid();

        // Felder leeren
        nameField.clear();
        emailField.clear();
        phoneField.clear();
    }

    private void updateGrid() {
        userGrid.setItems(userService.getAllUsers());
    }
}