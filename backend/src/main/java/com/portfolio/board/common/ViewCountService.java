package com.portfolio.board.common;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class ViewCountService {

    // key: "{boardType}_{postId}", value: Set<sessionId>
    private final ConcurrentHashMap<String, ConcurrentHashMap.KeySetView<String, Boolean>> viewedMap
            = new ConcurrentHashMap<>();

    /**
     * 동일 세션에서 이미 조회한 게시글이면 false, 새 조회면 true 반환
     */
    public boolean isNewView(String boardType, Long postId, String sessionId) {
        String key = boardType + "_" + postId;
        return viewedMap.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet())
                        .add(sessionId);
    }
}
