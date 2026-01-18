package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemForRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public ItemRequestDto create(Long userId, ItemRequestDto dto) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        ItemRequest request = new ItemRequest();
        request.setDescription(dto.getDescription());
        request.setRequestor(user);
        request.setCreated(LocalDateTime.now());

        ItemRequest saved = requestRepository.save(request);

        return new ItemRequestDto(
                saved.getId(),
                saved.getDescription(),
                userId,
                saved.getCreated()
        );
    }

    @Override
    public List<ItemRequestResponseDto> getOwnRequests(Long userId) {
        return requestRepository.findByRequestor_IdOrderByCreatedDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<ItemRequestResponseDto> getOtherRequests(Long userId) {
        return requestRepository.findByRequestor_IdNotOrderByCreatedDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public ItemRequestResponseDto getById(Long userId, Long requestId) {
        ItemRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос не найден"));

        return mapToResponse(request);
    }

    private ItemRequestResponseDto mapToResponse(ItemRequest request) {
        List<ItemForRequestDto> items = itemRepository
                .findByRequest_Id(request.getId())
                .stream()
                .map(item -> new ItemForRequestDto(
                        item.getId(),
                        item.getName(),
                        item.getOwner().getId()
                ))
                .toList();

        return new ItemRequestResponseDto(
                request.getId(),
                request.getDescription(),
                request.getCreated(),
                items
        );
    }
}
