package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@AutoConfigureTestDatabase
class ItemRequestServiceImplTest {

    @Autowired
    private ItemRequestService requestService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Test
    void create_shouldCreateRequestAndReturnDto() {
        User user = createTestUser("test@email.com", "Test User");
        User savedUser = userRepository.save(user);

        ItemRequestDto dto = new ItemRequestDto();
        dto.setDescription("Нужна дрель");

        ItemRequestDto result = requestService.create(savedUser.getId(), dto);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("Нужна дрель", result.getDescription());
        assertEquals(savedUser.getId(), result.getRequestorId());
        assertNotNull(result.getCreated());
        assertTrue(result.getCreated().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void getOwnRequests_shouldReturnOnlyUserRequests() {
        User user1 = userRepository.save(createTestUser("user1@email.com", "User 1"));
        User user2 = userRepository.save(createTestUser("user2@email.com", "User 2"));

        ItemRequestDto dto1 = new ItemRequestDto();
        dto1.setDescription("Запрос от пользователя 1");
        requestService.create(user1.getId(), dto1);

        ItemRequestDto dto2 = new ItemRequestDto();
        dto2.setDescription("Запрос от пользователя 2");
        requestService.create(user2.getId(), dto2);

        List<ItemRequestResponseDto> user1Requests = requestService.getOwnRequests(user1.getId());
        List<ItemRequestResponseDto> user2Requests = requestService.getOwnRequests(user2.getId());

        assertEquals(1, user1Requests.size());
        assertEquals("Запрос от пользователя 1", user1Requests.get(0).getDescription());

        assertEquals(1, user2Requests.size());
        assertEquals("Запрос от пользователя 2", user2Requests.get(0).getDescription());
    }

    @Test
    void getOtherRequests_shouldReturnRequestsFromOtherUsers() {
        User user1 = userRepository.save(createTestUser("user1@email.com", "User 1"));
        User user2 = userRepository.save(createTestUser("user2@email.com", "User 2"));
        User user3 = userRepository.save(createTestUser("user3@email.com", "User 3"));

        requestService.create(user1.getId(), new ItemRequestDto(null, "Запрос 1", null, null));
        requestService.create(user2.getId(), new ItemRequestDto(null, "Запрос 2", null, null));
        requestService.create(user3.getId(), new ItemRequestDto(null, "Запрос 3", null, null));

        List<ItemRequestResponseDto> otherRequestsForUser1 = requestService.getOtherRequests(user1.getId());

        assertEquals(2, otherRequestsForUser1.size());
        assertThat(otherRequestsForUser1)
                .extracting(ItemRequestResponseDto::getDescription)
                .containsExactlyInAnyOrder("Запрос 2", "Запрос 3");
    }

    @Test
    void getById_whenRequestNotFound_shouldThrowException() {
        User user = userRepository.save(createTestUser("test@email.com", "Test User"));

        assertThrows(RuntimeException.class, () ->
                requestService.getById(user.getId(), 999L)
        );
    }

    private User createTestUser(String email, String name) {
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        return user;
    }
}
