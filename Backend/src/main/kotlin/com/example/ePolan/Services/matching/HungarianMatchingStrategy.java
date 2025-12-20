package com.example.ePolan.Services.matching;

import com.example.ePolan.Services.matching.hungarianalgorithm.AssignmentAlgorithm;

public class HungarianMatchingStrategy implements MatchingStrategy {
    @Override
    public int[][] match(double[][] costMatrix) {
        return AssignmentAlgorithm.assign(costMatrix);
    }
}
