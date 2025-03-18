package org.vaadin.example.model;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Table(name = "app_user")
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String phone;
}
