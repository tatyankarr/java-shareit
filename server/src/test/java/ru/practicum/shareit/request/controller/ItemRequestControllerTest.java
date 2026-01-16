package ru.practicum.shareit.request.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemForRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestService requestService;

    @Test
    void create_shouldReturnCreatedRequest() throws Exception {
        Long userId = 1L;
        ItemRequestDto requestDto = new ItemRequestDto(null, "Нужна дрель", null, null);
        ItemRequestDto responseDto = new ItemRequestDto(1L, "Нужна дрель", userId, LocalDateTime.now());

        when(requestService.create(eq(userId), any(ItemRequestDto.class)))
                .thenReturn(responseDto);

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.description").value("Нужна дрель"))
                .andExpect(jsonPath("$.requestorId").value(userId));
    }

    @Test
    void getOwn_shouldReturnUserRequests() throws Exception {
        Long userId = 1L;
        ItemRequestResponseDto responseDto = new ItemRequestResponseDto(
                1L, "Нужна дрель", LocalDateTime.now(), List.of()
        );

        when(requestService.getOwnRequests(userId))
                .thenReturn(List.of(responseDto));

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].description").value("Нужна дрель"))
                .andExpect(jsonPath("$[0].items").isArray());
    }

    @Test
    void getAll_shouldReturnOtherUsersRequests() throws Exception {
        Long userId = 1L;
        ItemForRequestDto itemDto = new ItemForRequestDto(1L, "Дрель", 2L);
        ItemRequestResponseDto responseDto = new ItemRequestResponseDto(
                2L, "Нужна дрель", LocalDateTime.now(), List.of(itemDto)
        );

        when(requestService.getOtherRequests(userId))
                .thenReturn(List.of(responseDto));

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2L))
                .andExpect(jsonPath("$[0].description").value("Нужна дрель"))
                .andExpect(jsonPath("$[0].items[0].id").value(1L))
                .andExpect(jsonPath("$[0].items[0].name").value("Дрель"))
                .andExpect(jsonPath("$[0].items[0].ownerId").value(2L));
    }

    @Test
    void getById_shouldReturnRequest() throws Exception {
        Long userId = 1L;
        Long requestId = 2L;
        ItemForRequestDto itemDto = new ItemForRequestDto(1L, "Дрель", 2L);
        ItemRequestResponseDto responseDto = new ItemRequestResponseDto(
                requestId, "Нужна дрель", LocalDateTime.now(), List.of(itemDto)
        );

        when(requestService.getById(userId, requestId))
                .thenReturn(responseDto);

        mockMvc.perform(get("/requests/{requestId}", requestId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(requestId))
                .andExpect(jsonPath("$.description").value("Нужна дрель"))
                .andExpect(jsonPath("$.items[0].id").value(1L))
                .andExpect(jsonPath("$.items[0].name").value("Дрель"));
    }
}