package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
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
class ItemDtoJsonTest {

    @Autowired
    private JacksonTester<ItemDto> jsonItemDto;

    @Autowired
    private JacksonTester<ItemResponseDto> jsonItemResponseDto;

    @Autowired
    private JacksonTester<CommentDto> jsonCommentDto;

    @Autowired
    private JacksonTester<ItemUpdateDto> jsonItemUpdateDto;

    @Autowired
    private ObjectMapper objectMapper;

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void itemDtoSerializationTest() throws IOException {
        ItemDto dto = new ItemDto(1L, "Дрель", "Электрическая дрель", true, 100L);

        JsonContent<ItemDto> result = jsonItemDto.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Дрель");
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Электрическая дрель");
        assertThat(result).extractingJsonPathBooleanValue("$.available").isEqualTo(true);
        assertThat(result).extractingJsonPathNumberValue("$.requestId").isEqualTo(100);
    }

    @Test
    void itemDtoDeserializationTest() throws IOException {
        String json = "{" +
                "\"id\": 1," +
                "\"name\": \"Дрель\"," +
                "\"description\": \"Электрическая дрель\"," +
                "\"available\": true," +
                "\"requestId\": 100" +
                "}";

        ItemDto dto = jsonItemDto.parseObject(json);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Дрель");
        assertThat(dto.getDescription()).isEqualTo("Электрическая дрель");
        assertThat(dto.getAvailable()).isTrue();
        assertThat(dto.getRequestId()).isEqualTo(100L);
    }

    @Test
    void itemResponseDtoSerializationTest() throws IOException {
        CommentDto comment = new CommentDto(1L, "Отличная вещь!", "Booker", LocalDateTime.now());
        BookingShortDto lastBooking = new BookingShortDto(10L, 20L);
        BookingShortDto nextBooking = new BookingShortDto(11L, 21L);

        ItemResponseDto dto = new ItemResponseDto(
                1L, "Дрель", "Электрическая дрель", true,
                lastBooking, nextBooking, 100L, List.of(comment)
        );

        JsonContent<ItemResponseDto> result = jsonItemResponseDto.write(dto);
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Дрель");
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Электрическая дрель");
        assertThat(result).extractingJsonPathBooleanValue("$.available").isEqualTo(true);
        assertThat(result).extractingJsonPathNumberValue("$.requestId").isEqualTo(100);

        assertThat(result).extractingJsonPathNumberValue("$.lastBooking.id").isEqualTo(10);
        assertThat(result).extractingJsonPathNumberValue("$.lastBooking.bookerId").isEqualTo(20);
        assertThat(result).extractingJsonPathNumberValue("$.nextBooking.id").isEqualTo(11);
        assertThat(result).extractingJsonPathNumberValue("$.nextBooking.bookerId").isEqualTo(21);

        assertThat(result).extractingJsonPathNumberValue("$.comments[0].id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.comments[0].text").isEqualTo("Отличная вещь!");
        assertThat(result).extractingJsonPathStringValue("$.comments[0].authorName").isEqualTo("Booker");
    }

    @Test
    void itemResponseDtoDeserializationTest() throws IOException {
        String json = "{" +
                "\"id\": 1," +
                "\"name\": \"Дрель\"," +
                "\"description\": \"Электрическая дрель\"," +
                "\"available\": true," +
                "\"lastBooking\": {\"id\": 10, \"bookerId\": 20}," +
                "\"nextBooking\": {\"id\": 11, \"bookerId\": 21}," +
                "\"requestId\": 100," +
                "\"comments\": [{" +
                "   \"id\": 1," +
                "   \"text\": \"Отличная вещь!\"," +
                "   \"authorName\": \"Booker\"," +
                "   \"created\": \"2024-01-15T10:30:00\"" +
                "}]" +
                "}";

        ItemResponseDto dto = jsonItemResponseDto.parseObject(json);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Дрель");
        assertThat(dto.getDescription()).isEqualTo("Электрическая дрель");
        assertThat(dto.getAvailable()).isTrue();
        assertThat(dto.getRequestId()).isEqualTo(100L);

        assertThat(dto.getLastBooking()).isNotNull();
        assertThat(dto.getLastBooking().getId()).isEqualTo(10L);
        assertThat(dto.getLastBooking().getBookerId()).isEqualTo(20L);

        assertThat(dto.getNextBooking()).isNotNull();
        assertThat(dto.getNextBooking().getId()).isEqualTo(11L);
        assertThat(dto.getNextBooking().getBookerId()).isEqualTo(21L);

        assertThat(dto.getComments()).hasSize(1);
        assertThat(dto.getComments().get(0).getText()).isEqualTo("Отличная вещь!");
        assertThat(dto.getComments().get(0).getAuthorName()).isEqualTo("Booker");
    }

    @Test
    void commentDtoSerializationTest() throws IOException {
        LocalDateTime created = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
        CommentDto dto = new CommentDto(1L, "Отличная вещь!", "Booker", created);

        JsonContent<CommentDto> result = jsonCommentDto.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.text").isEqualTo("Отличная вещь!");
        assertThat(result).extractingJsonPathStringValue("$.authorName").isEqualTo("Booker");
        assertThat(result).extractingJsonPathStringValue("$.created")
                .isEqualTo(created.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }

    @Test
    void commentDtoDeserializationTest() throws IOException {
        String json = "{" +
                "\"id\": 1," +
                "\"text\": \"Отличная вещь!\"," +
                "\"authorName\": \"Booker\"," +
                "\"created\": \"2024-01-15T10:30:00\"" +
                "}";

        CommentDto dto = jsonCommentDto.parseObject(json);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getText()).isEqualTo("Отличная вещь!");
        assertThat(dto.getAuthorName()).isEqualTo("Booker");
        assertThat(dto.getCreated()).isEqualTo(LocalDateTime.of(2024, 1, 15, 10, 30, 0));
    }

    @Test
    void itemUpdateDtoSerializationTest() throws IOException {
        ItemUpdateDto dto = new ItemUpdateDto("Новое имя", "Новое описание", false);

        JsonContent<ItemUpdateDto> result = jsonItemUpdateDto.write(dto);

        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Новое имя");
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Новое описание");
        assertThat(result).extractingJsonPathBooleanValue("$.available").isEqualTo(false);
    }

    @Test
    void itemUpdateDtoDeserializationTest() throws IOException {
        String json = "{" +
                "\"name\": \"Новое имя\"," +
                "\"description\": \"Новое описание\"," +
                "\"available\": false" +
                "}";

        ItemUpdateDto dto = jsonItemUpdateDto.parseObject(json);

        assertThat(dto.getName()).isEqualTo("Новое имя");
        assertThat(dto.getDescription()).isEqualTo("Новое описание");
        assertThat(dto.getAvailable()).isFalse();
    }

    @Test
    void itemUpdateDto_whenPartialFields_shouldSerializeProperly() throws IOException {
        ItemUpdateDto dto = new ItemUpdateDto();
        dto.setName("Только имя");
        JsonContent<ItemUpdateDto> result = jsonItemUpdateDto.write(dto);

        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Только имя");
        assertThat(result).extractingJsonPathStringValue("$.description").isNull();
        assertThat(result).extractingJsonPathStringValue("$.available").isNull();
    }

    @Test
    void bookingShortDtoSerializationTest() throws IOException {
        BookingShortDto dto = new BookingShortDto(10L, 20L);

        String json = new ObjectMapper().writeValueAsString(dto);

        assertThat(json).contains("\"id\":10");
        assertThat(json).contains("\"bookerId\":20");
    }

    @Test
    void bookingShortDtoDeserializationTest() throws IOException {
        String json = "{\"id\": 10, \"bookerId\": 20}";

        BookingShortDto dto = new ObjectMapper().readValue(json, BookingShortDto.class);

        assertThat(dto.getId()).isEqualTo(10L);
        assertThat(dto.getBookerId()).isEqualTo(20L);
    }
}