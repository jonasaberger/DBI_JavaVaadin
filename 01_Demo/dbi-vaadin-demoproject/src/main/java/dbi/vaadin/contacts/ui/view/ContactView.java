package dbi.vaadin.contacts.ui.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import dbi.vaadin.base.ui.view.MainLayout;
import dbi.vaadin.contacts.service.ContactService;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "contacts", layout = MainLayout.class)
public class ContactView extends VerticalLayout {

    private final ContactService contactService;

    @Autowired
    public ContactView(ContactService contactService) {
        this.contactService = contactService;
        initializeForm();
    }

    private void initializeForm() {
        FormLayout formLayout = new FormLayout();

        TextField firstName = new TextField("First Name");
        TextField lastName = new TextField("Last Name");

        formLayout.add(firstName, lastName);

        Button submitButton = new Button("Submit");
        submitButton.addClickListener(event -> {
            if (!firstName.isEmpty() && !lastName.isEmpty()) {
                contactService.createContact(firstName.getValue(), lastName.getValue());

                // Get MainLayout instance from the current UI
                UI.getCurrent().getChildren()
                        .filter(component -> component instanceof MainLayout)
                        .findFirst()
                        .ifPresent(component -> {
                            ((MainLayout) component).refreshContacts();
                        });

                firstName.clear();
                lastName.clear();
            }
        });

        add(formLayout, submitButton);
    }
}