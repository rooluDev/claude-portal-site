package com.portfolio.board.free;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface FreeRepository extends JpaRepository<FreePost, Long> {

    @Query("SELECT f FROM FreePost f WHERE " +
           "(:startDate IS NULL OR f.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR f.createdAt <= :endDate) AND " +
           "(:category IS NULL OR f.category = :category) AND " +
           "(:searchText IS NULL OR f.title LIKE %:searchText% OR f.content LIKE %:searchText% OR f.user.name LIKE %:searchText%)")
    Page<FreePost> findByCondition(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("category") FreeCategory category,
            @Param("searchText") String searchText,
            Pageable pageable);

    List<FreePost> findTop6ByOrderByCreatedAtDesc();
}
