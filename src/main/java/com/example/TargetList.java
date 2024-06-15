package com.example;

import java.util.List;

public class TargetList {
    // TODO change class to config file
    public static List<Target> targetList = List.of(
            new Target("com/example/YourTargetClass", "targetMethod"),
            new Target("com/example/YourTargetClass2", "targetMethod2")
    );

    static class Target {
        String className;
        String methodName;

        public Target(String className, String methodName) {
            this.className = className;
            this.methodName = methodName;
        }

        public String getClassName() {
            return className;
        }

        public String getMethodName() {
            return methodName;
        }
    }
}
