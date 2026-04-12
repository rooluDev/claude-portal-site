package com.portfolio.board.inquiry;

import com.portfolio.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "inquiry")
@Getter
@NoArgsConstructor
public class Inquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private boolean isSecret;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AnswerStatus answerStatus;

    @Column(nullable = false)
    private int viewCount;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public Inquiry(User user, String title, String content, boolean isSecret) {
        this.user = user;
        this.title = title;
        this.content = content;
        this.isSecret = isSecret;
        this.answerStatus = AnswerStatus.PENDING;
        this.viewCount = 0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void incrementViewCount() {
        this.viewCount++;
    }

    public void update(String title, String content, boolean isSecret) {
        this.title = title;
        this.content = content;
        this.isSecret = isSecret;
        this.updatedAt = LocalDateTime.now();
    }

    public void markAnswered() {
        this.answerStatus = AnswerStatus.ANSWERED;
    }

    public void markPending() {
        this.answerStatus = AnswerStatus.PENDING;
    }
}
