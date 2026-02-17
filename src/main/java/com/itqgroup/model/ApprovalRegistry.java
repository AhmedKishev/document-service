package com.itqgroup.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;


@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "approval_registry")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApprovalRegistry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", unique = true, nullable = false)
    Document document;

    @Column(name = "approved_by", nullable = false)
    String approvedBy;

    @CreationTimestamp
    @Column(name = "approved_at", nullable = false)
    LocalDateTime approvedAt;

    @Column(name = "approval_number", unique = true, nullable = false)
    String approvalNumber;

}