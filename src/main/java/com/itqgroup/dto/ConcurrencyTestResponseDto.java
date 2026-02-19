package com.itqgroup.dto;


import lombok.*;
import lombok.experimental.FieldDefaults;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConcurrencyTestResponseDto {

    int successfulAttempts;
    int conflictAttempts;
    int registryErrorAttempts;
    int notFoundAttempts;
    DocumentStatus finalStatus;

}
