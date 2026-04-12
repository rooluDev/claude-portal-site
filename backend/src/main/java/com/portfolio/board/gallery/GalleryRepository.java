package com.portfolio.board.gallery;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface GalleryRepository extends JpaRepository<GalleryPost, Long> {

    @Query("SELECT g FROM GalleryPost g WHERE " +
           "(:startDate IS NULL OR g.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR g.createdAt <= :endDate) AND " +
           "(:category IS NULL OR g.category = :category) AND " +
           "(:searchText IS NULL OR g.title LIKE %:searchText% OR g.content LIKE %:searchText% OR g.user.name LIKE %:searchText%)")
    Page<GalleryPost> findByCondition(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("category") GalleryCategory category,
            @Param("searchText") String searchText,
            Pageable pageable);

    List<GalleryPost> findTop4ByOrderByCreatedAtDesc();
}
