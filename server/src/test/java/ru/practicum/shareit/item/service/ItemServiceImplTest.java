package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ItemServiceImplTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Test
    void getAllByOwner_returnsItemsWithEmptyBookingsForNonOwner() {
        User owner = userRepository.save(new User(null, "User", "u@mail.com"));
        Item item = itemRepository.save(
                new Item(null, "Drill", "Tool", true, owner, null)
        );

        List<ItemResponseDto> items = itemService.getAllByOwner(owner.getId());

        assertEquals(1, items.size());
        assertEquals("Drill", items.get(0).getName());
        assertTrue(items.get(0).getComments().isEmpty());
    }
}