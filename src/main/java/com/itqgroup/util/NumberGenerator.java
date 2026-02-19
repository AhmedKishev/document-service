package com.itqgroup.util;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class NumberGenerator {

    public String generateCreateDocumentNumber() {
        return "DOC- " + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8);
    }


    public String generateApprovalDocumentNumber() {
        return "APR-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

}
