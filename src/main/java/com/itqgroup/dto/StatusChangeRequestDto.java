package com.itqgroup.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StatusChangeRequestDto {

    @NotNull
    @Size(min = 1, max = 1000, message = "Document IDs count must be between 1 and 1000")
    List<Long> ids;

    @NotBlank(message = "Initiator is required")
    String initiator;

}
