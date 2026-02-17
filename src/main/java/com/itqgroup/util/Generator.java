package com.itqgroup.util;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class Generator {

    public String generateDocumentNumber() {
        return "DOC- " + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8);
    }


}
