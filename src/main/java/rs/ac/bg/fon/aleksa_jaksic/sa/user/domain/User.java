package rs.ac.bg.fon.aleksa_jaksic.sa.user.domain;

import jakarta.persistence.*;
import lombok.*;

/**
 * Represents a user within the system.
 * Manages identity credentials and contact details.
 * @author Aleksa Jaksic (a-jaksic)
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "users")
public class User {

    /**
     * Unique ID.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The unique username used by the user to log into the application.
     */
    private String username;

    /**
     * The hashed password used to authenticate the user.
     */
    private String password;

    /**
     * The official email address linked to the user.
     */
    private String email;

    /**
     * The system privilege level assigned to the user.
     */
    @Enumerated(EnumType.STRING)
    private Role role;
}
