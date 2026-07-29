package gcy.system.controller;

import gcy.system.entity.dto.Result;
import gcy.system.entity.pojo.ReviewComment;
import gcy.system.service.IReviewCommentService;
import gcy.system.utils.UserHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 评审评论控制器
 * <p>
 * 提供评审评论相关的 REST API 接口，包括评论列表查询、新增评论和删除评论功能。
 * 所有接口均需要用户登录，通过 {@link UserHolder} 获取当前登录用户信息。
 * </p>
 *
 * @author 郭名城
 * @date 2026-07-30
 */
@RestController
@RequestMapping("/review-comment")
@RequiredArgsConstructor
public class ReviewCommentController {

    private final IReviewCommentService reviewCommentService;

    /**
     * 根据评审ID查询评论列表
     * <p>
     * 通过 GET 请求获取指定评审下的所有评论，包括评论的层级结构（父评论与子回复）。
     * 当前登录用户用于判断每条评论是否属于该用户，以便前端控制编辑和删除权限。
     * </p>
     *
     * @param reviewId 评审ID，对应评审记录的唯一标识
     * @return 包含评论列表数据的 {@link Result} 对象
     */
    @GetMapping("/list/{reviewId}")
    public Result list(@PathVariable Long reviewId) {
        Long userId = UserHolder.getUser().getId();
        return reviewCommentService.getCommentsByReviewId(reviewId, userId);
    }

    /**
     * 新增评论
     * <p>
     * 通过 POST 请求为评审添加一条新评论或回复。
     * 请求体中的 {@link ReviewComment} 对象包含评论内容、所属评审ID以及可选的父评论ID（用于回复场景）。
     * </p>
     *
     * @param comment 评论实体对象，包含评论内容、评审ID、父评论ID等信息
     * @return 包含新增评论结果的 {@link Result} 对象
     */
    @PostMapping("/add")
    public Result add(@RequestBody ReviewComment comment) {
        Long userId = UserHolder.getUser().getId();
        return reviewCommentService.addComment(comment, userId);
    }

    /**
     * 删除评论
     * <p>
     * 通过 DELETE 请求删除指定ID的评论。仅评论作者可以删除自己的评论，
     * 删除操作会校验当前登录用户是否为评论的创建者。
     * </p>
     *
     * @param commentId 评论ID，对应评论记录的唯一标识
     * @return 包含删除操作结果的 {@link Result} 对象
     */
    @DeleteMapping("/{commentId}")
    public Result delete(@PathVariable Long commentId) {
        Long userId = UserHolder.getUser().getId();
        return reviewCommentService.deleteComment(commentId, userId);
    }
}
