package gcy.system.controller;

import gcy.system.entity.dto.Result;
import gcy.system.entity.pojo.Review;
import gcy.system.service.IReviewService;
import gcy.system.utils.UserHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/review")
@RequiredArgsConstructor
public class ReviewController {

    private final IReviewService reviewService;

    @GetMapping("/list/{furnitureId}")
    public Result list(@PathVariable Long furnitureId) {
        return reviewService.getReviewsByFurnitureId(furnitureId);
    }

    @GetMapping("/order/{orderId}")
    public Result listByOrder(@PathVariable Long orderId) {
        Long userId = UserHolder.getUser().getId();
        return reviewService.getReviewsByOrderId(orderId, userId);
    }

    @PostMapping("/add")
    public Result add(@RequestBody Review review) {
        Long userId = UserHolder.getUser().getId();
        return reviewService.addReview(review, userId);
    }

    @DeleteMapping("/{reviewId}")
    public Result delete(@PathVariable Long reviewId) {
        Long userId = UserHolder.getUser().getId();
        return reviewService.deleteReview(reviewId, userId);
    }
}
