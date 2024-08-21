package org.example;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        try {
            long time = System.currentTimeMillis();

            long[][] values = Files.lines(Paths.get(args[0]))
                    .map(line -> line.split(";"))
                    .filter(parts -> Arrays.stream(parts)
                            .allMatch(part -> part.matches("\"\\d{0,11}\""))) // Проверка корректности строки
                    .map(parts -> Arrays.stream(parts)
                            .map(part -> part.replace("\"", ""))
                            .mapToLong(part -> part.isEmpty() ? 0L : Long.parseLong(part))
                            .toArray())
                    .toArray(long[][]::new);

            Map<Long, ArrayList<Pair>> map = new HashMap<>();

            // Заполняем карту значений
            for (int rowIndex = 0; rowIndex < values.length; rowIndex++) {
                for (int colIndex = 0; colIndex < values[rowIndex].length; colIndex++) {
                    long value = values[rowIndex][colIndex];
                    if (value != 0) {
                        map.computeIfAbsent(value, _ -> new ArrayList<>()).add(new Pair(colIndex, rowIndex));
                    }
                }
            }

            DisjointSet disjointSet = new DisjointSet(values.length);

            // Обрабатываем значения и объединяем группы
            for (ArrayList<Pair> list : map.values()) {
                Collections.sort(list);

                Pair previous = null;
                for (Pair pair : list) {
                    if (previous == null || previous.x != pair.x) {
                        previous = pair;
                    } else {
                        disjointSet.union(previous.y, pair.y);
                    }
                }
            }

            ArrayList<Integer>[] groups = new ArrayList[values.length];
            for (int i = 0; i < values.length; i++) {
                int root = disjointSet.find(i);
                if (groups[root] == null) {
                    groups[root] = new ArrayList<>();
                }
                groups[root].add(i);
            }

            List<ArrayList<Integer>> result = Arrays.stream(groups)
                    .filter(Objects::nonNull)
                    .sorted((a, b) -> Integer.compare(b.size(), a.size()))
                    .toList();

            int moreOneCnt = 0;
            int num = 1;

            // Выводим результат в BufferedWriter
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out))) {
                for (ArrayList<Integer> list : result) {
                    if (list.size() > 1) {
                        moreOneCnt++;
                    }
                    writer.write("Группа " + num + "\n");
                    for (Integer i : list) {
                        String formattedLine = Arrays.stream(values[i])
                                .mapToObj(value -> "\"" + (value == 0 ? "" : value) + "\"")
                                .collect(Collectors.joining(";"));
                        writer.write(formattedLine + "\n");
                    }
                    num++;
                }

                writer.write("\nГрупп с более чем одним элементом: " + moreOneCnt + "\n");
                writer.write("Время на выполнение: " + ((System.currentTimeMillis() - time) / 1000.0) + " секунд\n");
            }

        } catch (IOException e) {
            System.err.println("Ошибка чтения файла: " + e.getMessage());
        }
    }
}