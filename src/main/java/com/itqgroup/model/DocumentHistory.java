package com.itqgroup.model;

import com.itqgroup.dto.DocumentAction;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "document_history")
public class DocumentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    Document document;

    @Column(name = "initiator", nullable = false)
    String initiator;

    @Column(name = "action_time", nullable = false)
    @CreationTimestamp
    LocalDateTime actionTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false)
    DocumentAction action;

    @Column(name = "comment")
    String comment;
}