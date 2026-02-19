package com.itqgroup.dto;


import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StatusChangeResultDto {

    Long id;

    StatusChangeStatus status;

    String message;

    public enum StatusChangeStatus {
        SUCCESS,
        CONFLICT,
        NOT_FOUND,
        REGISTRY_ERROR
    }

}
