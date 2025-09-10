package com.example.bankcards.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@NoArgsConstructor
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Role cannot be null")
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @NotBlank(message = "Login cannot be blank")
    @Size(min = 3, max = 50, message = "Login must be between 3 and 50 characters")
    @Column(name = "login", nullable = false, unique = true)
    private String login;

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 6, message = "Password must be at least 6 characters")
    @Column(name = "password", nullable = false)
    private String password;

    @NotBlank(message = "Surname cannot be blank")
    @Size(max = 35, message = "Surname must be less than 35 characters")
    @Column(name = "surname", nullable = false)
    private String surname;

    @NotBlank(message = "Name cannot be blank")
    @Size(max = 35, message = "Name must be less than 35 characters")
    @Column(name = "name", nullable = false)
    private String name;

    @Size(max = 35, message = "Patronymic must be less than 35 characters")
    @Column(name = "patronymic")
    private String patronymic;

    @NotNull(message = "Birthday cannot be null")
    @Past(message = "Birthday must be in the past")
    @Column(name = "birthday", nullable = false)
    private LocalDate birthday;

    public User(Role role, String login, String password, String surname, String name,
                String patronymic, LocalDate birthday) {
        this.role = role;
        this.login = login;
        this.password = password;
        this.surname = surname;
        this.name = name;
        this.patronymic = patronymic;
        this.birthday = birthday;
    }

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Card> cards = new ArrayList<>();
}
