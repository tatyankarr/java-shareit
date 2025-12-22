package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemDto {
    private Long id;

    @NotBlank(message = "Name обязательно")
    private String name;

    @NotBlank(message = "Description обязательно")
    private String description;

    @NotNull(message = "Available не может быть пустым")
    private Boolean available;
    
    private Long requestId;
}

