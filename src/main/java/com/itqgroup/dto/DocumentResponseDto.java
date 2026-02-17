package com.itqgroup.dto;

import com.itqgroup.model.DocumentStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DocumentResponseDto {

    String author;

    String title;

    DocumentStatus status;

    LocalDateTime createdAt;

    List<DocumentHistoryDto> history;

}
