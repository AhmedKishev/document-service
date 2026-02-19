package com.itqgroup.mapper;

import com.itqgroup.dto.DocumentAction;
import com.itqgroup.model.Document;
import com.itqgroup.model.DocumentHistory;

public class DocumentHistoryMapper {

    public static DocumentHistory toDocumentHistory(Document document, String initiator) {
        return DocumentHistory.builder()
                .initiator(initiator)
                .document(document)
                .action(DocumentAction.CREATE)
                .comment("Document was created")
                .build();
    }

}
