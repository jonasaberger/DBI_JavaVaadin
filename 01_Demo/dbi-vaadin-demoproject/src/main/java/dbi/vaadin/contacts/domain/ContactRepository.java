package dbi.vaadin.contacts.domain;

import dbi.vaadin.todo.domain.Todo;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ContactRepository extends JpaRepository<Contact, Long>, JpaSpecificationExecutor<Contact> {

    // If you don't need a total row count, Slice is better than Page.
    Slice<Contact> findAllBy(Pageable pageable);

}
