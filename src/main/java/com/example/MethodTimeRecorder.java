package com.example;

public class MethodTimeRecorder {
    private static long startTime;
    private static int callCount;

    public static void start() {
        startTime = System.currentTimeMillis();
    }

    public static void end() {
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        callCount++;
        System.out.println("Method call #" + callCount + ": " + duration + " ms");
    }

    public static void main(String[] args) {
        System.out.println("Hello world!");
    }
}