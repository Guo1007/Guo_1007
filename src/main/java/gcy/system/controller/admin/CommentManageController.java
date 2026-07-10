package gcy.system.controller.admin;

import gcy.system.entity.dto.Result;
import gcy.system.service.admin.ICommentManageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/comment")
@RequiredArgsConstructor
public class CommentManageController {

    private final ICommentManageService commentManageService;

    @GetMapping("/list")
    public Result getAllComments(@RequestParam(defaultValue = "1") Integer current,
                                 @RequestParam(defaultValue = "10") Integer size) {
        return commentManageService.getAllComments(current, size);
    }

    @PutMapping("/approve/{id}")
    public Result approveComment(@PathVariable Long id) {
        return commentManageService.approveComment(id);
    }

    @PutMapping("/reject/{id}")
    public Result rejectComment(@PathVariable Long id) {
        return commentManageService.rejectComment(id);
    }

    @GetMapping("/append/list")
    public Result getAllAppends(@RequestParam(defaultValue = "1") Integer current,
                                @RequestParam(defaultValue = "10") Integer size) {
        return commentManageService.getAllAppends(current, size);
    }

    @PutMapping("/append/approve/{id}")
    public Result approveAppend(@PathVariable Long id) {
        return commentManageService.approveAppend(id);
    }

    @PutMapping("/append/reject/{id}")
    public Result rejectAppend(@PathVariable Long id) {
        return commentManageService.rejectAppend(id);
    }

    @GetMapping("/review-comment/list")
    public Result getAllReviewComments(@RequestParam(defaultValue = "1") Integer current,
                                       @RequestParam(defaultValue = "10") Integer size) {
        return commentManageService.getAllReviewComments(current, size);
    }

    @PutMapping("/review-comment/approve/{id}")
    public Result approveReviewComment(@PathVariable Long id) {
        return commentManageService.approveReviewComment(id);
    }

    @PutMapping("/review-comment/reject/{id}")
    public Result rejectReviewComment(@PathVariable Long id) {
        return commentManageService.rejectReviewComment(id);
    }

    // ========== 删除 ==========

    @DeleteMapping("/{id}")
    public Result deleteComment(@PathVariable Long id) {
        return commentManageService.deleteComment(id);
    }

    @DeleteMapping("/batch")
    public Result batchDeleteComments(@RequestBody List<Long> ids) {
        return commentManageService.batchDeleteComments(ids);
    }

    @DeleteMapping("/append/{id}")
    public Result deleteAppend(@PathVariable Long id) {
        return commentManageService.deleteAppend(id);
    }

    @DeleteMapping("/append/batch")
    public Result batchDeleteAppends(@RequestBody List<Long> ids) {
        return commentManageService.batchDeleteAppends(ids);
    }

    @DeleteMapping("/review-comment/{id}")
    public Result deleteReviewComment(@PathVariable Long id) {
        return commentManageService.deleteReviewComment(id);
    }

    @DeleteMapping("/review-comment/batch")
    public Result batchDeleteReviewComments(@RequestBody List<Long> ids) {
        return commentManageService.batchDeleteReviewComments(ids);
    }
}
