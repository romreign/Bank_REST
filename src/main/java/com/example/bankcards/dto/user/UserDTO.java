package com.example.bankcards.dto.user;

import com.example.bankcards.entity.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {

    @NotNull(message = "Id cannot be null")
    private Long id;

    @NotBlank(message = "Surname cannot be blank")
    private String surname;

    @NotBlank(message = "Name cannot be blank")
    private String name;

    @NotBlank(message = "Patronymic cannot be blank")
    private String patronymic;

    @NotBlank(message = "Login cannot be blank")
    private String login;

    @NotBlank(message = "Role cannot be blank")
    private String role;

    @NotBlank(message = "Birthday cannot be blank")
    private LocalDate birthday;

    public static UserDTO fromEntity(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .surname(user.getSurname())
                .name(user.getName())
                .patronymic(user.getPatronymic())
                .login(user.getLogin())
                .role(user.getRole().getName())
                .birthday(user.getBirthday())
                .build();
    }
}
