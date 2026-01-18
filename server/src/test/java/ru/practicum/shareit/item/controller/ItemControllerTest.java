package ru.practicum.shareit.item.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.service.ItemService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    @Test
    void create_shouldReturnCreatedItem() throws Exception {
        Long userId = 1L;
        ItemDto requestDto = new ItemDto();
        requestDto.setName("Дрель");
        requestDto.setDescription("Электрическая дрель");
        requestDto.setAvailable(true);

        ItemResponseDto responseDto = new ItemResponseDto();
        responseDto.setId(1L);
        responseDto.setName("Дрель");
        responseDto.setDescription("Электрическая дрель");
        responseDto.setAvailable(true);

        when(itemService.create(eq(userId), any(ItemDto.class)))
                .thenReturn(responseDto);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Дрель"))
                .andExpect(jsonPath("$.description").value("Электрическая дрель"))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void update_shouldReturnUpdatedItem() throws Exception {
        Long userId = 1L;
        Long itemId = 10L;

        ItemUpdateDto updateDto = new ItemUpdateDto();
        updateDto.setName("Обновленная дрель");
        updateDto.setDescription("Новое описание");

        ItemDto responseDto = new ItemDto();
        responseDto.setId(itemId);
        responseDto.setName("Обновленная дрель");
        responseDto.setDescription("Новое описание");
        responseDto.setAvailable(true);

        when(itemService.update(userId, itemId, updateDto))
                .thenReturn(responseDto);

        mockMvc.perform(patch("/items/{itemId}", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemId))
                .andExpect(jsonPath("$.name").value("Обновленная дрель"))
                .andExpect(jsonPath("$.description").value("Новое описание"));
    }

    @Test
    void getById_shouldReturnItem() throws Exception {
        Long userId = 1L;
        Long itemId = 10L;

        CommentDto commentDto = new CommentDto();
        commentDto.setId(1L);
        commentDto.setText("Отличная вещь!");
        commentDto.setAuthorName("Booker");
        commentDto.setCreated(LocalDateTime.now());

        ItemResponseDto responseDto = new ItemResponseDto();
        responseDto.setId(itemId);
        responseDto.setName("Дрель");
        responseDto.setDescription("Электрическая дрель");
        responseDto.setAvailable(true);
        responseDto.setComments(List.of(commentDto));

        when(itemService.getById(userId, itemId))
                .thenReturn(responseDto);

        mockMvc.perform(get("/items/{itemId}", itemId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemId))
                .andExpect(jsonPath("$.name").value("Дрель"))
                .andExpect(jsonPath("$.comments[0].text").value("Отличная вещь!"))
                .andExpect(jsonPath("$.comments[0].authorName").value("Booker"));
    }

    @Test
    void getAllByOwner_shouldReturnOwnerItems() throws Exception {
        Long userId = 1L;

        ItemResponseDto item1 = new ItemResponseDto();
        item1.setId(1L);
        item1.setName("Дрель");
        item1.setAvailable(true);

        ItemResponseDto item2 = new ItemResponseDto();
        item2.setId(2L);
        item2.setName("Молоток");
        item2.setAvailable(true);

        when(itemService.getAllByOwner(userId))
                .thenReturn(List.of(item1, item2));

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Дрель"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].name").value("Молоток"));
    }

    @Test
    void search_shouldReturnFoundItems() throws Exception {
        Long userId = 1L;
        String searchText = "дрель";

        ItemDto item1 = new ItemDto();
        item1.setId(1L);
        item1.setName("Дрель");
        item1.setDescription("Электрическая дрель");
        item1.setAvailable(true);

        ItemDto item2 = new ItemDto();
        item2.setId(2L);
        item2.setName("Аккумуляторная дрель");
        item2.setDescription("Без проводов");
        item2.setAvailable(true);

        when(itemService.search(userId, searchText))
                .thenReturn(List.of(item1, item2));

        mockMvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", userId)
                        .param("text", searchText))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Дрель"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].name").value("Аккумуляторная дрель"));
    }

    @Test
    void createComment_shouldReturnCreatedComment() throws Exception {
        Long userId = 1L;
        Long itemId = 10L;

        CommentDto requestDto = new CommentDto();
        requestDto.setText("Отличная вещь, спасибо!");

        CommentDto responseDto = new CommentDto();
        responseDto.setId(1L);
        responseDto.setText("Отличная вещь, спасибо!");
        responseDto.setAuthorName("Booker");
        responseDto.setCreated(LocalDateTime.now());

        when(itemService.createComment(userId, itemId, requestDto))
                .thenReturn(responseDto);

        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.text").value("Отличная вещь, спасибо!"))
                .andExpect(jsonPath("$.authorName").value("Booker"))
                .andExpect(jsonPath("$.created").isNotEmpty());
    }
}