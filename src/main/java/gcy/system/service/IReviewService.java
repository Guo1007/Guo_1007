package gcy.system.service;

import gcy.system.entity.dto.Result;
import gcy.system.entity.pojo.Review;

public interface IReviewService {

    Result getReviewsByFurnitureId(Long furnitureId);

    Result getReviewsByOrderId(Long orderId, Long userId);

    Result addReview(Review review, Long userId);

    Result deleteReview(Long reviewId, Long userId);
}
