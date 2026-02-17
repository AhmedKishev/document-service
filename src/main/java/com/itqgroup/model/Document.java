package com.itqgroup.model;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "documents")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "document_number", unique = true, nullable = false)
    String documentNumber;

    @Column(name = "author", nullable = false)
    String author;

    @Column(name = "title", nullable = false)
    String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    DocumentStatus status = DocumentStatus.DRAFT;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    LocalDateTime updatedAt;

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<DocumentHistory> history;

    @OneToOne(mappedBy = "document", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    ApprovalRegistry approvalRegistry;

    public void addHistory(DocumentHistory documentHistory) {
        history = new ArrayList<>();
        history.add(documentHistory);
    }


}
