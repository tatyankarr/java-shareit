package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.ConflictException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class UserServiceImplTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private UserDto userDto;
    private User existingUser;

    @BeforeEach
    void setUp() {
        existingUser = new User();
        existingUser.setName("Existing User");
        existingUser.setEmail("existing@test.com");
        existingUser = userRepository.save(existingUser);

        userDto = new UserDto();
        userDto.setName("Test User");
        userDto.setEmail("test@test.com");
    }

    @Test
    void create_shouldCreateUserSuccessfully() {
        UserDto result = userService.create(userDto);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("Test User", result.getName());
        assertEquals("test@test.com", result.getEmail());

        User savedUser = userRepository.findById(result.getId()).orElseThrow();
        assertEquals("Test User", savedUser.getName());
        assertEquals("test@test.com", savedUser.getEmail());
    }

    @Test
    void create_whenEmailAlreadyExists_shouldThrowConflictException() {
        UserDto duplicateEmailDto = new UserDto();
        duplicateEmailDto.setName("Another User");
        duplicateEmailDto.setEmail("existing@test.com");

        assertThrows(ConflictException.class, () ->
                userService.create(duplicateEmailDto)
        );
    }

    @Test
    void update_shouldUpdateUserSuccessfully() {
        UserDto updateDto = new UserDto();
        updateDto.setName("Updated Name");
        updateDto.setEmail("updated@test.com");

        UserDto result = userService.update(existingUser.getId(), updateDto);

        assertEquals("Updated Name", result.getName());
        assertEquals("updated@test.com", result.getEmail());

        User updatedUser = userRepository.findById(existingUser.getId()).orElseThrow();
        assertEquals("Updated Name", updatedUser.getName());
        assertEquals("updated@test.com", updatedUser.getEmail());
    }

    @Test
    void update_whenPartialUpdate_shouldUpdateOnlyProvidedFields() {
        UserDto updateDto = new UserDto();
        updateDto.setName("New Name");

        UserDto result = userService.update(existingUser.getId(), updateDto);

        assertEquals("New Name", result.getName());
        assertEquals("existing@test.com", result.getEmail());
    }

    @Test
    void update_whenEmailNotChanged_shouldNotCheckForDuplicates() {
        UserDto updateDto = new UserDto();
        updateDto.setName("New Name");
        updateDto.setEmail("existing@test.com");

        UserDto result = userService.update(existingUser.getId(), updateDto);

        assertEquals("New Name", result.getName());
        assertEquals("existing@test.com", result.getEmail());
    }

    @Test
    void update_whenUserNotFound_shouldThrowNotFoundException() {
        UserDto updateDto = new UserDto();
        updateDto.setName("Updated Name");

        assertThrows(NotFoundException.class, () ->
                userService.update(999L, updateDto)
        );
    }

    @Test
    void getById_shouldReturnUser() {
        UserDto result = userService.getById(existingUser.getId());

        assertNotNull(result);
        assertEquals(existingUser.getId(), result.getId());
        assertEquals("Existing User", result.getName());
        assertEquals("existing@test.com", result.getEmail());
    }

    @Test
    void getById_whenUserNotFound_shouldThrowNotFoundException() {
        assertThrows(NotFoundException.class, () ->
                userService.getById(999L)
        );
    }

    @Test
    void getAll_shouldReturnAllUsers() {
        User secondUser = new User();
        secondUser.setName("Second User");
        secondUser.setEmail("second@test.com");
        userRepository.save(secondUser);

        List<UserDto> results = userService.getAll();

        assertEquals(2, results.size());
        assertThat(results)
                .extracting(UserDto::getName)
                .containsExactlyInAnyOrder("Existing User", "Second User");
    }

    @Test
    void delete_shouldDeleteUser() {
        Long userId = existingUser.getId();
        userService.delete(userId);
        assertFalse(userRepository.existsById(userId));
    }

    @Test
    void delete_whenUserNotFound_shouldNotThrowException() {
        assertDoesNotThrow(() -> userService.delete(999L));
    }
}
