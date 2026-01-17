package ru.practicum.shareit.request.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.client.RequestClient;
import ru.practicum.shareit.request.dto.ItemRequestDto;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemRequestController {

    private final RequestClient requestClient;
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @PostMapping
    public Object create(@RequestHeader(USER_ID_HEADER) Long userId,
                                         @Valid @RequestBody ItemRequestDto dto) {
        log.info("Creating request {}, userId={}", dto, userId);
        return requestClient.create(userId, dto);
    }

    @GetMapping
    public Object getOwn(@RequestHeader(USER_ID_HEADER) Long userId) {
        log.info("Get own requests for userId={}", userId);
        return requestClient.getOwn(userId);
    }

    @GetMapping("/all")
    public Object getAll(@RequestHeader(USER_ID_HEADER) Long userId) {
        log.info("Get all requests for userId={}", userId);
        return requestClient.getAll(userId);
    }

    @GetMapping("/{requestId}")
    public Object getById(@RequestHeader(USER_ID_HEADER) Long userId,
                                          @PathVariable Long requestId) {
        log.info("Get request {}, userId={}", requestId, userId);
        return requestClient.getById(userId, requestId);
    }
}
