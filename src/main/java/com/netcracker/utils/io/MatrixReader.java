package com.netcracker.utils.io;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.List;
import java.util.stream.Collectors;

public class MatrixReader {

    private static final String SPLIT_REGEX = "[\\;\\,]{1}";

    public static List<int[][]> readMatricesFromFile(List<String> fileNameList) {
        return fileNameList
                .stream()
                .map(MatrixReader::readMatrixFromFile)
                .collect(Collectors.toList());
    }

    public static int[][] readMatrixFromFile(String fileName) {
        try (LineNumberReader reader =
                     new LineNumberReader(
                             new InputStreamReader(MatrixReader.class.getResourceAsStream(fileName)))){

            String firstLine = reader.readLine();
            String[] firstLineNumbers = firstLine.split(SPLIT_REGEX);
            final int n = firstLineNumbers.length;
            final int[][] matrix = new int[n][n];

            int currentLineNo = 0;
            for (String currentLine = firstLine; currentLine != null; currentLine = reader.readLine()) {
                String[] currentLineNumbers = currentLine.split(SPLIT_REGEX);
                for (int i = 0; i < n; i++) {
                    matrix[currentLineNo][i] = Integer.parseInt(currentLineNumbers[i]);
                }
                currentLineNo++;
            }

            return matrix;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
