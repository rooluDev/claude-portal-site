package com.portfolio.board.inquiry;

import com.portfolio.board.common.BoardListRequestDto;
import com.portfolio.board.common.PageResponseDto;
import com.portfolio.common.response.ApiResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class InquiryController {

    private final InquiryService inquiryService;

    // ── 문의 게시글 ──────────────────────────────────────

    @GetMapping("/api/inquiry")
    public ResponseEntity<ApiResponse<PageResponseDto<InquiryListResponseDto>>> getList(
            @ModelAttribute BoardListRequestDto dto,
            @AuthenticationPrincipal Long currentUserId,
            Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(
                inquiryService.getList(dto, currentUserId, isAdmin(authentication))));
    }

    @GetMapping("/api/inquiry/{id}")
    public ResponseEntity<ApiResponse<InquiryDetailResponseDto>> getDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal Long currentUserId,
            Authentication authentication,
            HttpSession session) {
        return ResponseEntity.ok(ApiResponse.success(
                inquiryService.getDetail(id, currentUserId, isAdmin(authentication), session)));
    }

    @PostMapping("/api/inquiry")
    public ResponseEntity<ApiResponse<Map<String, Long>>> create(
            @RequestBody @Valid InquiryWriteRequestDto dto,
            @AuthenticationPrincipal Long currentUserId) {
        Long id = inquiryService.create(dto, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("게시글이 등록되었습니다.", Map.of("id", id)));
    }

    @PutMapping("/api/inquiry/{id}")
    public ResponseEntity<ApiResponse<Void>> update(
            @PathVariable Long id,
            @RequestBody @Valid InquiryWriteRequestDto dto,
            @AuthenticationPrincipal Long currentUserId,
            Authentication authentication) {
        inquiryService.update(id, dto, currentUserId, isAdmin(authentication));
        return ResponseEntity.ok(ApiResponse.success("게시글이 수정되었습니다.", null));
    }

    @DeleteMapping("/api/inquiry/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal Long currentUserId,
            Authentication authentication) {
        inquiryService.delete(id, currentUserId, isAdmin(authentication));
        return ResponseEntity.ok(ApiResponse.success("게시글이 삭제되었습니다.", null));
    }

    // ── 관리자 답변 ──────────────────────────────────────

    @PostMapping("/api/inquiry-answer/{inquiryId}")
    public ResponseEntity<ApiResponse<Void>> createAnswer(
            @PathVariable Long inquiryId,
            @RequestBody @Valid InquiryAnswerRequestDto dto,
            @AuthenticationPrincipal Long adminId) {
        inquiryService.createAnswer(inquiryId, dto, adminId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("답변이 등록되었습니다.", null));
    }

    @PutMapping("/api/inquiry-answer/{inquiryId}")
    public ResponseEntity<ApiResponse<Void>> updateAnswer(
            @PathVariable Long inquiryId,
            @RequestBody @Valid InquiryAnswerRequestDto dto) {
        inquiryService.updateAnswer(inquiryId, dto);
        return ResponseEntity.ok(ApiResponse.success("답변이 수정되었습니다.", null));
    }

    @DeleteMapping("/api/inquiry-answer/{inquiryId}")
    public ResponseEntity<ApiResponse<Void>> deleteAnswer(
            @PathVariable Long inquiryId) {
        inquiryService.deleteAnswer(inquiryId);
        return ResponseEntity.ok(ApiResponse.success("답변이 삭제되었습니다.", null));
    }

    private boolean isAdmin(Authentication auth) {
        if (auth == null) return false;
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
