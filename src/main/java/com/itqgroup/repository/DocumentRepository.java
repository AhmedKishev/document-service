package com.itqgroup.repository;

import com.itqgroup.dto.DocumentStatus;
import com.itqgroup.model.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByIdIn(List<Long> ids);


    @Query("SELECT d FROM Document d WHERE " +
            "(:status IS NULL OR d.status = :status) AND " +
            "(:author IS NULL OR d.author = :author) AND " +
            "d.createdAt >= COALESCE(:dateFrom, d.createdAt) AND " +
            "d.createdAt <= COALESCE(:dateTo, d.createdAt)")
    Page<Document> searchDocuments(
            @Param("status") DocumentStatus status,
            @Param("author") String author,
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo,
            Pageable pageable);

    List<Document> findByStatus(DocumentStatus documentStatus, PageRequest of);

}
