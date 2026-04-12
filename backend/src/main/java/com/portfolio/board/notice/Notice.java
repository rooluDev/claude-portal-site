package com.portfolio.board.notice;

import com.portfolio.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notice")
@Getter
@NoArgsConstructor
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NoticeCategory category;

    @Column(nullable = false)
    private boolean isPinned;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private int viewCount;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public Notice(User user, NoticeCategory category, boolean isPinned, String title, String content) {
        this.user = user;
        this.category = category;
        this.isPinned = isPinned;
        this.title = title;
        this.content = content;
        this.viewCount = 0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void incrementViewCount() {
        this.viewCount++;
    }

    public void update(NoticeCategory category, boolean isPinned, String title, String content) {
        this.category = category;
        this.isPinned = isPinned;
        this.title = title;
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }
}
