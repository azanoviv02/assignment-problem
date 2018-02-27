package com.netcracker.algorithms.hungarian;

import com.netcracker.algorithms.AssignmentProblemSolver;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import static java.lang.Integer.max;

public class HungarianAlgorithm implements AssignmentProblemSolver {

    private int[][] costMatrix;
    private int n;

    private int maxMatch;

    private int[] xy;
    private int[] yx;
    private int[] labelX;
    private int[] labelY;

    private boolean[] S;
    private boolean[] T;

    private int[] slack;
    private int[] slackX;

    private int[] prev;

    @Override
    public int[] findMaxCostMatching(int[][] costMatrix) {
        initFields(costMatrix);
        augment();
        return xy;
    }

    private void initFields(int[][] costMatrix) {
        this.costMatrix = costMatrix;
        this.n = costMatrix[0].length;

        this.maxMatch = 0;

        this.labelX = new int[n];
        this.labelY = new int[n];
        Arrays.fill(labelX, 0);
        Arrays.fill(labelY, 0);
        for (int x = 0; x < n; x++) {
            for (int y = 0; y < n; y++) {
                labelX[x] = max(labelX[x], costMatrix[x][y]);
            }
        }

        this.xy = new int[n];
        this.yx = new int[n];
        Arrays.fill(xy, -1);
        Arrays.fill(yx, -1);

        this.S = new boolean[n];
        this.T = new boolean[n];
        this.slack = new int[n];
        this.slackX = new int[n];
        this.prev = new int[n];
    }

    private void augment() {
        if (maxMatch == n) {
            return;
        }

        int root = 0;
        Queue<Integer> bfsQueue = new LinkedList<>();

        Arrays.fill(S, false);
        Arrays.fill(T, false);
        Arrays.fill(prev, -1);
        for (int x = 0; x < n; x++) {
            if (xy[x] == -1) {
                root = x;
                bfsQueue.add(x);
                prev[x] = -2;
                S[x] = true;
                break;
            }
        }

        for (int y = 0; y < n; y++) {
            slack[y] = labelX[root] + labelY[y] - costMatrix[root][y];
            slackX[y] = root;
        }

        int x = 0;
        int y = 0;

        outer:
        while (true) {

            while (!bfsQueue.isEmpty()) {
                x = bfsQueue.remove();
                for (y = 0; y < n; y++) {
                    if (costMatrix[x][y] == labelX[x] + labelY[y] && !T[y]) {

                        if (yx[y] != -1) {
                            T[y] = true;
                            bfsQueue.add(yx[y]);
                            addToTree(yx[y], x);
                        } else {
                            break outer;
                        }
                    }
                }
            }

            updateLabels();
            bfsQueue.clear();

            for (y = 0; y < n; y++) {
                if (!T[y] && slack[y] == 0) {

                    if (yx[y] != -1) {
                        T[y] = true;
                        if (!S[yx[y]]) {
                            bfsQueue.add(yx[y]);
                            addToTree(yx[y], slackX[y]);
                        }
                    } else {
                        x = slackX[y];
                        break outer;
                    }
                }
            }
        }

        if (y < n) {
            maxMatch++;
            for (int cx = x, cy = y, ty; cx != -2; cx = prev[cx], cy = ty) {
                ty = xy[cx];
                yx[cy] = cx;
                xy[cx] = cy;
            }
            augment();
        }
    }

    private void updateLabels() {
        int delta = Integer.MAX_VALUE;

        for (int y = 0; y < n; y++) {
            if (!T[y]) {
                delta = Integer.min(delta, slack[y]);
            }
        }

        for (int x = 0; x < n; x++) {
            if (S[x]) {
                labelX[x] -= delta;
            }
        }

        for (int y = 0; y < n; y++) {
            if (T[y]) {
                labelY[y] += delta;
            }
        }

        for (int y = 0; y < n; y++) {
            if (!T[y]) {
                slack[y] -= delta;
            }
        }
    }

    private void addToTree(int x, int prevx) {
        S[x] = true;
        prev[x] = prevx;
        for (int y = 0; y < n; y++) {
            if (labelX[x] + labelY[y] - costMatrix[x][y] < slack[y]) {
                slack[y] = labelX[x] + labelY[y] - costMatrix[x][y];
                slackX[y] = x;
            }
        }
    }

}

