package org.example;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        try {
            long time = System.currentTimeMillis();

            double[][] values = Files.lines(Paths.get(args[0]))
                    .map(line -> line.split(";"))
                    .filter(parts -> Arrays.stream(parts).allMatch(Main::isCorrectPart))
                    .map(parts -> Arrays.stream(parts)
                            .mapToDouble(Main::mapPartToDouble)
                            .toArray())
                    .filter(a -> a.length > 0)
                    .sorted((a, b) -> Integer.compare(b.length, a.length))
                    .toArray(double[][]::new);

            DisjointSet disjointSet = new DisjointSet(values.length);

            Map<Double, Integer> column = new HashMap<>();
            for (int j = 0; j < values[0].length; j++) {
                column.clear();
                int i = 0;
                while (i < values.length && j < values[i].length) {
                    if (Double.isNaN(values[i][j])) {
                        i++;
                        continue;
                    }
                    Integer x = column.get(values[i][j]);
                    if (x != null) {
                        disjointSet.union(x, i);
                    } else {
                        column.put(values[i][j], i);
                    }
                    i++;
                }
            }

            Map<Integer, Integer> setSizes = new HashMap<>();
            for (int i = 0; i < values.length; i++) {
                int root = disjointSet.find(i);
                setSizes.put(root, setSizes.getOrDefault(root, 0) + 1);
            }

            Map<Integer, Integer> pointers = new HashMap<>();
            HashMap<Integer, int[]> indexes = new HashMap<>();
            for (Map.Entry<Integer, Integer> entry : setSizes.entrySet()) {
                indexes.put(entry.getKey(), new int[entry.getValue()]);
                pointers.put(entry.getKey(), 0);
            }
            setSizes.clear();

            for (int i = 0; i < values.length; i++) {
                int root = disjointSet.find(i);
                indexes.get(root)[pointers.get(root)] = i;
                pointers.put(root, pointers.get(root) + 1);
            }
            pointers.clear();

            int moreOneCnt = 0;
            int inc = 1;

            ArrayList<int[]> result = new ArrayList<>(indexes.values());
            result.sort((a, b) -> Integer.compare(b.length, a.length));

            for (int[] entry : result) {
                if (entry.length > 1) {
                    moreOneCnt++;
                }
            }

            try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("output.txt"))) {
                writer.write("Групп с более чем одним элементом: " + moreOneCnt + "\n");

                for (int[] entry : result) {

                    writer.write("Группа " + inc + "\n");
                    for (int j : entry) {
                        double[] array = values[j];
                        String formattedLine = Arrays.stream(array)
                                .mapToObj(value -> Double.isNaN(value) ? "\"\"" : "\"" + value + "\"")
                                .collect(Collectors.joining(";"));
                        writer.write(formattedLine + "\n");
                    }
                    inc++;
                }

                System.out.println("Групп с более чем одним элементом: " + moreOneCnt);
                System.out.println("Время на выполнение: " + ((System.currentTimeMillis() - time) / 1000.0) + " секунд\n");
            }

        } catch (IOException e) {
            System.err.println("Ошибка чтения файла: " + e.getMessage());
        }
    }

    private static double mapPartToDouble(String part) {
        if (part.isBlank() || part.equals("\"\"")) {
            return Double.NaN;
        }
        try {
            return Double.parseDouble(part.substring(1, part.length() - 1));
        } catch (NumberFormatException e) {
            return Double.NaN;
        }
    }

    private static boolean isCorrectPart(String part) {
        return part.isBlank() || part.equals("\"\"") || (part.startsWith("\"")
                && part.endsWith("\"") && tryParseDouble(part.substring(1, part.length() - 1)));
    }

    private static boolean tryParseDouble(String part) {
        try {
            Double.parseDouble(part);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }
}