package com.handloom.marketplace.service;

import com.handloom.marketplace.model.Review;
import com.handloom.marketplace.repository.ReviewRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;

    public ReviewService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    public Review addReview(Long productId, Long customerId, int rating, String comment) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5 stars.");
        }

        Review review = new Review();
        review.setProductId(productId);
        review.setCustomerId(customerId);
        review.setRating(rating);
        review.setComment(comment);

        Long reviewId = reviewRepository.save(review);
        review.setId(reviewId);
        return review;
    }

    public List<Review> findByProductId(Long productId) {
        return reviewRepository.findByProductId(productId);
    }

    public List<Review> findByCustomerId(Long customerId) {
        return reviewRepository.findByCustomerId(customerId);
    }

    public boolean hasUserReviewed(Long productId, Long customerId) {
        return reviewRepository.existsByProductAndCustomer(productId, customerId);
    }

    public Double getAverageRating(Long productId) {
        return reviewRepository.getAverageRating(productId);
    }
}
