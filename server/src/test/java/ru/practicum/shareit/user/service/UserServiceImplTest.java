package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class UserServiceImplTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void createUser_savesToDatabase() {
        UserDto dto = new UserDto(null, "Alex", "alex@mail.com");

        UserDto saved = userService.create(dto);

        assertNotNull(saved.getId());
        assertEquals("Alex", saved.getName());
        assertEquals("alex@mail.com", saved.getEmail());

        assertTrue(userRepository.existsById(saved.getId()));
    }
}
