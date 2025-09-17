package com.example.bankcards.service;

import com.example.bankcards.dto.user.UserDTO;
import com.example.bankcards.dto.user.UserUpdateDTO;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Test
    public void getUsers_ShouldReturnListOfAllUsers() {
        Role userRole = new Role();
        userRole.setName("USER");

        Role adminRole = new Role();
        adminRole.setName("ADMIN");

        User user1 = User.builder()
                .role(userRole)
                .login("ivanov")
                .password("password123")
                .surname("Иванов")
                .name("Иван")
                .patronymic("Иванович")
                .birthday(LocalDate.of(1990, 5, 15))
                .build();

        User user2 = User.builder()
                .role(adminRole)
                .login("petrov")
                .password("password456")
                .surname("Петров")
                .name("Петр")
                .patronymic("Петрович")
                .birthday(LocalDate.of(1985, 8, 20))
                .build();

        List<User> mockUsers = List.of(user1, user2);

        when(userRepository.findAll()).thenReturn(mockUsers);

        List<UserDTO> result = userService.getUsers();

        assertNotNull(result);
        assertEquals(2, result.size());

        UserDTO firstUser = result.getFirst();
        assertEquals("Иванов", firstUser.getSurname());
        assertEquals("Иван", firstUser.getName());
        assertEquals("Иванович", firstUser.getPatronymic());
        assertEquals("ivanov", firstUser.getLogin());
        assertEquals("USER", firstUser.getRole());
        assertEquals(LocalDate.of(1990, 5, 15), firstUser.getBirthday());

        UserDTO secondUser = result.get(1);
        assertEquals("Петров", secondUser.getSurname());
        assertEquals("Петр", secondUser.getName());
        assertEquals("Петрович", secondUser.getPatronymic());
        assertEquals("petrov", secondUser.getLogin());
        assertEquals("ADMIN", secondUser.getRole());
        assertEquals(LocalDate.of(1985, 8, 20), secondUser.getBirthday());

        verify(userRepository, times(1)).findAll();
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    public void getUser_ShouldReturnUserDTO_WhenUserExists() {
        long userId = 1L;

        Role userRole = new Role();
        userRole.setName("USER");

        User mockUser = User.builder()
                .role(userRole)
                .login("ivanov")
                .password("password123")
                .surname("Иванов")
                .name("Иван")
                .patronymic("Иванович")
                .birthday(LocalDate.of(1990, 5, 15))
                .build();

        mockUser.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        UserDTO result = userService.getUser(userId);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("Иванов", result.getSurname());
        assertEquals("Иван", result.getName());
        assertEquals("Иванович", result.getPatronymic());
        assertEquals("ivanov", result.getLogin());
        assertEquals("USER", result.getRole());
        assertEquals(LocalDate.of(1990, 5, 15), result.getBirthday());

        verify(userRepository, times(1)).findById(userId);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    public void getUser_ShouldThrowUserNotFoundException_WhenUserNotFound() {
        long userId = 999L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUser(userId));

        verify(userRepository, times(1)).findById(userId);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    public void updateUser_ShouldUpdateUserDetailsAndReturnUpdatedUserDTO() {
        long userId = 1L;

        UserUpdateDTO updateDTO = UserUpdateDTO.builder()
                .surname("Петров")
                .name("Петр")
                .build();

        Role userRole = new Role();
        userRole.setName("USER");

        User existingUser = User.builder()
                .role(userRole)
                .login("ivanov")
                .password("password123")
                .surname("Иванов")
                .name("Иван")
                .patronymic("Иванович")
                .birthday(LocalDate.of(1990, 5, 15))
                .build();
        existingUser.setId(userId);

        User savedUser = User.builder()
                .role(userRole)
                .login("ivanov")
                .password("password123")
                .surname("Петров")
                .name("Петр")
                .patronymic("Иванович")
                .birthday(LocalDate.of(1990, 5, 15))
                .build();
        savedUser.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserDTO result = userService.updateUser(userId, updateDTO);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("Петров", result.getSurname());
        assertEquals("Петр", result.getName());
        assertEquals("Иванович", result.getPatronymic());
        assertEquals("ivanov", result.getLogin());
        assertEquals("USER", result.getRole());
        assertEquals(LocalDate.of(1990, 5, 15), result.getBirthday());

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(existingUser);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    public void updateUser_ShouldThrowUserNotFoundException_WhenUserNotFound() {
        long userId = 999L;

        UserUpdateDTO updateDTO = UserUpdateDTO.builder()
                .surname("Петров")
                .name("Петр")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.updateUser(userId, updateDTO));

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).save(any(User.class));
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    public void deleteUser_ShouldDeleteUser_WhenUserExists() {
        Long userId = 1L;

        Role userRole = new Role();
        userRole.setName("USER");

        User existingUser = User.builder()
                .role(userRole)
                .login("ivanov")
                .password("password123")
                .surname("Иванов")
                .name("Иван")
                .patronymic("Иванович")
                .birthday(LocalDate.of(1990, 5, 15))
                .build();
        existingUser.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        assertDoesNotThrow(() -> userService.deleteUser(userId));

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).delete(existingUser);
    }

    @Test
    public void deleteUser_ShouldThrowUserNotFoundException_WhenUserNotFound() {
        Long userId = 999L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(userId));

        verify(userRepository, times(1)).findById(userId);
        verifyNoMoreInteractions(userRepository);
    }
}
