package com.portfolio.attachment.service;

import com.portfolio.attachment.entity.Attachment;
import com.portfolio.attachment.repository.AttachmentRepository;
import com.portfolio.board.common.BoardType;
import com.portfolio.common.exception.CustomException;
import com.portfolio.common.exception.ErrorCode;
import com.portfolio.config.FilePolicyProperties;
import com.portfolio.config.FileProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final FileProperties fileProperties;
    private final FilePolicyProperties filePolicyProperties;

    private static final List<String> GALLERY_ALLOWED_EXTENSIONS =
            Arrays.asList("jpg", "jpeg", "png", "gif", "webp");

    /**
     * 파일 저장 (게시글 저장 후 호출)
     * @param files       업로드된 파일 목록
     * @param boardType   게시판 타입
     * @param postId      게시글 ID
     * @param startOrder  시작 sort_order (수정 시 기존 파일 뒤에 붙일 때 사용)
     */
    @Transactional
    public void saveFiles(List<MultipartFile> files, BoardType boardType, Long postId, int startOrder) {
        if (files == null || files.isEmpty()) return;

        String dir = boardType == BoardType.GALLERY
                ? fileProperties.getGalleryDir()
                : fileProperties.getFreeDir();

        FilePolicyProperties.BoardFilePolicy policy = boardType == BoardType.GALLERY
                ? filePolicyProperties.getGallery()
                : filePolicyProperties.getFree();

        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            if (file.isEmpty()) continue;

            if (boardType == BoardType.GALLERY) {
                validateGalleryExtension(file);
            }
            validateFileSize(file, policy.getMaxSizeMb());

            String originalName = file.getOriginalFilename();
            String storedName = UUID.randomUUID() + "-" + originalName;
            String filePath = dir + "/" + storedName;

            try {
                File destFile = new File(filePath);
                if (!destFile.isAbsolute()) {
                    destFile = new File(System.getProperty("user.dir"), filePath);
                }
                destFile.getParentFile().mkdirs();
                file.transferTo(destFile);
            } catch (IOException e) {
                log.error("파일 저장 실패: {}", filePath, e);
                throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
            }

            attachmentRepository.save(Attachment.builder()
                    .boardType(boardType)
                    .postId(postId)
                    .originalName(originalName)
                    .storedName(storedName)
                    .filePath(filePath)
                    .fileSize(file.getSize())
                    .sortOrder(startOrder + i)
                    .build());
        }
    }

    /**
     * 개별 파일 삭제 (로컬 파일 삭제 실패는 예외를 삼키고 로그만 기록)
     */
    @Transactional
    public void deleteFile(Attachment attachment) {
        try {
            File file = new File(attachment.getFilePath());
            if (!file.isAbsolute()) {
                file = new File(System.getProperty("user.dir"), attachment.getFilePath());
            }
            if (file.exists() && !file.delete()) {
                log.warn("파일 삭제 실패 (로컬): {}", file.getAbsolutePath());
            }
        } catch (Exception e) {
            log.error("파일 삭제 중 오류 (로컬): {}", attachment.getFilePath(), e);
        }
        attachmentRepository.delete(attachment);
    }

    /**
     * 게시글의 모든 파일 삭제
     */
    @Transactional
    public void deleteAllByPost(BoardType boardType, Long postId) {
        List<Attachment> attachments =
                attachmentRepository.findByBoardTypeAndPostIdOrderBySortOrderAsc(boardType, postId);
        for (Attachment attachment : attachments) {
            deleteFile(attachment);
        }
    }

    /**
     * 갤러리 이미지 개수 검증 (기존 - 삭제할 + 새로 추가할 ≤ maxCount)
     */
    public void validateGalleryCount(Long postId, List<Long> deleteIds, int newFileCount) {
        long existing = attachmentRepository.countByBoardTypeAndPostId(BoardType.GALLERY, postId);
        long deleteCount = deleteIds != null ? deleteIds.size() : 0;
        long after = existing - deleteCount + newFileCount;
        if (after > filePolicyProperties.getGallery().getMaxCount()) {
            throw new CustomException(ErrorCode.FILE_COUNT_EXCEEDED);
        }
    }

    /**
     * 자유게시판 파일 개수 검증
     */
    public void validateFreeCount(Long postId, List<Long> deleteIds, int newFileCount) {
        long existing = attachmentRepository.countByBoardTypeAndPostId(BoardType.FREE, postId);
        long deleteCount = deleteIds != null ? deleteIds.size() : 0;
        long after = existing - deleteCount + newFileCount;
        if (after > filePolicyProperties.getFree().getMaxCount()) {
            throw new CustomException(ErrorCode.FILE_COUNT_EXCEEDED);
        }
    }

    private void validateGalleryExtension(MultipartFile file) {
        String name = file.getOriginalFilename();
        if (name == null) throw new CustomException(ErrorCode.FILE_EXTENSION_INVALID);
        int dot = name.lastIndexOf('.');
        if (dot < 0) throw new CustomException(ErrorCode.FILE_EXTENSION_INVALID);
        String ext = name.substring(dot + 1).toLowerCase();
        if (!GALLERY_ALLOWED_EXTENSIONS.contains(ext)) {
            throw new CustomException(ErrorCode.FILE_EXTENSION_INVALID);
        }
    }

    private void validateFileSize(MultipartFile file, int maxMb) {
        long maxBytes = (long) maxMb * 1024 * 1024;
        if (file.getSize() > maxBytes) {
            throw new CustomException(ErrorCode.FILE_SIZE_EXCEEDED);
        }
    }

    public String buildFileUrl(BoardType boardType, String storedName) {
        String sub = boardType == BoardType.GALLERY ? "gallery" : "free";
        return "/api/files/" + sub + "/" + storedName;
    }
}
