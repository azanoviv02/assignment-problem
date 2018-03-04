package com.netcracker.benchmark;

import com.netcracker.algorithms.AssignmentProblemSolver;
import com.netcracker.algorithms.auction.AuctionAlgorithm;
import com.netcracker.algorithms.auction.implementation.synchronous.SynchronousJacobi;
import com.netcracker.utils.io.MatrixReader;
import org.openjdk.jmh.Main;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.RunnerException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * My initial attempts at using Java Benchmarking Harness.
 * For now I have put it on hold.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
public class BasicBenchmark {

    private List<int[][]> inputMatrices = MatrixReader.readMatricesFromFile(getFileNames());

//    AssignmentProblemSolver hungarian = new HungarianAlgorithm();
    AssignmentProblemSolver parallelAuction = new AuctionAlgorithm(new SynchronousJacobi(4, 4));

    @Setup
    public void init() {
    }

//    @Benchmark
//    public List<int[]> hungarian() {
//        List<int[]> solutions = new ArrayList<>();
//        for(int[][] matrix : inputMatrices){
//            solutions.add(hungarian.findMaxCostAssignment(matrix));
//        }
//        return solutions;
//    }
//
//    @Benchmark
//    public List<int[]> initialAuction() {
//        List<int[]> solutions = new ArrayList<>();
//        for(int[][] matrix : inputMatrices){
//            solutions.add(initialAuction.findMaxCostAssignment(matrix));
//        }
//        return solutions;
//    }
//
//    @Benchmark
//    public List<int[]> refactoredAuction() {
//        List<int[]> solutions = new ArrayList<>();
//        for(int[][] matrix : inputMatrices){
//            solutions.add(refactoredAuction.findMaxCostAssignment(matrix));
//        }
//        return solutions;
//    }

    @Benchmark
    public List<int[]> parallelAuction() {
        List<int[]> solutions = new ArrayList<>();
        for(int[][] matrix : inputMatrices){
            solutions.add(parallelAuction.findMaxCostAssignment(matrix));
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
