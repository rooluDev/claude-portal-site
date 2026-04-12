package com.portfolio.board.gallery;

import com.portfolio.attachment.entity.Attachment;
import com.portfolio.attachment.repository.AttachmentRepository;
import com.portfolio.attachment.service.AttachmentService;
import com.portfolio.board.common.BoardListRequestDto;
import com.portfolio.board.common.BoardType;
import com.portfolio.board.common.PageResponseDto;
import com.portfolio.board.common.ViewCountService;
import com.portfolio.comment.repository.CommentRepository;
import com.portfolio.common.exception.CustomException;
import com.portfolio.common.exception.ErrorCode;
import com.portfolio.config.FilePolicyProperties;
import com.portfolio.user.entity.User;
import com.portfolio.user.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GalleryService {

    private final GalleryRepository galleryRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final AttachmentRepository attachmentRepository;
    private final AttachmentService attachmentService;
    private final ViewCountService viewCountService;
    private final FilePolicyProperties filePolicyProperties;

    @Transactional(readOnly = true)
    public PageResponseDto<GalleryListResponseDto> getList(BoardListRequestDto dto) {
        LocalDateTime startDate = parseStartDate(dto.getStartDate());
        LocalDateTime endDate = parseEndDate(dto.getEndDate());
        String searchText = resolveSearchText(dto.getSearchText());
        GalleryCategory category = resolveCategory(dto.getCategory());

        Sort sort = Sort.by(Sort.Direction.fromString(dto.getOrderDirection()),
                mapOrderField(dto.getOrderValue()));
        Pageable pageable = PageRequest.of(dto.getPageNum() - 1, dto.getPageSize(), sort);

        Page<GalleryPost> page = galleryRepository.findByCondition(startDate, endDate, category, searchText, pageable);
        List<GalleryListResponseDto> content = page.getContent().stream()
                .map(post -> {
                    Attachment thumbnail = attachmentRepository
                            .findFirstByBoardTypeAndPostIdOrderBySortOrderAsc(BoardType.GALLERY, post.getId())
                            .orElse(null);
                    String thumbnailUrl = thumbnail != null
                            ? attachmentService.buildFileUrl(BoardType.GALLERY, thumbnail.getStoredName())
                            : null;
                    int totalImageCount = (int) attachmentRepository.countByBoardTypeAndPostId(BoardType.GALLERY, post.getId());
                    return new GalleryListResponseDto(post, thumbnailUrl, totalImageCount);
                })
                .collect(Collectors.toList());

        return new PageResponseDto<>(page, content);
    }

    @Transactional
    public GalleryDetailResponseDto getDetail(Long id, Long currentUserId, boolean isAdmin, HttpSession session) {
        GalleryPost post = findById(id);

        if (viewCountService.isNewView("GALLERY", id, session.getId())) {
            post.incrementViewCount();
        }

        boolean isEditable = currentUserId != null &&
                (post.getUser().getId().equals(currentUserId) || isAdmin);

        List<Attachment> images =
                attachmentRepository.findByBoardTypeAndPostIdOrderBySortOrderAsc(BoardType.GALLERY, id);

        return new GalleryDetailResponseDto(post, isEditable, images, attachmentService);
    }

    @Transactional
    public Long create(GalleryWriteRequestDto dto, List<MultipartFile> files, Long currentUserId) {
        int newCount = countNonEmpty(files);
        int maxCount = filePolicyProperties.getGallery().getMaxCount();
        if (newCount > maxCount) throw new CustomException(ErrorCode.FILE_COUNT_EXCEEDED);

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED));

        GalleryPost post = GalleryPost.builder()
                .user(user)
                .category(dto.getCategory())
                .title(dto.getTitle())
                .content(dto.getContent())
                .build();
        galleryRepository.save(post);

        attachmentService.saveFiles(files, BoardType.GALLERY, post.getId(), 0);
        return post.getId();
    }

    @Transactional
    public void update(Long id, GalleryWriteRequestDto dto, List<MultipartFile> files,
                       Long currentUserId, boolean isAdmin) {
        GalleryPost post = findById(id);
        checkPermission(post.getUser().getId(), currentUserId, isAdmin);

        int newCount = countNonEmpty(files);
        attachmentService.validateGalleryCount(id, dto.getDeleteAttachmentIds(), newCount);

        // 삭제 요청 이미지 삭제
        if (dto.getDeleteAttachmentIds() != null) {
            for (Long attachmentId : dto.getDeleteAttachmentIds()) {
                attachmentRepository.findById(attachmentId).ifPresent(attachmentService::deleteFile);
            }
        }

        // 남은 이미지 sort_order 재정렬
        List<Attachment> remaining =
                attachmentRepository.findByBoardTypeAndPostIdOrderBySortOrderAsc(BoardType.GALLERY, id);
        for (int i = 0; i < remaining.size(); i++) {
            remaining.get(i).updateSortOrder(i);
        }

        // 새 이미지 추가
        attachmentService.saveFiles(files, BoardType.GALLERY, id, remaining.size());

        post.update(dto.getCategory(), dto.getTitle(), dto.getContent());
    }

    @Transactional
    public void delete(Long id, Long currentUserId, boolean isAdmin) {
        GalleryPost post = findById(id);
        checkPermission(post.getUser().getId(), currentUserId, isAdmin);

        attachmentService.deleteAllByPost(BoardType.GALLERY, id);
        commentRepository.deleteByBoardTypeAndPostId(BoardType.GALLERY, id);
        galleryRepository.delete(post);
    }

    public List<GalleryPost> getTop4ForMain() {
        return galleryRepository.findTop4ByOrderByCreatedAtDesc();
    }

    private GalleryPost findById(Long id) {
        return galleryRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
    }

    private void checkPermission(Long authorId, Long currentUserId, boolean isAdmin) {
        if (!authorId.equals(currentUserId) && !isAdmin) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
    }

    private int countNonEmpty(List<MultipartFile> files) {
        return (files == null) ? 0 : (int) files.stream().filter(f -> !f.isEmpty()).count();
    }

    private LocalDateTime parseStartDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return LocalDateTime.now().minusYears(1).with(LocalTime.MIN);
        try { return LocalDate.parse(dateStr).atStartOfDay(); }
        catch (DateTimeParseException e) { return LocalDateTime.now().minusYears(1).with(LocalTime.MIN); }
    }

    private LocalDateTime parseEndDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return LocalDateTime.now().with(LocalTime.MAX);
        try { return LocalDate.parse(dateStr).atTime(LocalTime.MAX); }
        catch (DateTimeParseException e) { return LocalDateTime.now().with(LocalTime.MAX); }
    }

    private String resolveSearchText(String s) {
        return (s == null || s.length() < 2) ? null : s;
    }

    private GalleryCategory resolveCategory(String s) {
        if (s == null || s.isBlank()) return null;
        try { return GalleryCategory.valueOf(s); } catch (IllegalArgumentException e) { return null; }
    }

    private String mapOrderField(String v) {
        return switch (v) {
            case "title" -> "title";
            case "viewCount" -> "viewCount";
            case "category" -> "category";
            default -> "createdAt";
        };
    }
}
