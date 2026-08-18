package gcy.system.listener;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import dev.langchain4j.model.chat.ChatModel;
import gcy.system.entity.pojo.CommentAppend;
import gcy.system.entity.pojo.GoodsComment;
import gcy.system.entity.pojo.ReviewComment;
import gcy.system.mapper.CommentAppendMapper;
import gcy.system.mapper.GoodsCommentMapper;
import gcy.system.mapper.ReviewCommentMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * AI 自动审核消费者。
 * <p>
 * 监听评论、追评、评价回复的自动审核消息，调用 AI 大模型进行内容审核。
 * AI 判定通过则直接设为 status=1（已通过），判定不通过则设为 status=3（待人工复审）。
 * 支持并发消费（最多 5 个线程），内置幂等判断防止重复消费。
 * </p>
 *
 * @author 郭名城
 * @date 2026-08-17
 */
@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
        topic = "comment-auto-review-topic",
        consumerGroup = "ai-review-consumer",
        consumeThreadMax = 20,
        consumeMode = ConsumeMode.CONCURRENTLY
)
public class AiReviewConsumer implements RocketMQListener<String> {

    private final ChatModel openAiChatModel;

    private final GoodsCommentMapper goodsCommentMapper;

    private final CommentAppendMapper commentAppendMapper;

    private final ReviewCommentMapper reviewCommentMapper;

    /**
     * 处理AI自动审核消息。
     * <p>
     * 解析消息体获取评论类型和ID，查询对应内容，调用AI审核后更新状态。
     * 内置幂等判断：若评论状态已不是 0（待审核），则跳过。
     * AI 调用失败时抛出异常，由 RocketMQ 自动重试。
     * </p>
     *
     * @param message 包含审核目标类型和ID的JSON消息
     */
    @Override
    public void onMessage(String message) {
        AiReviewMessage msg;
        try {
            msg = JSONUtil.toBean(message, AiReviewMessage.class);
        } catch (Exception e) {
            log.error("AI审核消息解析失败: {}", message, e);
            return;
        }

        switch (msg.getType()) {
            case "goods_comment" -> reviewGoodsComment(msg.getId());
            case "comment_append" -> reviewCommentAppend(msg.getId());
            case "review_comment" -> reviewReviewComment(msg.getId());
            default -> log.warn("未知的AI审核消息类型: {}", msg.getType());
        }
    }

    /**
     * AI审核商品评价。
     */
    private void reviewGoodsComment(Long commentId) {
        GoodsComment comment = goodsCommentMapper.selectById(commentId);
        if (comment == null || comment.getStatus() != 0) {
            return; // 幂等：已处理过
        }
        AiReviewResult result = aiReview(comment.getContent());
        int newStatus = result.isPass() ? 1 : 3;
        LambdaUpdateWrapper<GoodsComment> wrapper = new LambdaUpdateWrapper<GoodsComment>()
                .eq(GoodsComment::getId, commentId)
                .set(GoodsComment::getStatus, newStatus);
        if (!result.isPass()) {
            wrapper.set(GoodsComment::getAiRejectReason, result.getRejectReason());
        }
        goodsCommentMapper.update(null, wrapper);
        log.info("AI审核商品评价: id={}, result={}", commentId, result.isPass() ? "通过" : "待人工复审");
    }

    /**
     * AI审核追评。
     */
    private void reviewCommentAppend(Long appendId) {
        CommentAppend append = commentAppendMapper.selectById(appendId);
        if (append == null || append.getStatus() != 0) {
            return; // 幂等：已处理过
        }
        AiReviewResult result = aiReview(append.getAppendContent());
        int newStatus = result.isPass() ? 1 : 3;
        LambdaUpdateWrapper<CommentAppend> wrapper = new LambdaUpdateWrapper<CommentAppend>()
                .eq(CommentAppend::getId, appendId)
                .set(CommentAppend::getStatus, newStatus);
        if (!result.isPass()) {
            wrapper.set(CommentAppend::getAiRejectReason, result.getRejectReason());
        }
        commentAppendMapper.update(null, wrapper);
        log.info("AI审核追评: id={}, result={}", appendId, result.isPass() ? "通过" : "待人工复审");
    }

    /**
     * AI审核评价回复。
     */
    private void reviewReviewComment(Long commentId) {
        ReviewComment comment = reviewCommentMapper.selectById(commentId);
        if (comment == null || comment.getStatus() != 0) {
            return; // 幂等：已处理过
        }
        AiReviewResult result = aiReview(comment.getContent());
        int newStatus = result.isPass() ? 1 : 3;
        LambdaUpdateWrapper<ReviewComment> wrapper = new LambdaUpdateWrapper<ReviewComment>()
                .eq(ReviewComment::getId, commentId)
                .set(ReviewComment::getStatus, newStatus);
        if (!result.isPass()) {
            wrapper.set(ReviewComment::getAiRejectReason, result.getRejectReason());
        }
        reviewCommentMapper.update(null, wrapper);
        log.info("AI审核评价回复: id={}, result={}", commentId, result.isPass() ? "通过" : "待人工复审");
    }

    /**
     * 调用AI大模型进行内容审核。
     * <p>
     * 通过预设的审核 prompt 让 AI 判断评论内容是否合规。
     * 返回 true 表示审核通过，false 表示需要人工复审。
     * AI 拒绝时会将拒绝原因写入数据库。
     * </p>
     *
     * @param content 待审核的评论内容
     * @return true=通过，false=需人工复审
     */
    private AiReviewResult aiReview(String content) {
        if (content == null || content.trim().isEmpty()) {
            return new AiReviewResult(true, null);
        }
        String prompt = buildReviewPrompt(content);
        try {
            String response = openAiChatModel.chat(prompt);
            log.debug("AI审核原始响应: {}", response);
            return parseAiResponse(response);
        } catch (Exception e) {
            log.error("AI审核调用失败，内容: {}", content, e);
            throw new RuntimeException("AI审核调用失败", e);
        }
    }

    /**
     * 解析AI审核响应。
     * <p>
     * 期望格式：PASS 或 FAIL: 拒绝原因
     * </p>
     *
     * @param response AI原始响应
     * @return 审核结果
     */
    private AiReviewResult parseAiResponse(String response) {
        if (response == null) return new AiReviewResult(true, null);
        String trimmed = response.trim();
        if (trimmed.startsWith("FAIL")) {
            String reason = trimmed.length() > 4 ? trimmed.substring(4).trim() : null;
            if (reason != null && reason.startsWith(":")) {
                reason = reason.substring(1).trim();
            }
            if (reason == null || reason.isEmpty()) {
                reason = "内容不符合审核规则";
            }
            return new AiReviewResult(false, reason);
        }
        return new AiReviewResult(true, null);
    }

    /**
     * 构建AI审核的提示词。
     * <p>
     * 要求AI输出 PASS 或 FAIL: 拒绝原因，并给出明确的审核规则。
     * </p>
     *
     * @param content 待审核的评论内容
     * @return 完整的审核 prompt
     */
    private String buildReviewPrompt(String content) {
        return """
                你是一个电商平台的内容审核员。请审核以下用户评论内容是否合规。
                
                审核规则：
                1. 包含广告、推广链接、联系方式（手机号、微信号、QQ号等）→ 不通过
                2. 包含辱骂、人身攻击、色情、政治敏感内容 → 不通过
                3. 内容与商品完全无关的灌水内容（如"哈哈哈"、"test"、"123"）→ 不通过
                4. 正常的产品评价、使用感受、提问、追评 → 通过
                5. 也要智能识别一下内容是否有隐含的违规内容，比如谐音字、谐音字母、甚至象形字等，如果有 → 不通过
                
                输出格式：
                - 如果通过，只输出：PASS
                - 如果不通过，输出：FAIL: 拒绝原因（一句话简要说明，如"包含广告内容"、"包含辱骂词汇"、"与商品无关的灌水内容"等）
                
                待审核内容：
                %s
                """.formatted(content);
    }

    /**
     * AI审核结果。
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AiReviewResult {
        /**
         * 是否通过
         */
        private boolean pass;
        /**
         * 拒绝原因（通过时为null）
         */
        private String rejectReason;
    }

    /**
     * AI审核消息体。
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AiReviewMessage {
        /**
         * 审核类型：goods_comment / comment_append / review_comment
         */
        private String type;
        /**
         * 对应记录的ID
         */
        private Long id;
    }
}