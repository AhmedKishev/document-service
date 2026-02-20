package com.itqgroup.repository;

import com.itqgroup.dto.DocumentStatus;
import com.itqgroup.model.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    @Query("SELECT DISTINCT d FROM Document d LEFT JOIN FETCH d.history LEFT JOIN FETCH d.approvalRegistry WHERE d.id IN :ids")
    List<Document> findByIdIn(@Param("ids") List<Long> ids);

    @Query("SELECT DISTINCT d FROM Document d " +
            "LEFT JOIN FETCH d.history " +
            "LEFT JOIN FETCH d.approvalRegistry " +
            "WHERE (:status IS NULL OR d.status = :status) " +
            "AND (:author IS NULL OR d.author = :author) " +
            "AND d.createdAt >= COALESCE(:dateFrom, d.createdAt) " +
            "AND d.createdAt <= COALESCE(:dateTo, d.createdAt)")
    Page<Document> searchDocuments(
            @Param("status") DocumentStatus status,
            @Param("author") String author,
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo,
            Pageable pageable);


    @Query("SELECT DISTINCT d FROM Document d " +
            "LEFT JOIN FETCH d.history " +
            "LEFT JOIN FETCH d.approvalRegistry " +
            "WHERE d.status = :status")
    List<Document> findByStatus(@Param("status") DocumentStatus status, Pageable pageable);

    @Query("SELECT d FROM Document d LEFT JOIN FETCH d.history LEFT JOIN FETCH d.approvalRegistry WHERE d.id = :id")
    Optional<Document> findByIdWithDetails(@Param("id") Long id);


    @Query(value = """
            SELECT *
            FROM documents
            WHERE status = :status
            ORDER BY id
            LIMIT :limit
            FOR UPDATE SKIP LOCKED
            """,
            nativeQuery = true)
    List<Document> findByStatusWithLock(
            @Param("status") String status,
            @Param("limit") int limit
    );

    @Query("SELECT DISTINCT d FROM Document d LEFT JOIN FETCH d.history")
    List<Document> findAllWithHistory();
}
