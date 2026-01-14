package com.example.ePolan;

import com.example.ePolan.Services.matching.hungarianalgorithm.AssignmentAlgorithm;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AssignmentAlgorithmTest {

    @Test
    void testSingleElementMatrix() {
        double[][] matrix = {{5.0}};
        int[][] assignment = AssignmentAlgorithm.assign(matrix);

        assertEquals(1, assignment.length);
        assertArrayEquals(new int[]{0, 0}, assignment[0]);
    }

    @Test
    void testSquareMatrix() {
        double[][] matrix = {
                {70.0, 40.0, 20.0, 55.0},
                {65.0, 60.0, 45.0, 90.0},
                {30.0, 45.0, 50.0, 75.0},
                {25.0, 30.0, 55.0, 40.0}
        };
        int[][] expected = {{0, 2}, {1, 1}, {2, 0}, {3, 3}};
        int[][] assignment = AssignmentAlgorithm.assign(matrix);

        assertTrue(matches(assignment, expected),
                () -> "Expected " + format(expected) + " but got " + format(assignment));
    }

    @Test
    void testMoreRowsThanCols() {
        double[][] matrix = {
                {1.0, 2.0},
                {2.0, 1.0},
                {3.0, 2.0}
        };
        int[][] expected = {{0,0}, {1,1}, {1,2}};
        int[][] assignment = AssignmentAlgorithm.assign(matrix);

        for (int i = 0; i < expected.length; i++) {
            assertArrayEquals(expected[i], assignment[i]);
        }

    }

    @Test
    void testEqualSquareMatrix() {
        int size = 3;
        double[][] matrix = new double[size][size];
        for (int i = 0; i < size; i++)
            for (int j = 0; j < size; j++)
                matrix[i][j] = 5.0;

        int[][] assignment = AssignmentAlgorithm.assign(matrix);

        assertEquals(size, assignment.length);
        for (int[] pair : assignment) {
            assertTrue(pair[0] >= 0 && pair[0] < size);
            assertTrue(pair[1] >= 0 && pair[1] < size);
            assertEquals(5.0, matrix[pair[0]][pair[1]]);
        }
    }

    @Test
    void testMoreColumnsThanRows() {
        double[][] matrix = {
                {1.0, 2.0, 3.0},
                {4.0, 5.0, 6.0}
        };
        int[][] assignment = AssignmentAlgorithm.assign(matrix);

        // akceptujemy kilka poprawnych rozwiązań
        int[][] expected1 = {{0, 0}, {1, 1}};
        int[][] expected2 = {{0, 1}, {1, 0}};
        assertTrue(matches(assignment, expected1) || matches(assignment, expected2),
                () -> "Unexpected assignment: " + format(assignment));
    }

    @Test
    void testMatrixWithInfs() {
        double INF = Double.MAX_VALUE;
        double[][] matrix = {
                {1.0, INF, 3.0},
                {4.0, 2.0, INF},
                {INF, 0.0, 1.0}
        };
        int[][] assignment = AssignmentAlgorithm.assign(matrix);

        assertEquals(3, assignment.length);
        for (int[] pair : assignment) {
            assertTrue(pair[0] >= 0 && pair[0] < 3);
            assertTrue(pair[1] >= 0 && pair[1] < 3);
        }
    }

    @Test
    void testAllInfsMatrix() {
        double INF = Double.MAX_VALUE;
        double[][] matrix = {
                {INF, INF},
                {INF, INF}
        };
        int[][] assignment = AssignmentAlgorithm.assign(matrix);

        assertEquals(0, assignment.length);
    }

    @Test
    void testDecimalValuesMatrix() {
        double[][] matrix = {
                {1.1, 0.9},
                {2.5, 3.5}
        };
        int[][] expected = {{0, 1}, {1, 0}};
        int[][] assignment = AssignmentAlgorithm.assign(matrix);

        assertTrue(matches(assignment, expected),
                () -> "Expected " + format(expected) + " but got " + format(assignment));
    }

    // ---- pomocnicze metody ----
    private boolean matches(int[][] a, int[][] b) {
        if (a.length != b.length) return false;
        for (int i = 0; i < a.length; i++)
            if (a[i][0] != b[i][0] || a[i][1] != b[i][1])
                return false;
        return true;
    }

    private String format(int[][] arr) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < arr.length; i++) {
            sb.append("{" + arr[i][0] + "," + arr[i][1] + "}");
            if (i < arr.length - 1) sb.append(", ");
        }
        sb.append("]");
        return sb.toString();
    }
}
