package com.portfolio.board.inquiry;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {

    @Query("SELECT i FROM Inquiry i WHERE " +
           "(:startDate IS NULL OR i.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR i.createdAt <= :endDate) AND " +
           "(:answerStatus IS NULL OR i.answerStatus = :answerStatus) AND " +
           "(:searchText IS NULL OR i.title LIKE %:searchText% OR i.content LIKE %:searchText% OR i.user.name LIKE %:searchText%) AND " +
           "(:userId IS NULL OR i.user.id = :userId)")
    Page<Inquiry> findByCondition(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("answerStatus") AnswerStatus answerStatus,
            @Param("searchText") String searchText,
            @Param("userId") Long userId,
            Pageable pageable);

    List<Inquiry> findTop4ByOrderByCreatedAtDesc();
}
