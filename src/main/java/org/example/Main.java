package org.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class Main {
    public static final Path INPUT_FILE = Path.of("./src/main/resources/text.txt");
    public static final Path OUTPUT_FILE = Path.of("./src/main/resources/result.txt");
    public static final int KEY = 3;

    public static void main(String[] args) throws IOException {
        List<String> text = Files.readAllLines(INPUT_FILE);
        measureExecution("Multi-threaded execution by chunks", () -> multiThreadCaesarByChunks(text));
        measureExecution("Multi-threaded execution by lines", () -> multiThreadCaesarByLines(text));
        measureExecution("Single-threaded execution", () -> singleThreadCaesar(text));
        measureExecution("Parallel stream execution", () -> parallelStreamCaesar(text));
    }

    public static void measureExecution(String taskName, Runnable runnable) {
        long startTime = System.currentTimeMillis();
        runnable.run();
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        System.out.println(taskName + ": " + duration + " ms");
    }

    public static void singleThreadCaesar(List<String> text) {
        try {
            List<String> result = text.stream()
                    .map(Main::encryptLine)
                    .toList();

            Files.write(OUTPUT_FILE, result);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void multiThreadCaesarByLines(List<String> text) {
        try {
            List<String> result = new ArrayList<>();
            List<Future<String>> futures = new ArrayList<>();

            try (ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())) {
                for (String line : text) {
                    Callable<String> task = () -> encryptLine(line);
                    futures.add(executor.submit(task));
                }

                for (Future<String> future : futures) {
                    result.add(future.get());
                }

                Files.write(OUTPUT_FILE, result);
            }
        } catch (IOException | InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public static void multiThreadCaesarByChunks(List<String> textToJoin) {
        try {
            String text = String.join(System.lineSeparator(), textToJoin);
            int availableProcessors = Runtime.getRuntime().availableProcessors();
            int chunkSize = (text.length() + availableProcessors - 1) / availableProcessors;
            List<Future<String>> futures = new ArrayList<>();
            List<String> result = new ArrayList<>();

            try (ExecutorService executor = Executors.newFixedThreadPool(availableProcessors)) {
                for (int i = 0; i < text.length(); i += chunkSize) {
                    int start = i;
                    int end = Math.min(text.length(), start + chunkSize);
                    Callable<String> task = () -> encryptChunk(text, start, end);
                    futures.add(executor.submit(task));
                }

                for (Future<String> future : futures) {
                    result.add(future.get());
                }

                Files.write(OUTPUT_FILE, result);
            }
        } catch (IOException | InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public static void parallelStreamCaesar(List<String> text) {
        try {
            List<String> result = text.parallelStream()
                    .map(Main::encryptLine)
                    .collect(Collectors.toList());

            Files.write(OUTPUT_FILE, result);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String encryptChunk(String text, int start, int end) {
        StringBuilder chunkResult = new StringBuilder();

        for (int i = start; i < end; i++) {
            chunkResult.append(shiftLetter(text.charAt(i)));
        }

        return chunkResult.toString();
    }

    private static String encryptLine(String line) {
        StringBuilder builder = new StringBuilder();

        for (char letter : line.toCharArray()) {
            builder.append(shiftLetter(letter));
        }

        return builder.toString();
    }

    private static char shiftLetter(char letter) {
        if (!Character.isAlphabetic(letter)) {
            return letter;
        }

        String alphabet = "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ";
        boolean isUpperCase = Character.isUpperCase(letter);
        int alphabetLength = alphabet.length();
        int currentPosition = alphabet.indexOf(Character.toUpperCase(letter));

        if (currentPosition == -1) {
            return letter;
        }

        int shiftedPosition = (currentPosition + KEY + alphabetLength) % alphabetLength;
        char shifterChar = alphabet.charAt(shiftedPosition);

        return isUpperCase
                ? Character.toUpperCase(shifterChar)
                : Character.toLowerCase(shifterChar);
    }
}
