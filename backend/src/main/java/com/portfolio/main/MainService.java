package com.portfolio.main;

import com.portfolio.attachment.repository.AttachmentRepository;
import com.portfolio.attachment.service.AttachmentService;
import com.portfolio.board.common.BoardType;
import com.portfolio.board.free.FreePost;
import com.portfolio.board.free.FreeService;
import com.portfolio.board.gallery.GalleryPost;
import com.portfolio.board.gallery.GalleryService;
import com.portfolio.board.inquiry.Inquiry;
import com.portfolio.board.inquiry.InquiryService;
import com.portfolio.board.notice.Notice;
import com.portfolio.board.notice.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MainService {

    private final NoticeService noticeService;
    private final FreeService freeService;
    private final GalleryService galleryService;
    private final InquiryService inquiryService;
    private final AttachmentRepository attachmentRepository;
    private final AttachmentService attachmentService;

    @Transactional(readOnly = true)
    public MainResponseDto getMainData() {
        List<Notice> notices = noticeService.getTop6ForMain();
        List<MainResponseDto.NoticeItem> noticeItems = notices.stream()
                .map(MainResponseDto.NoticeItem::new)
                .collect(Collectors.toList());

        List<FreePost> freePosts = freeService.getTop6ForMain();
        List<MainResponseDto.FreeItem> freeItems = freePosts.stream()
                .map(MainResponseDto.FreeItem::new)
                .collect(Collectors.toList());

        List<GalleryPost> galleryPosts = galleryService.getTop4ForMain();
        List<MainResponseDto.GalleryItem> galleryItems = galleryPosts.stream()
                .map(post -> {
                    var thumbnail = attachmentRepository
                            .findFirstByBoardTypeAndPostIdOrderBySortOrderAsc(BoardType.GALLERY, post.getId())
                            .orElse(null);
                    return new MainResponseDto.GalleryItem(post, thumbnail, attachmentService);
                })
                .collect(Collectors.toList());

        List<Inquiry> inquiries = inquiryService.getTop4ForMain();
        List<MainResponseDto.InquiryItem> inquiryItems = inquiries.stream()
                .map(MainResponseDto.InquiryItem::new)
                .collect(Collectors.toList());

        return new MainResponseDto(noticeItems, freeItems, galleryItems, inquiryItems);
    }
}
