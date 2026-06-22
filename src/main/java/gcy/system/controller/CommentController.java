package gcy.system.controller;

import gcy.system.entity.dto.Result;
import gcy.system.entity.pojo.CommentAppend;
import gcy.system.entity.pojo.GoodsComment;
import gcy.system.service.ICommentService;
import gcy.system.utils.FileUploadUtil;
import gcy.system.utils.UserHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/comment")
@RequiredArgsConstructor
public class CommentController {

    private final ICommentService commentService;

    @GetMapping("/list/{goodsId}")
    public Result list(@PathVariable Long goodsId,
                       @RequestParam(defaultValue = "1") Integer current,
                       @RequestParam(defaultValue = "10") Integer size) {
        Long userId = UserHolder.getUser().getId();
        return commentService.getCommentsByGoodsId(goodsId, userId, current, size);
    }

    @GetMapping("/list/order/{orderId}")
    public Result listByOrderId(@PathVariable Long orderId) {
        Long userId = UserHolder.getUser().getId();
        return commentService.getCommentsByOrderId(orderId, userId);
    }

    @PostMapping("/add")
    public Result add(@RequestBody GoodsComment comment) {
        Long userId = UserHolder.getUser().getId();
        return commentService.addComment(comment, userId);
    }

    @PostMapping("/append")
    public Result append(@RequestBody CommentAppend append) {
        Long userId = UserHolder.getUser().getId();
        return commentService.appendComment(append, userId);
    }

    @DeleteMapping("/{commentId}")
    public Result delete(@PathVariable Long commentId) {
        Long userId = UserHolder.getUser().getId();
        return commentService.deleteComment(commentId, userId);
    }

    @DeleteMapping("/append/{appendId}")
    public Result deleteAppend(@PathVariable Long appendId) {
        Long userId = UserHolder.getUser().getId();
        return commentService.deleteAppend(appendId, userId);
    }

    @DeleteMapping("/review/{reviewId}")
    public Result deleteReview(@PathVariable Long reviewId) {
        Long userId = UserHolder.getUser().getId();
        return commentService.deleteReview(reviewId, userId);
    }

    @PostMapping("/upload/image")
    public Result uploadImage(@RequestParam("file") MultipartFile file) {
        String url = FileUploadUtil.upload(file, "comment/image");
        return Result.ok(url);
    }

    @PostMapping("/upload/video")
    public Result uploadVideo(@RequestParam("file") MultipartFile file) {
        String url = FileUploadUtil.upload(file, "comment/video");
        return Result.ok(url);
    }
}
