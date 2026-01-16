package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Transactional
@AutoConfigureTestDatabase
public class BookingServiceImplTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private User owner;
    private User booker;
    private Item item;

    @BeforeEach
    void setUp() {
        owner = userRepository.save(
                new User(null, "Owner", "owner@mail.com")
        );

        booker = userRepository.save(
                new User(null, "Booker", "booker@mail.com")
        );

        item = itemRepository.save(
                new Item(
                        null,
                        "Drill",
                        "Power drill",
                        true,
                        owner,
                        null
                )
        );
    }

    @Test
    void create_shouldSaveBookingInDatabase() {
        BookingDto dto = new BookingDto(
                null,
                item.getId(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                null
        );

        BookingResponseDto response =
                bookingService.create(booker.getId(), dto);

        assertNotNull(response.getId());

        Booking bookingFromDb =
                bookingRepository.findById(response.getId()).orElseThrow();

        assertEquals(BookingStatus.WAITING.name(), bookingFromDb.getStatus().name());
        assertEquals(booker.getId(), bookingFromDb.getBooker().getId());
        assertEquals(item.getId(), bookingFromDb.getItem().getId());
    }
}
