package org.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class Main {
    public static final Path inputFile = Path.of("./src/main/resources/text.txt");
    public static final Path outputFile = Path.of("./src/main/resources/result.txt");
    public static final int key = 3;

    public static void main(String[] args) {
        measureExecution("Single-threaded execution", Main::singleThreadCaesar);
        measureExecution("Multi-threaded execution by lines",Main::multiThreadCaesarByLines);
        measureExecution("Multi-threaded execution by chunks",Main::multiThreadCaesarByChunks);
    }

    public static void measureExecution(String taskName, Runnable runnable) {
        long startTime = System.currentTimeMillis();
        runnable.run();
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        System.out.println(taskName + ": " + duration + " ms");
    }

    public static void singleThreadCaesar() {
        try {
            List<String> text = Files.readAllLines(inputFile);
            List<String> result = text.stream()
                    .map(Main::encryptLine)
                    .collect(Collectors.toList());

            Files.write(outputFile, result);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void multiThreadCaesarByLines() {
        try {
            List<String> text = Files.readAllLines(inputFile);
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

                Files.write(outputFile, result);
            }
        } catch (IOException | InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public static void multiThreadCaesarByChunks() {
        try {
            String text = String.join(System.lineSeparator(), Files.readAllLines(inputFile));
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

                Files.write(outputFile, result);
            }
        } catch (IOException | InterruptedException | ExecutionException e) {
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

        int shiftedPosition = (currentPosition + key + alphabetLength) % alphabetLength;
        char shifterChar = alphabet.charAt(shiftedPosition);

        return isUpperCase
                ? Character.toUpperCase(shifterChar)
                : Character.toLowerCase(shifterChar);
    }
}