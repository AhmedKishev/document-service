package com.itqgroup.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConcurrencyTestRequestDto {


    @NotNull
    Long documentId;

    @Min(value = 1, message = "Threads must be at least 1")
    int threads;

    @Min(value = 1, message = "Attempts must be at least 1")
    int attempts;

    @NotNull(message = "Initiator is required")
    String initiator;

}
