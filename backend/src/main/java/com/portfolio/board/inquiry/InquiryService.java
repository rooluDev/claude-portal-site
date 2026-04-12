package com.portfolio.board.inquiry;

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
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final InquiryAnswerRepository inquiryAnswerRepository;
    private final UserRepository userRepository;
    private final ViewCountService viewCountService;

    @Transactional(readOnly = true)
    public PageResponseDto<InquiryListResponseDto> getList(BoardListRequestDto dto,
                                                           Long currentUserId, boolean isAdmin) {
        LocalDateTime startDate = parseStartDate(dto.getStartDate());
        LocalDateTime endDate = parseEndDate(dto.getEndDate());
        String searchText = resolveSearchText(dto.getSearchText());
        AnswerStatus answerStatus = resolveAnswerStatus(dto.getCategory());
        Long filterUserId = (Boolean.TRUE.equals(dto.getMy()) && currentUserId != null) ? currentUserId : null;

        Sort sort = Sort.by(Sort.Direction.fromString(dto.getOrderDirection()),
                mapOrderField(dto.getOrderValue()));
        Pageable pageable = PageRequest.of(dto.getPageNum() - 1, dto.getPageSize(), sort);

        Page<Inquiry> page = inquiryRepository.findByCondition(
                startDate, endDate, answerStatus, searchText, filterUserId, pageable);

        List<InquiryListResponseDto> content = page.getContent().stream()
                .map(inquiry -> {
                    boolean canSeeTitle = !inquiry.isSecret() || isAdmin ||
                            (currentUserId != null && inquiry.getUser().getId().equals(currentUserId));
                    return new InquiryListResponseDto(inquiry, canSeeTitle);
                })
                .collect(Collectors.toList());

        return new PageResponseDto<>(page, content);
    }

    @Transactional
    public InquiryDetailResponseDto getDetail(Long id, Long currentUserId, boolean isAdmin, HttpSession session) {
        Inquiry inquiry = findById(id);

        // 비밀글 접근 제어
        if (inquiry.isSecret()) {
            if (currentUserId == null) throw new CustomException(ErrorCode.UNAUTHORIZED);
            if (!isAdmin && !inquiry.getUser().getId().equals(currentUserId)) {
                throw new CustomException(ErrorCode.SECRET_POST_FORBIDDEN);
            }
        }

        if (viewCountService.isNewView("INQUIRY", id, session.getId())) {
            inquiry.incrementViewCount();
        }

        boolean isEditable = currentUserId != null &&
                (inquiry.getUser().getId().equals(currentUserId) || isAdmin) &&
                inquiry.getAnswerStatus() != AnswerStatus.ANSWERED;

        InquiryAnswer answer = inquiryAnswerRepository.findByInquiryId(id).orElse(null);

        return new InquiryDetailResponseDto(inquiry, isEditable, answer);
    }

    @Transactional
    public Long create(InquiryWriteRequestDto dto, Long currentUserId) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED));

        Inquiry inquiry = Inquiry.builder()
                .user(user)
                .title(dto.getTitle())
                .content(dto.getContent())
                .isSecret(dto.isSecret())
                .build();
        inquiryRepository.save(inquiry);
        return inquiry.getId();
    }

    @Transactional
    public void update(Long id, InquiryWriteRequestDto dto, Long currentUserId, boolean isAdmin) {
        Inquiry inquiry = findById(id);
        checkPermission(inquiry.getUser().getId(), currentUserId, isAdmin);

        if (inquiry.getAnswerStatus() == AnswerStatus.ANSWERED) {
            throw new CustomException(ErrorCode.ANSWERED_POST_FORBIDDEN);
        }

        inquiry.update(dto.getTitle(), dto.getContent(), dto.isSecret());
    }

    @Transactional
    public void delete(Long id, Long currentUserId, boolean isAdmin) {
        Inquiry inquiry = findById(id);
        checkPermission(inquiry.getUser().getId(), currentUserId, isAdmin);
        // inquiry_answer는 ON DELETE CASCADE로 자동 삭제
        inquiryRepository.delete(inquiry);
    }

    // 관리자 답변 등록
    @Transactional
    public void createAnswer(Long inquiryId, InquiryAnswerRequestDto dto, Long adminId) {
        Inquiry inquiry = findById(inquiryId);

        if (inquiryAnswerRepository.existsByInquiryId(inquiryId)) {
            throw new CustomException(ErrorCode.ANSWER_ALREADY_EXISTS);
        }

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED));

        InquiryAnswer answer = InquiryAnswer.builder()
                .inquiryId(inquiryId)
                .admin(admin)
                .content(dto.getContent())
                .build();
        inquiryAnswerRepository.save(answer);
        inquiry.markAnswered();
    }

    // 관리자 답변 수정
    @Transactional
    public void updateAnswer(Long inquiryId, InquiryAnswerRequestDto dto) {
        InquiryAnswer answer = inquiryAnswerRepository.findByInquiryId(inquiryId)
                .orElseThrow(() -> new CustomException(ErrorCode.ANSWER_NOT_FOUND));
        answer.update(dto.getContent());
    }

    // 관리자 답변 삭제
    @Transactional
    public void deleteAnswer(Long inquiryId) {
        Inquiry inquiry = findById(inquiryId);
        InquiryAnswer answer = inquiryAnswerRepository.findByInquiryId(inquiryId)
                .orElseThrow(() -> new CustomException(ErrorCode.ANSWER_NOT_FOUND));
        inquiryAnswerRepository.delete(answer);
        inquiry.markPending();
    }

    public List<Inquiry> getTop4ForMain() {
        return inquiryRepository.findTop4ByOrderByCreatedAtDesc();
    }

    private Inquiry findById(Long id) {
        return inquiryRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
    }

    private void checkPermission(Long authorId, Long currentUserId, boolean isAdmin) {
        if (!authorId.equals(currentUserId) && !isAdmin) {
            throw new CustomException(ErrorCode.FORBIDDEN);
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

    private AnswerStatus resolveAnswerStatus(String s) {
        if (s == null || s.isBlank()) return null;
        try { return AnswerStatus.valueOf(s); } catch (IllegalArgumentException e) { return null; }
    }

    private String mapOrderField(String v) {
        return switch (v) {
            case "title" -> "title";
            case "viewCount" -> "viewCount";
            case "answerStatus" -> "answerStatus";
            default -> "createdAt";
        };
    }
}
