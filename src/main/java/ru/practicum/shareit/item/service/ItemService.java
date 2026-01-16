package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;

import java.util.List;

public interface ItemService {
    ItemResponseDto create(Long userId, ItemDto itemDto);

    ItemDto update(Long userId, Long itemId, ItemUpdateDto itemDto);

    CommentDto createComment(Long userId, Long itemId, CommentDto commentDto);

    ItemResponseDto getById(Long userId, Long itemId);

    List<ItemResponseDto> getAllByOwner(Long userId);

    List<ItemDto> search(Long userId, String text);
}
