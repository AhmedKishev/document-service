package com.itqgroup.dto;

import com.itqgroup.model.DocumentAction;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DocumentHistoryDto {

    String initiator;

    LocalDateTime actionTime;

    DocumentAction action;

    String comment;

}
