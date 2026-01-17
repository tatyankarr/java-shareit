package ru.practicum.shareit.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.client.UserClient;
import ru.practicum.shareit.user.dto.UserDto;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserController {
    private final UserClient userClient;

    @PostMapping
    public Object create(@Validated({UserDto.Create.class}) @RequestBody UserDto userDto) {
        log.info("Creating user {}", userDto);
        return userClient.create(userDto);
    }

    @PatchMapping("/{id}")
    public Object update(@PathVariable Long id, @RequestBody UserDto userDto) {
        log.info("Updating user id={}, data={}", id, userDto);
        return userClient.update(id, userDto);
    }

    @GetMapping("/{id}")
    public Object getById(@PathVariable Long id) {
        log.info("Get user id={}", id);
        return userClient.getById(id);
    }

    @GetMapping
    public Object getAll() {
        log.info("Get all users");
        return userClient.getAll();
    }

    @DeleteMapping("/{id}")
    public Object delete(@PathVariable Long id) {
        log.info("Delete user id={}", id);
        return userClient.deleteUser(id);
    }
}
