package com.portfolio.board.notice;

import com.portfolio.board.common.BoardListRequestDto;
import com.portfolio.board.common.PageResponseDto;
import com.portfolio.board.common.ViewCountService;
import com.portfolio.common.exception.CustomException;
import com.portfolio.common.exception.ErrorCode;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final UserRepository userRepository;
    private final ViewCountService viewCountService;

    @Transactional(readOnly = true)
    public PageResponseDto<NoticeListResponseDto> getList(BoardListRequestDto dto) {
        LocalDateTime startDate = parseStartDate(dto.getStartDate());
        LocalDateTime endDate = parseEndDate(dto.getEndDate());
        String searchText = resolveSearchText(dto.getSearchText());
        NoticeCategory category = resolveCategory(dto.getCategory());

        // isPinned DESC 항상 1순위
        Sort sort = Sort.by(Sort.Order.desc("isPinned"))
                .and(Sort.by(Sort.Direction.fromString(dto.getOrderDirection()),
                        mapOrderField(dto.getOrderValue())));
        Pageable pageable = PageRequest.of(dto.getPageNum() - 1, dto.getPageSize(), sort);

        Page<Notice> page = noticeRepository.findByCondition(startDate, endDate, category, searchText, pageable);
        List<NoticeListResponseDto> content = page.getContent().stream()
                .map(NoticeListResponseDto::new)
                .collect(Collectors.toList());

        return new PageResponseDto<>(page, content);
    }

    @Transactional
    public NoticeDetailResponseDto getDetail(Long id, Long currentUserId, boolean isAdmin, HttpSession session) {
        Notice notice = findById(id);

        if (viewCountService.isNewView("NOTICE", id, session.getId())) {
            notice.incrementViewCount();
        }

        return new NoticeDetailResponseDto(notice, isAdmin);
    }

    @Transactional
    public Long create(NoticeWriteRequestDto dto, Long adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED));

        Notice notice = Notice.builder()
                .user(admin)
                .category(dto.getCategory())
                .isPinned(dto.getIsPinned())
                .title(dto.getTitle())
                .content(dto.getContent())
                .build();

        return noticeRepository.save(notice).getId();
    }

    @Transactional
    public void update(Long id, NoticeWriteRequestDto dto) {
        Notice notice = findById(id);
        notice.update(dto.getCategory(), dto.getIsPinned(), dto.getTitle(), dto.getContent());
    }

    @Transactional
    public void delete(Long id) {
        Notice notice = findById(id);
        noticeRepository.delete(notice);
    }

    // 메인 위젯용
    @Transactional(readOnly = true)
    public List<Notice> getTop6ForMain() {
        return noticeRepository.findTop6ByOrderByIsPinnedDescCreatedAtDesc();
    }

    private Notice findById(Long id) {
        return noticeRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
    }

    private LocalDateTime parseStartDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return LocalDateTime.now().minusYears(1).with(LocalTime.MIN);
        }
        try {
            return LocalDate.parse(dateStr).atStartOfDay();
        } catch (DateTimeParseException e) {
            return LocalDateTime.now().minusYears(1).with(LocalTime.MIN);
        }
    }

    private LocalDateTime parseEndDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return LocalDateTime.now().with(LocalTime.MAX);
        }
        try {
            return LocalDate.parse(dateStr).atTime(LocalTime.MAX);
        } catch (DateTimeParseException e) {
            return LocalDateTime.now().with(LocalTime.MAX);
        }
    }

    private String resolveSearchText(String searchText) {
        if (searchText == null || searchText.length() < 2) return null;
        return searchText;
    }

    private NoticeCategory resolveCategory(String category) {
        if (category == null || category.isBlank()) return null;
        try {
            return NoticeCategory.valueOf(category);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private String mapOrderField(String orderValue) {
        return switch (orderValue) {
            case "title" -> "title";
            case "viewCount" -> "viewCount";
            case "category" -> "category";
            default -> "createdAt";
        };
    }
}
