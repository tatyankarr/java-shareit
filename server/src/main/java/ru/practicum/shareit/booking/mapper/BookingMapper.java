package ru.practicum.shareit.booking.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

@UtilityClass
public class BookingMapper {

    public BookingResponseDto toBookingResponseDto(Booking booking) {
        return new BookingResponseDto(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                booking.getStatus().name(),
                new BookingResponseDto.BookerDto(booking.getBooker().getId()),
                new BookingResponseDto.ItemDto(booking.getItem().getId(), booking.getItem().getName())
        );
    }

    public Booking toBooking(BookingDto dto, User booker, Item item) {
        return new Booking(
                dto.getId(),
                item,
                booker,
                dto.getStart(),
                dto.getEnd(),
                null
        );
    }
}
