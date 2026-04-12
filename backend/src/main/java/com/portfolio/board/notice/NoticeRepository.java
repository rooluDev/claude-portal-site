package com.portfolio.board.notice;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    @Query("SELECT n FROM Notice n WHERE " +
           "(:startDate IS NULL OR n.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR n.createdAt <= :endDate) AND " +
           "(:category IS NULL OR n.category = :category) AND " +
           "(:searchText IS NULL OR n.title LIKE %:searchText%)")
    Page<Notice> findByCondition(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("category") NoticeCategory category,
            @Param("searchText") String searchText,
            Pageable pageable);

    // 메인 위젯용: isPinned DESC, createdAt DESC 상위 6건
    List<Notice> findTop6ByOrderByIsPinnedDescCreatedAtDesc();
}
