package rs.ac.bg.fon.aleksa_jaksic.sa.user.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
     * Must not be blank and enforces a length restriction requiring
     * a minimum of 3 and a maximum of 50 characters to fulfill profile requirements.
     */
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    /**
     * The hashed password used to authenticate the user.
     * Must not be blank and requires a minimum length of 6 characters to uphold password strength.
     */
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;

    /**
     * The official email address linked to the user.
     * Must not be blank, must comply with standard web email formats,
     * and is capped at a maximum of 50 characters.
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 50, message = "Email is too long")
    private String email;

    /**
     * The system privilege level assigned to the user.
     * Must not be null.
     */
    @NotNull(message = "User role is required")
    @Enumerated(EnumType.STRING)
    private Role role;
}
