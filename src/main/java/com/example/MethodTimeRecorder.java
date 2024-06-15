package com.example;

public class MethodTimeRecorder {
    private static long startTime;
    private static int callCount;

    public static void start() {
        startTime = System.nanoTime();
    }

    public static void end() {
        long endTime = System.nanoTime();
        long duration = endTime - startTime;
        callCount++;
        System.out.println("Method call #" + callCount + ": " + duration + " nanoseconds");
    }

    public static void main(String[] args) {
        System.out.println("Hello world!");
    }
}