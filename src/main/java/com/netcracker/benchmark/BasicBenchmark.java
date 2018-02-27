package com.netcracker.benchmark;

import com.netcracker.algorithms.AssignmentProblemSolver;
import com.netcracker.algorithms.auction.SynchronousJacobiAlgorithm;
import com.netcracker.utils.MatrixReader;
import org.openjdk.jmh.Main;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.RunnerException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
public class BasicBenchmark {

    private List<int[][]> inputMatrices = MatrixReader.readMatricesFromFile(getFileNames());

//    AssignmentProblemSolver hungarian = new HungarianAlgorithm();
//    AssignmentProblemSolver initialAuction = new AuctionAlgorithm();
//    AssignmentProblemSolver refactoredAuction = new AuctionAlgorithm();
    AssignmentProblemSolver parallelAuction = new SynchronousJacobiAlgorithm(4);
    AssignmentProblemSolver nonParallelAuction = new SynchronousJacobiAlgorithm(10);

    @Setup
    public void init() {
    }

//    @Benchmark
//    public List<int[]> hungarian() {
//        List<int[]> solutions = new ArrayList<>();
//        for(int[][] matrix : inputMatrices){
//            solutions.add(hungarian.findMaxCostMatching(matrix));
//        }
//        return solutions;
//    }
//
//    @Benchmark
//    public List<int[]> initialAuction() {
//        List<int[]> solutions = new ArrayList<>();
//        for(int[][] matrix : inputMatrices){
//            solutions.add(initialAuction.findMaxCostMatching(matrix));
//        }
//        return solutions;
//    }
//
//    @Benchmark
//    public List<int[]> refactoredAuction() {
//        List<int[]> solutions = new ArrayList<>();
//        for(int[][] matrix : inputMatrices){
//            solutions.add(refactoredAuction.findMaxCostMatching(matrix));
//        }
//        return solutions;
//    }

    @Benchmark
    public List<int[]> nonParallelAuction() {
        List<int[]> solutions = new ArrayList<>();
        for(int[][] matrix : inputMatrices){
            solutions.add(nonParallelAuction.findMaxCostMatching(matrix));
        }
        return solutions;
    }

    @Benchmark
    public List<int[]> parallelAuction() {
        List<int[]> solutions = new ArrayList<>();
        for(int[][] matrix : inputMatrices){
            solutions.add(parallelAuction.findMaxCostMatching(matrix));
        }
        return solutions;
    }

    public static List<String> getFileNames() {
        List<String> fileNames = new ArrayList<>();

//        fileNames.add("/matrices/util10.txt");
//        fileNames.add("/matrices/util20.txt");
//        fileNames.add("/matrices/util40.txt");
        fileNames.add("/matrices/util60.txt");

        return fileNames;
    }

    public static void main(String[] args) {
        try {
            Main.main(args);
        } catch (RunnerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
