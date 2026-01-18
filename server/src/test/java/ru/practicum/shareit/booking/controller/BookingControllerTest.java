package ru.practicum.shareit.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.service.BookingService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    @Test
    void create_shouldReturnCreatedBooking() throws Exception {
        Long userId = 1L;
        Long itemId = 10L;

        BookingDto requestDto = new BookingDto();
        requestDto.setItemId(itemId);
        requestDto.setStart(LocalDateTime.now().plusDays(1));
        requestDto.setEnd(LocalDateTime.now().plusDays(2));

        BookingResponseDto responseDto = new BookingResponseDto();
        responseDto.setId(1L);
        responseDto.setStart(requestDto.getStart());
        responseDto.setEnd(requestDto.getEnd());
        responseDto.setStatus("WAITING");
        responseDto.setBooker(new BookingResponseDto.BookerDto(userId));
        responseDto.setItem(new BookingResponseDto.ItemDto(itemId, "Дрель"));

        when(bookingService.create(eq(userId), any(BookingDto.class)))
                .thenReturn(responseDto);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("WAITING"))
                .andExpect(jsonPath("$.booker.id").value(userId))
                .andExpect(jsonPath("$.item.id").value(itemId))
                .andExpect(jsonPath("$.item.name").value("Дрель"));
    }

    @Test
    void approve_shouldApproveBooking() throws Exception {
        Long userId = 1L;
        Long bookingId = 100L;

        BookingResponseDto responseDto = new BookingResponseDto();
        responseDto.setId(bookingId);
        responseDto.setStatus("APPROVED");

        when(bookingService.approve(userId, bookingId, true))
                .thenReturn(responseDto);

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", userId)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingId))
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void getById_shouldReturnBooking() throws Exception {
        Long userId = 1L;
        Long bookingId = 100L;

        BookingResponseDto responseDto = new BookingResponseDto();
        responseDto.setId(bookingId);
        responseDto.setStatus("APPROVED");
        responseDto.setBooker(new BookingResponseDto.BookerDto(userId));
        responseDto.setItem(new BookingResponseDto.ItemDto(10L, "Дрель"));

        when(bookingService.getById(userId, bookingId))
                .thenReturn(responseDto);

        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingId))
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.booker.id").value(userId))
                .andExpect(jsonPath("$.item.id").value(10L))
                .andExpect(jsonPath("$.item.name").value("Дрель"));
    }

    @Test
    void getUserBookings_shouldReturnBookingsWithState() throws Exception {
        Long userId = 1L;
        String state = "ALL";

        BookingResponseDto booking1 = new BookingResponseDto();
        booking1.setId(1L);
        booking1.setStatus("APPROVED");

        BookingResponseDto booking2 = new BookingResponseDto();
        booking2.setId(2L);
        booking2.setStatus("WAITING");

        when(bookingService.getUserBookings(userId, state))
                .thenReturn(List.of(booking1, booking2));

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", state))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].status").value("APPROVED"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].status").value("WAITING"));
    }

    @Test
    void getOwnerBookings_shouldReturnOwnerBookingsWithState() throws Exception {
        Long userId = 1L;
        String state = "FUTURE";

        BookingResponseDto booking = new BookingResponseDto();
        booking.setId(1L);
        booking.setStatus("APPROVED");

        when(bookingService.getOwnerBookings(userId, state))
                .thenReturn(List.of(booking));

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", state))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].status").value("APPROVED"));
    }
}