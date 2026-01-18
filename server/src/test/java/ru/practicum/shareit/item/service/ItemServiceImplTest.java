package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ItemServiceImplTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ItemRequestRepository requestRepository;

    private User owner;
    private User booker;
    private Item item;
    private ItemDto itemDto;

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

        itemDto = new ItemDto();
        itemDto.setName("Новая дрель");
        itemDto.setDescription("Новая электрическая дрель");
        itemDto.setAvailable(true);
    }

    @Test
    void create_shouldCreateItemSuccessfully() {
        ItemResponseDto result = itemService.create(owner.getId(), itemDto);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("Новая дрель", result.getName());
        assertEquals("Новая электрическая дрель", result.getDescription());
        assertTrue(result.getAvailable());

        Item savedItem = itemRepository.findById(result.getId()).orElseThrow();
        assertEquals(owner.getId(), savedItem.getOwner().getId());
    }

    @Test
    void create_withRequest_shouldCreateItemWithRequest() {
        ItemRequest request = new ItemRequest();
        request.setDescription("Нужна дрель");
        request.setRequestor(booker);
        request.setCreated(LocalDateTime.now());
        request = requestRepository.save(request);

        itemDto.setRequestId(request.getId());

        ItemResponseDto result = itemService.create(owner.getId(), itemDto);

        assertNotNull(result);
        assertEquals(request.getId(), result.getRequestId());

        Item savedItem = itemRepository.findById(result.getId()).orElseThrow();
        assertNotNull(savedItem.getRequest());
        assertEquals(request.getId(), savedItem.getRequest().getId());
    }

    @Test
    void create_whenUserNotFound_shouldThrowNotFoundException() {
        assertThrows(NotFoundException.class, () ->
                itemService.create(999L, itemDto)
        );
    }

    @Test
    void update_shouldUpdateItemSuccessfully() {
        ItemUpdateDto updateDto = new ItemUpdateDto();
        updateDto.setName("Обновленная дрель");
        updateDto.setDescription("Обновленное описание");
        updateDto.setAvailable(false);

        ItemDto result = itemService.update(owner.getId(), item.getId(), updateDto);

        assertEquals("Обновленная дрель", result.getName());
        assertEquals("Обновленное описание", result.getDescription());
        assertFalse(result.getAvailable());

        Item updatedItem = itemRepository.findById(item.getId()).orElseThrow();
        assertEquals("Обновленная дрель", updatedItem.getName());
        assertEquals("Обновленное описание", updatedItem.getDescription());
        assertFalse(updatedItem.getAvailable());
    }

    @Test
    void update_whenPartialUpdate_shouldUpdateOnlyProvidedFields() {
        ItemUpdateDto updateDto = new ItemUpdateDto();
        updateDto.setName("Новое имя");

        ItemDto result = itemService.update(owner.getId(), item.getId(), updateDto);

        assertEquals("Новое имя", result.getName());
        assertEquals("Электрическая дрель", result.getDescription());
        assertTrue(result.getAvailable());
    }

    @Test
    void update_whenNotOwner_shouldThrowNotFoundException() {
        ItemUpdateDto updateDto = new ItemUpdateDto();
        updateDto.setName("Обновленная дрель");
        assertThrows(NotFoundException.class, () ->
                itemService.update(booker.getId(), item.getId(), updateDto)
        );
    }

    @Test
    void getById_shouldReturnItemWithComments() {
        Comment comment = new Comment();
        comment.setText("Отличная дрель!");
        comment.setItem(item);
        comment.setAuthor(booker);
        comment.setCreated(LocalDateTime.now());
        commentRepository.save(comment);

        ItemResponseDto result = itemService.getById(owner.getId(), item.getId());

        assertNotNull(result);
        assertEquals(item.getId(), result.getId());
        assertEquals(item.getName(), result.getName());
        assertEquals(1, result.getComments().size());
        assertEquals("Отличная дрель!", result.getComments().get(0).getText());
        assertEquals(booker.getName(), result.getComments().get(0).getAuthorName());
    }

    @Test
    void getById_whenOwner_shouldIncludeBookings() {
        Booking booking = new Booking();
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStart(LocalDateTime.now().minusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(1));
        booking.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(booking);

        ItemResponseDto result = itemService.getById(owner.getId(), item.getId());

        assertNotNull(result);
        assertNotNull(result.getLastBooking());
        assertEquals(booking.getId(), result.getLastBooking().getId());
        assertEquals(booker.getId(), result.getLastBooking().getBookerId());
    }

    @Test
    void search_shouldFindItemsByText() {
        Item item2 = new Item();
        item2.setName("Электрический молоток");
        item2.setDescription("Мощный инструмент");
        item2.setAvailable(true);
        item2.setOwner(owner);
        itemRepository.save(item2);

        Item unavailableItem = new Item();
        unavailableItem.setName("Старая дрель");
        unavailableItem.setDescription("Не работает");
        unavailableItem.setAvailable(false);
        unavailableItem.setOwner(owner);
        itemRepository.save(unavailableItem);

        List<ItemDto> results = itemService.search(owner.getId(), "дрель");

        assertEquals(1, results.size());
        assertEquals("Дрель", results.get(0).getName());

        List<ItemDto> resultsByDescription = itemService.search(owner.getId(), "электрический");
        assertEquals(1, resultsByDescription.size());
        assertEquals("Электрический молоток", resultsByDescription.get(0).getName());
    }

    @Test
    void createComment_shouldCreateCommentSuccessfully() {
        Booking booking = new Booking();
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStart(LocalDateTime.now().minusDays(2));
        booking.setEnd(LocalDateTime.now().minusDays(1));
        booking.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(booking);

        CommentDto commentDto = new CommentDto();
        commentDto.setText("Отличная дрель, спасибо!");

        CommentDto result = itemService.createComment(booker.getId(), item.getId(), commentDto);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("Отличная дрель, спасибо!", result.getText());
        assertEquals(booker.getName(), result.getAuthorName());
        assertNotNull(result.getCreated());

        List<Comment> comments = commentRepository.findAllByItemId(item.getId());
        assertEquals(1, comments.size());
        assertEquals("Отличная дрель, спасибо!", comments.get(0).getText());
    }

    @Test
    void createComment_whenNoCompletedBooking_shouldThrowValidationException() {
        CommentDto commentDto = new CommentDto();
        commentDto.setText("Отличная дрель!");
        assertThrows(ValidationException.class, () ->
                itemService.createComment(booker.getId(), item.getId(), commentDto)
        );
    }

    @Test
    void createComment_whenBookingNotApproved_shouldThrowValidationException() {
        Booking booking = new Booking();
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStart(LocalDateTime.now().minusDays(2));
        booking.setEnd(LocalDateTime.now().minusDays(1));
        booking.setStatus(BookingStatus.WAITING);
        bookingRepository.save(booking);

        CommentDto commentDto = new CommentDto();
        commentDto.setText("Отличная дрель!");

        assertThrows(ValidationException.class, () ->
                itemService.createComment(booker.getId(), item.getId(), commentDto)
        );
    }

    @Test
    void getAllByOwner_shouldReturnItemsSortedById() {
        Item item2 = new Item();
        item2.setName("Молоток");
        item2.setDescription("Строительный молоток");
        item2.setAvailable(true);
        item2.setOwner(owner);
        itemRepository.save(item2);

        Item item3 = new Item();
        item3.setName("Отвертка");
        item3.setDescription("Крестовая отвертка");
        item3.setAvailable(true);
        item3.setOwner(owner);
        itemRepository.save(item3);

        List<ItemResponseDto> results = itemService.getAllByOwner(owner.getId());

        assertEquals(3, results.size());
        for (int i = 0; i < results.size() - 1; i++) {
            assertTrue(results.get(i).getId() < results.get(i + 1).getId());
        }
    }
}