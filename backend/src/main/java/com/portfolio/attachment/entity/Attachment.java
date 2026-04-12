package com.portfolio.attachment.entity;

import com.portfolio.board.common.BoardType;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "attachment")
@Getter
@NoArgsConstructor
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BoardType boardType;

    @Column(nullable = false)
    private Long postId;

    @Column(nullable = false, length = 255)
    private String originalName;

    @Column(nullable = false, unique = true, length = 255)
    private String storedName;

    @Column(nullable = false, length = 500)
    private String filePath;

    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = false)
    private int sortOrder;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public void updateSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    @Builder
    public Attachment(BoardType boardType, Long postId, String originalName,
                      String storedName, String filePath, Long fileSize, int sortOrder) {
        this.boardType = boardType;
        this.postId = postId;
        this.originalName = originalName;
        this.storedName = storedName;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.sortOrder = sortOrder;
        this.createdAt = LocalDateTime.now();
    }
}
