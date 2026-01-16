package ru.practicum.shareit.request.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestDtoJsonTest {

    @Autowired
    private JacksonTester<ItemRequestDto> jsonItemRequestDto;

    @Autowired
    private JacksonTester<ItemRequestResponseDto> jsonItemRequestResponseDto;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void itemRequestDtoSerializationTest() throws IOException {
        LocalDateTime created = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
        ItemRequestDto dto = new ItemRequestDto(1L, "Нужна дрель", 123L, created);

        JsonContent<ItemRequestDto> result = jsonItemRequestDto.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Нужна дрель");
        assertThat(result).extractingJsonPathNumberValue("$.requestorId").isEqualTo(123);
        assertThat(result).extractingJsonPathStringValue("$.created")
                .isEqualTo(created.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }

    @Test
    void itemRequestDtoDeserializationTest() throws IOException {
        String json = "{\"id\": 1, \"description\": \"Нужна дрель\", " +
                "\"requestorId\": 123, \"created\": \"2024-01-15T10:30:00\"}";

        ItemRequestDto dto = jsonItemRequestDto.parseObject(json);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getDescription()).isEqualTo("Нужна дрель");
        assertThat(dto.getRequestorId()).isEqualTo(123L);
        assertThat(dto.getCreated()).isEqualTo(LocalDateTime.of(2024, 1, 15, 10, 30, 0));
    }

    @Test
    void itemRequestResponseDtoSerializationTest() throws IOException {
        LocalDateTime created = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
        ItemForRequestDto itemDto = new ItemForRequestDto(1L, "Дрель", 456L);
        ItemRequestResponseDto dto = new ItemRequestResponseDto(
                1L, "Нужна дрель", created, List.of(itemDto)
        );

        JsonContent<ItemRequestResponseDto> result = jsonItemRequestResponseDto.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Нужна дрель");
        assertThat(result).extractingJsonPathStringValue("$.created")
                .isEqualTo(created.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        assertThat(result).extractingJsonPathNumberValue("$.items[0].id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.items[0].name").isEqualTo("Дрель");
        assertThat(result).extractingJsonPathNumberValue("$.items[0].ownerId").isEqualTo(456);
    }

    @Test
    void itemForRequestDtoSerializationTest() throws IOException {
        ItemForRequestDto dto = new ItemForRequestDto(1L, "Дрель", 123L);

        String json = objectMapper.writeValueAsString(dto);

        assertThat(json).contains("\"id\":1");
        assertThat(json).contains("\"name\":\"Дрель\"");
        assertThat(json).contains("\"ownerId\":123");
    }
}