package gcy.system.controller;

import gcy.system.entity.dto.Result;
import gcy.system.entity.pojo.ReviewComment;
import gcy.system.service.IReviewCommentService;
import gcy.system.utils.UserHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/review-comment")
@RequiredArgsConstructor
public class ReviewCommentController {

    private final IReviewCommentService reviewCommentService;

    @GetMapping("/list/{reviewId}")
    public Result list(@PathVariable Long reviewId) {
        Long userId = UserHolder.getUser().getId();
        return reviewCommentService.getCommentsByReviewId(reviewId, userId);
    }

    @PostMapping("/add")
    public Result add(@RequestBody ReviewComment comment) {
        Long userId = UserHolder.getUser().getId();
        return reviewCommentService.addComment(comment, userId);
    }

    @DeleteMapping("/{commentId}")
    public Result delete(@PathVariable Long commentId) {
        Long userId = UserHolder.getUser().getId();
        return reviewCommentService.deleteComment(commentId, userId);
    }
}
