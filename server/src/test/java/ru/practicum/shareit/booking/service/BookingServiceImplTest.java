package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class BookingServiceImplTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    private User owner;
    private User booker;
    private Item item;
    private BookingDto bookingDto;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner@test.com");
        owner = userRepository.save(owner);

        booker = new User();
        booker.setName("Booker");
        booker.setEmail("booker@test.com");
        booker = userRepository.save(booker);

        item = new Item();
        item.setName("Дрель");
        item.setDescription("Электрическая дрель");
        item.setAvailable(true);
        item.setOwner(owner);
        item = itemRepository.save(item);

        bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));
    }

    @Test
    void create_shouldCreateBookingSuccessfully() {
        BookingResponseDto result = bookingService.create(booker.getId(), bookingDto);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(booker.getId(), result.getBooker().getId());
        assertEquals(item.getId(), result.getItem().getId());
        assertEquals("WAITING", result.getStatus());
        assertEquals(bookingDto.getStart(), result.getStart());
        assertEquals(bookingDto.getEnd(), result.getEnd());

        Booking savedBooking = bookingRepository.findById(result.getId()).orElseThrow();
        assertEquals(BookingStatus.WAITING, savedBooking.getStatus());
    }

    @Test
    void create_whenItemNotAvailable_shouldThrowValidationException() {
        item.setAvailable(false);
        itemRepository.save(item);

        assertThrows(ValidationException.class, () ->
                bookingService.create(booker.getId(), bookingDto)
        );
    }

    @Test
    void create_whenOwnerTriesToBookOwnItem_shouldThrowNotFoundException() {
        assertThrows(NotFoundException.class, () ->
                bookingService.create(owner.getId(), bookingDto)
        );
    }

    @Test
    void create_whenEndBeforeStart_shouldThrowValidationException() {
        bookingDto.setStart(LocalDateTime.now().plusDays(2));
        bookingDto.setEnd(LocalDateTime.now().plusDays(1));

        assertThrows(ValidationException.class, () ->
                bookingService.create(booker.getId(), bookingDto)
        );
    }

    @Test
    void create_whenStartInPast_shouldThrowValidationException() {
        bookingDto.setStart(LocalDateTime.now().minusDays(1));

        assertThrows(ValidationException.class, () ->
                bookingService.create(booker.getId(), bookingDto)
        );
    }

    @Test
    void approve_shouldApproveBookingSuccessfully() {
        BookingResponseDto createdBooking = bookingService.create(booker.getId(), bookingDto);

        BookingResponseDto approvedBooking = bookingService.approve(owner.getId(), createdBooking.getId(), true);

        assertEquals("APPROVED", approvedBooking.getStatus());

        Booking booking = bookingRepository.findById(createdBooking.getId()).orElseThrow();
        assertEquals(BookingStatus.APPROVED, booking.getStatus());
    }

    @Test
    void approve_shouldRejectBookingSuccessfully() {
        BookingResponseDto createdBooking = bookingService.create(booker.getId(), bookingDto);
        BookingResponseDto rejectedBooking = bookingService.approve(owner.getId(), createdBooking.getId(), false);
        assertEquals("REJECTED", rejectedBooking.getStatus());
    }

    @Test
    void getById_shouldReturnBookingForBooker() {
        BookingResponseDto createdBooking = bookingService.create(booker.getId(), bookingDto);
        BookingResponseDto result = bookingService.getById(booker.getId(), createdBooking.getId());
        assertEquals(createdBooking.getId(), result.getId());
    }

    @Test
    void getById_shouldReturnBookingForOwner() {
        BookingResponseDto createdBooking = bookingService.create(booker.getId(), bookingDto);
        BookingResponseDto result = bookingService.getById(owner.getId(), createdBooking.getId());
        assertEquals(createdBooking.getId(), result.getId());
    }
}