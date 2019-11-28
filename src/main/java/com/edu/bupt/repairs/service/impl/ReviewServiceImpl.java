package com.edu.bupt.repairs.service.impl;

import com.edu.bupt.repairs.model.Review;
import com.edu.bupt.repairs.service.ReviewService;
import com.edu.bupt.repairs.utils.IDKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;

@Service
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    ReviewMapper reviewMapper;

    private IDKeyUtil keyConstructor = new IDKeyUtil(5L, 5L);

    @Override
    public Review saveReview(Review review) {
        BigInteger reviewId = keyConstructor.nextGlobalId();
        review.setId(reviewId);
        reviewMapper.insert(review);
        return review;
    }
}
