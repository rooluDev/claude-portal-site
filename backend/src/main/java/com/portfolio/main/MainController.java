package com.portfolio.main;

import com.portfolio.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/main")
@RequiredArgsConstructor
public class MainController {

    private final MainService mainService;

    @GetMapping
    public ResponseEntity<ApiResponse<MainResponseDto>> getMainData() {
        return ResponseEntity.ok(ApiResponse.success(mainService.getMainData()));
    }
}
