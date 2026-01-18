package ru.practicum.shareit.booking.dto;

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

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingDtoJsonTest {

    @Autowired
    private JacksonTester<BookingDto> jsonBookingDto;

    @Autowired
    private JacksonTester<BookingResponseDto> jsonBookingResponseDto;

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
    void bookingDtoSerializationTest() throws IOException {
        LocalDateTime start = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 16, 10, 30, 0);
        BookingDto dto = new BookingDto(1L, 10L, start, end, "WAITING");

        JsonContent<BookingDto> result = jsonBookingDto.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathNumberValue("$.itemId").isEqualTo(10);
        assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo("WAITING");
        assertThat(result).extractingJsonPathStringValue("$.start")
                .isEqualTo(start.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        assertThat(result).extractingJsonPathStringValue("$.end")
                .isEqualTo(end.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }

    @Test
    void bookingDtoDeserializationTest() throws IOException {
        String json = "{" +
                "\"id\": 1," +
                "\"itemId\": 10," +
                "\"start\": \"2024-01-15T10:30:00\"," +
                "\"end\": \"2024-01-16T10:30:00\"," +
                "\"status\": \"WAITING\"" +
                "}";

        BookingDto dto = jsonBookingDto.parseObject(json);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getItemId()).isEqualTo(10L);
        assertThat(dto.getStatus()).isEqualTo("WAITING");
        assertThat(dto.getStart()).isEqualTo(LocalDateTime.of(2024, 1, 15, 10, 30, 0));
        assertThat(dto.getEnd()).isEqualTo(LocalDateTime.of(2024, 1, 16, 10, 30, 0));
    }

    @Test
    void bookingResponseDtoSerializationTest() throws IOException {
        LocalDateTime start = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 16, 10, 30, 0);

        BookingResponseDto.BookerDto booker = new BookingResponseDto.BookerDto(1L);
        BookingResponseDto.ItemDto item = new BookingResponseDto.ItemDto(10L, "Дрель");

        BookingResponseDto dto = new BookingResponseDto(1L, start, end, "APPROVED", booker, item);

        JsonContent<BookingResponseDto> result = jsonBookingResponseDto.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo("APPROVED");
        assertThat(result).extractingJsonPathNumberValue("$.booker.id").isEqualTo(1);
        assertThat(result).extractingJsonPathNumberValue("$.item.id").isEqualTo(10);
        assertThat(result).extractingJsonPathStringValue("$.item.name").isEqualTo("Дрель");

        assertThat(result).extractingJsonPathStringValue("$.start")
                .isEqualTo(start.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        assertThat(result).extractingJsonPathStringValue("$.end")
                .isEqualTo(end.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }

    @Test
    void bookingResponseDtoDeserializationTest() throws IOException {
        String json = "{" +
                "\"id\": 1," +
                "\"start\": \"2024-01-15T10:30:00\"," +
                "\"end\": \"2024-01-16T10:30:00\"," +
                "\"status\": \"APPROVED\"," +
                "\"booker\": {\"id\": 1}," +
                "\"item\": {\"id\": 10, \"name\": \"Дрель\"}" +
                "}";

        BookingResponseDto dto = jsonBookingResponseDto.parseObject(json);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getStatus()).isEqualTo("APPROVED");
        assertThat(dto.getBooker().getId()).isEqualTo(1L);
        assertThat(dto.getItem().getId()).isEqualTo(10L);
        assertThat(dto.getItem().getName()).isEqualTo("Дрель");
        assertThat(dto.getStart()).isEqualTo(LocalDateTime.of(2024, 1, 15, 10, 30, 0));
        assertThat(dto.getEnd()).isEqualTo(LocalDateTime.of(2024, 1, 16, 10, 30, 0));
    }

    @Test
    void bookingResponseDto_whenEmptyBookerAndItem_shouldSerializeProperly() throws IOException {
        BookingResponseDto dto = new BookingResponseDto();
        dto.setId(1L);
        dto.setStatus("WAITING");

        JsonContent<BookingResponseDto> result = jsonBookingResponseDto.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo("WAITING");
        assertThat(result).extractingJsonPathStringValue("$.booker").isNull();
        assertThat(result).extractingJsonPathStringValue("$.item").isNull();
    }
}