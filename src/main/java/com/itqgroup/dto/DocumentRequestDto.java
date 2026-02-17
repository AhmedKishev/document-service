package com.itqgroup.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DocumentRequestDto {

    @NotBlank(message = "Author is required")
    String author;

    @NotBlank(message = "Title is required")
    String title;

}
