package dbi.vaadin.contacts.service;

import dbi.vaadin.contacts.domain.Contact;
import dbi.vaadin.contacts.domain.ContactRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class ContactService {

    private final ContactRepository contactRepository;

    public ContactService(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    public Contact createContact(String firstName, String lastName) {
        Contact contact = new Contact();
        contact.setFirstName(firstName);
        contact.setLastName(lastName);
        return contactRepository.saveAndFlush(contact); // Return saved contact
    }


    public List<Contact> findAll() {
        return contactRepository.findAll();
    }

    public void deleteAll() {
        contactRepository.deleteAll();
    }
}