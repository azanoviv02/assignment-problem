package com.netcracker.assignment.utils;

public class AssertionMaker {

    public static void makeAssertion(boolean result) {
        if (!result) {
            throw new IllegalStateException("Custom assert failed");
        }
    }

    public static void makeAssertion(boolean result, String message) {
        if (!result) {
            throw new IllegalStateException("Custom assert failed, message: "+message);
        }
    }
}
