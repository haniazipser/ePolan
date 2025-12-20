package com.example.ePolan.Services.matching;

public interface MatchingStrategy {
    int[][] match(double[][] costMatrix);
}