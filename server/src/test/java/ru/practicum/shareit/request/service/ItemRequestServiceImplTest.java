package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@AutoConfigureTestDatabase
class ItemRequestServiceImplTest {

    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRequestRepository requestRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = userRepository.save(
                new User(null, "User", "user@mail.com")
        );

        requestRepository.save(
                new ItemRequest(
                        null,
                        "Need drill",
                        user,
                        LocalDateTime.now().minusDays(1)
                )
        );

        requestRepository.save(
                new ItemRequest(
                        null,
                        "Need bike",
                        user,
                        LocalDateTime.now()
                )
        );
    }

    @Test
    void getOwnRequests_shouldReturnRequestsOrderedByCreatedDesc() {
        List<ItemRequestResponseDto> result =
                itemRequestService.getOwnRequests(user.getId());

        assertEquals(2, result.size());
        assertEquals("Need bike", result.get(0).getDescription());
        assertEquals("Need drill", result.get(1).getDescription());
    }
}
