package com.portfolio.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 400 Bad Request
    INVALID_INPUT           (400, "입력값이 올바르지 않습니다."),
    INVALID_CATEGORY        (400, "올바르지 않은 카테고리 값입니다."),
    INVALID_DATE_RANGE      (400, "날짜 범위가 올바르지 않습니다."),
    FILE_COUNT_EXCEEDED     (400, "첨부파일은 최대 %d개까지 업로드할 수 있습니다."),
    FILE_SIZE_EXCEEDED      (400, "파일 크기는 %dMB를 초과할 수 없습니다."),
    FILE_EXTENSION_INVALID  (400, "이미지 파일만 업로드할 수 있습니다."),
    COMMENT_TOO_LONG        (400, "댓글은 1000자를 초과할 수 없습니다."),

    // 401 Unauthorized
    UNAUTHORIZED            (401, "로그인이 필요합니다."),
    LOGIN_FAILED            (401, "아이디 또는 비밀번호가 올바르지 않습니다."),
    TOKEN_EXPIRED           (401, "로그인 세션이 만료되었습니다. 다시 로그인해주세요."),

    // 403 Forbidden
    FORBIDDEN               (403, "접근 권한이 없습니다."),
    SECRET_POST_FORBIDDEN   (403, "비밀글은 작성자와 관리자만 열람할 수 있습니다."),
    ANSWERED_POST_FORBIDDEN (403, "답변이 완료된 문의는 수정할 수 없습니다."),

    // 404 Not Found
    NOT_FOUND               (404, "요청한 리소스를 찾을 수 없습니다."),
    POST_NOT_FOUND          (404, "게시글을 찾을 수 없습니다."),
    COMMENT_NOT_FOUND       (404, "댓글을 찾을 수 없습니다."),
    ATTACHMENT_NOT_FOUND    (404, "첨부파일을 찾을 수 없습니다."),
    ANSWER_NOT_FOUND        (404, "답변을 찾을 수 없습니다."),
    FILE_NOT_FOUND          (404, "요청한 파일을 찾을 수 없습니다."),

    // 409 Conflict
    DUPLICATE_USERNAME      (409, "이미 사용 중인 아이디입니다."),
    ANSWER_ALREADY_EXISTS   (409, "이미 답변이 등록된 문의입니다."),

    // 500 Internal Server Error
    SERVER_ERROR            (500, "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요."),
    FILE_UPLOAD_FAILED      (500, "파일 업로드 중 오류가 발생했습니다."),
    FILE_DELETE_FAILED      (500, "파일 삭제 중 오류가 발생했습니다.");

    private final int status;
    private final String message;
}
