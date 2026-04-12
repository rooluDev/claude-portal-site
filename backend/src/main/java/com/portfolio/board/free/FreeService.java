package com.portfolio.board.free;

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
public class FreeService {

    private final FreeRepository freeRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final AttachmentRepository attachmentRepository;
    private final AttachmentService attachmentService;
    private final ViewCountService viewCountService;
    private final FilePolicyProperties filePolicyProperties;

    @Transactional(readOnly = true)
    public PageResponseDto<FreeListResponseDto> getList(BoardListRequestDto dto) {
        LocalDateTime startDate = parseStartDate(dto.getStartDate());
        LocalDateTime endDate = parseEndDate(dto.getEndDate());
        String searchText = resolveSearchText(dto.getSearchText());
        FreeCategory category = resolveCategory(dto.getCategory());

        Sort sort = Sort.by(Sort.Direction.fromString(dto.getOrderDirection()),
                mapOrderField(dto.getOrderValue()));
        Pageable pageable = PageRequest.of(dto.getPageNum() - 1, dto.getPageSize(), sort);

        Page<FreePost> page = freeRepository.findByCondition(startDate, endDate, category, searchText, pageable);
        List<FreeListResponseDto> content = page.getContent().stream()
                .map(post -> new FreeListResponseDto(
                        post,
                        commentRepository.countByBoardTypeAndPostId(BoardType.FREE, post.getId()),
                        attachmentRepository.existsByBoardTypeAndPostId(BoardType.FREE, post.getId())))
                .collect(Collectors.toList());

        return new PageResponseDto<>(page, content);
    }

    @Transactional
    public FreeDetailResponseDto getDetail(Long id, Long currentUserId, boolean isAdmin, HttpSession session) {
        FreePost post = findById(id);

        if (viewCountService.isNewView("FREE", id, session.getId())) {
            post.incrementViewCount();
        }

        boolean isEditable = currentUserId != null &&
                (post.getUser().getId().equals(currentUserId) || isAdmin);

        List<Attachment> attachments =
                attachmentRepository.findByBoardTypeAndPostIdOrderBySortOrderAsc(BoardType.FREE, id);

        return new FreeDetailResponseDto(post, isEditable, attachments, attachmentService);
    }

    @Transactional
    public Long create(FreeWriteRequestDto dto, List<MultipartFile> files, Long currentUserId) {
        validateFileCount(null, null, files);

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED));

        FreePost post = FreePost.builder()
                .user(user)
                .category(dto.getCategory())
                .title(dto.getTitle())
                .content(dto.getContent())
                .build();
        freeRepository.save(post);

        attachmentService.saveFiles(files, BoardType.FREE, post.getId(), 0);
        return post.getId();
    }

    @Transactional
    public void update(Long id, FreeWriteRequestDto dto, List<MultipartFile> files, Long currentUserId, boolean isAdmin) {
        FreePost post = findById(id);
        checkPermission(post.getUser().getId(), currentUserId, isAdmin);
        validateFileCount(id, dto.getDeleteAttachmentIds(), files);

        // 삭제 요청된 첨부파일 삭제
        if (dto.getDeleteAttachmentIds() != null) {
            for (Long attachmentId : dto.getDeleteAttachmentIds()) {
                attachmentRepository.findById(attachmentId).ifPresent(attachmentService::deleteFile);
            }
        }

        // 남은 파일 수 뒤에 새 파일 추가
        int currentCount = (int) attachmentRepository.countByBoardTypeAndPostId(BoardType.FREE, id);
        attachmentService.saveFiles(files, BoardType.FREE, id, currentCount);

        post.update(dto.getCategory(), dto.getTitle(), dto.getContent());
    }

    @Transactional
    public void delete(Long id, Long currentUserId, boolean isAdmin) {
        FreePost post = findById(id);
        checkPermission(post.getUser().getId(), currentUserId, isAdmin);

        attachmentService.deleteAllByPost(BoardType.FREE, id);
        commentRepository.deleteByBoardTypeAndPostId(BoardType.FREE, id);
        freeRepository.delete(post);
    }

    public List<FreePost> getTop6ForMain() {
        return freeRepository.findTop6ByOrderByCreatedAtDesc();
    }

    private FreePost findById(Long id) {
        return freeRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
    }

    private void checkPermission(Long authorId, Long currentUserId, boolean isAdmin) {
        if (!authorId.equals(currentUserId) && !isAdmin) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
    }

    private void validateFileCount(Long postId, List<Long> deleteIds, List<MultipartFile> files) {
        int newCount = (files == null) ? 0 : (int) files.stream().filter(f -> !f.isEmpty()).count();
        int maxCount = filePolicyProperties.getFree().getMaxCount();

        if (postId == null) {
            // 신규 작성
            if (newCount > maxCount) throw new CustomException(ErrorCode.FILE_COUNT_EXCEEDED);
        } else {
            // 수정
            attachmentService.validateFreeCount(postId, deleteIds, newCount);
        }
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

    private FreeCategory resolveCategory(String s) {
        if (s == null || s.isBlank()) return null;
        try { return FreeCategory.valueOf(s); } catch (IllegalArgumentException e) { return null; }
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
