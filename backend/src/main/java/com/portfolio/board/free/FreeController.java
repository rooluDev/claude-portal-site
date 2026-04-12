package com.portfolio.board.free;

import com.portfolio.board.common.BoardListRequestDto;
import com.portfolio.board.common.PageResponseDto;
import com.portfolio.common.response.ApiResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/free")
@RequiredArgsConstructor
public class FreeController {

    private final FreeService freeService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponseDto<FreeListResponseDto>>> getList(
            @ModelAttribute BoardListRequestDto dto) {
        return ResponseEntity.ok(ApiResponse.success(freeService.getList(dto)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FreeDetailResponseDto>> getDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal Long currentUserId,
            Authentication authentication,
            HttpSession session) {
        return ResponseEntity.ok(ApiResponse.success(
                freeService.getDetail(id, currentUserId, isAdmin(authentication), session)));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Map<String, Long>>> create(
            @RequestPart("data") @Valid FreeWriteRequestDto dto,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @AuthenticationPrincipal Long currentUserId) {
        Long id = freeService.create(dto, files, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("게시글이 등록되었습니다.", Map.of("id", id)));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Void>> update(
            @PathVariable Long id,
            @RequestPart("data") @Valid FreeWriteRequestDto dto,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @AuthenticationPrincipal Long currentUserId,
            Authentication authentication) {
        freeService.update(id, dto, files, currentUserId, isAdmin(authentication));
        return ResponseEntity.ok(ApiResponse.success("게시글이 수정되었습니다.", null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal Long currentUserId,
            Authentication authentication) {
        freeService.delete(id, currentUserId, isAdmin(authentication));
        return ResponseEntity.ok(ApiResponse.success("게시글이 삭제되었습니다.", null));
    }

    private boolean isAdmin(Authentication auth) {
        if (auth == null) return false;
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
