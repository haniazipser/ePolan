package com.example.ePolan.Services.matching.hungarianalgorithm;


/**
 * An extension of the Kuhn–Munkres assignment algorithm of the year 1957.
 * this implementation allows for non-square matrices by converting them to square matrices.
 * It also adds a random value to the costs in the matrix to make the algorithm more fair
 * The algorithm is run in a loop until no new assignments are found - allowing for multiple assignments for one student
 *
 * @author https://github.com/jnowak123 | May 2025
 * @version 1.0
 */

public class AssignmentAlgorithm {

    public static int[][] assign(double[][] matrix) {
        return assign(matrix, 0);
    }

    public static int[][] assign(double[][] matrix, int randomizeValue) {

        int rows = matrix.length;
        int cols = matrix[0].length;
        int maxSize = Math.max(rows, cols);

        // Increase the costs in the atrix by a random value - this makes the algorithm more fair
        if (randomizeValue > 0) {
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    if (matrix[i][j] != Double.MAX_VALUE) {
                        matrix[i][j] += Math.random() * randomizeValue;
                    }
                }
            }
        }

        // Convert the matrix to a square matrix, as the Hungarian algorithm requires a square matrix
        double[][] squareMatrix = new double[maxSize][maxSize];
        for (int i = 0; i < maxSize; i++) {
            for (int j = 0; j < maxSize; j++) {
                if (i < rows && j < cols) {
                    squareMatrix[i][j] = matrix[i][j];
                } else {
                    squareMatrix[i][j] = Double.MAX_VALUE; // Use a large value to indicate no assignment
                }
            }
        }

        boolean newAssignments = true;
        int[][] result = new int[2 * maxSize][2];
        int resultIndex = 0;

        while (newAssignments) {
            newAssignments = false;

            // Run the Hungarian algorithm on the square matrix
            double[][] copyMatrix = new double[maxSize][maxSize];
            for (int i = 0; i < maxSize; i++) {
                for (int j = 0; j < maxSize; j++) {
                    copyMatrix[i][j] = squareMatrix[i][j];
                }
            }
            HungarianAlgorithm hungarianAlgorithm = new HungarianAlgorithm(copyMatrix);
            int[][] assignment = hungarianAlgorithm.findOptimalAssignment();

            // Adds new assignments to the result and marks them as assigned in the square matrix
            for (int i = 0; i < assignment.length; i++) {
                if (squareMatrix[assignment[i][1]][assignment[i][0]] != Double.MAX_VALUE) {
                    newAssignments = true;
                    result[resultIndex][0] = assignment[i][0];
                    result[resultIndex][1] = assignment[i][1];
                    resultIndex++;

                    for (int j = 0; j < maxSize; j++) {
                        squareMatrix[assignment[i][1]][j] = Double.MAX_VALUE; // Mark the row as assigned
                    }
                }
            }
        }

        // Remove unused rows from the result
        int[][] trimmedResult = new int[resultIndex][2];
        for (int i = 0; i < resultIndex; i++) {
            trimmedResult[i][0] = result[i][0];
            trimmedResult[i][1] = result[i][1];
        }

        return trimmedResult;
    }
}
