package dbi.vaadin.base.ui.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.avatar.AvatarVariant;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.server.menu.MenuConfiguration;
import com.vaadin.flow.server.menu.MenuEntry;
import dbi.vaadin.contacts.domain.Contact;
import dbi.vaadin.contacts.service.ContactService;
import dbi.vaadin.contacts.ui.view.ContactView;
import dbi.vaadin.todo.ui.view.TodoView;

import static com.vaadin.flow.theme.lumo.LumoUtility.*;

import java.util.List;

@Layout
public final class MainLayout extends AppLayout {

    private final ContactService contactService;
    private Avatar avatar;
    private MenuBar userMenu;
    private MenuItem userMenuItem;
    private SubMenu contactsSubMenu;
    private boolean initialLoadDone = false;

    public MainLayout(ContactService contactService) {
        this.contactService = contactService;
        setPrimarySection(Section.DRAWER);
        addToDrawer(createHeader(), new Scroller(createSideNav()), createUserMenu());
    }

    private Div createHeader() {
        var appLogo = VaadinIcon.CUBES.create();
        appLogo.addClassNames(TextColor.PRIMARY, IconSize.LARGE);

        var appName = new Span("DBI Vaadin Demo Project");
        appName.addClassNames(FontWeight.SEMIBOLD, FontSize.LARGE);

        var header = new Div(appLogo, appName);
        header.addClassNames(Display.FLEX, Padding.MEDIUM, Gap.MEDIUM, AlignItems.CENTER);
        return header;
    }

    private SideNav createSideNav() {
        var nav = new SideNav();
        nav.addClassNames(Margin.Horizontal.MEDIUM);
        MenuConfiguration.getMenuEntries().forEach(entry -> nav.addItem(createSideNavItem(entry)));

        nav.addItem(new SideNavItem("Contacts", ContactView.class, VaadinIcon.USER.create()));
        return nav;
    }

    private SideNavItem createSideNavItem(MenuEntry menuEntry) {
        if (menuEntry.icon() != null) {
            return new SideNavItem(menuEntry.title(), menuEntry.path(), new Icon(menuEntry.icon()));
        } else {
            return new SideNavItem(menuEntry.title(), menuEntry.path());
        }
    }

    private Component createUserMenu() {
        avatar = new Avatar("Default User");
        avatar.addThemeVariants(AvatarVariant.LUMO_XSMALL);
        avatar.addClassNames(Margin.Right.SMALL);

        userMenu = new MenuBar();
        userMenu.addThemeVariants(MenuBarVariant.LUMO_TERTIARY_INLINE);
        userMenu.addClassNames(Margin.MEDIUM);

        userMenuItem = userMenu.addItem(avatar);
        userMenuItem.add("Default User");

        contactsSubMenu = userMenuItem.getSubMenu();
        contactsSubMenu.addItem("Select Contact").getElement().setAttribute("disabled", true);

        // Load contacts on first click only
        userMenuItem.addClickListener(e -> {
            if (!initialLoadDone) {
                loadContacts();
                initialLoadDone = true;
            }
        });

        return userMenu;
    }

    public void refreshContacts() {
        loadContacts(); // Force reload when a new contact is added
    }

    private void loadContacts() {
        contactsSubMenu.removeAll();
        contactsSubMenu.addItem("Select Contact").getElement().setAttribute("disabled", true);

        List<Contact> contacts = contactService.findAll();

        if (contacts.isEmpty()) {
            contactsSubMenu.addItem("No contacts available").setEnabled(false);
        } else {
            contacts.forEach(contact -> {
                MenuItem contactItem = contactsSubMenu.addItem(
                        contact.getFirstName() + " " + contact.getLastName(),
                        e -> updateSelectedContact(contact) // No refresh here, just update selection
                );
                contactItem.addClassNames("contact-menu-item");
            });
        }
    }

    private void updateSelectedContact(Contact contact) {
        String fullName = contact.getFirstName() + " " + contact.getLastName();
        avatar.setName(fullName);
        userMenuItem.setText(fullName);
    }
}