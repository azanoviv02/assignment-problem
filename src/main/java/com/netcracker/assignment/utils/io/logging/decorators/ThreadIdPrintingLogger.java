package com.netcracker.assignment.utils.io.logging.decorators;

import com.netcracker.assignment.utils.io.logging.Logger;

public class ThreadIdPrintingLogger implements Logger {

    private final Logger nestedLogger;
//    private final ConcurrentMap<Long, String> threadNickNameMap;
//    private final Queue<String> nicknameQueue;

    public ThreadIdPrintingLogger(Logger nestedLogger) {
        this.nestedLogger = nestedLogger;
//        this.threadNickNameMap = new ConcurrentHashMap<>();
//        this.nicknameQueue = initNicknameQueue();
    }

    @Override
    public void info(String format, Object... args) {
        final long threadId = Thread.currentThread().getId();
//        final String threadNickname = getThreadNickname(threadId);

        final String formatWithThreadId = String.format("[%d] %s", threadId, format);
        nestedLogger.info(formatWithThreadId, args);

//        final String formatWithThreadNickname = String.format("[%d] %s", threadNickname, format);
//        nestedLogger.info(formatWithThreadNickname, args);
    }

//    private String getThreadNickname(long threadId) {
//        return threadNickNameMap.computeIfAbsent(threadId, id -> {
//            String newNickname = nicknameQueue.poll();
//            if (newNickname == null) {
//                newNickname = Long.toString(id);
//            }
//            return newNickname;
//        });
//    }
//
//    private static Queue<String> initNicknameQueue() {
//        List<String> nicknameList = asList(
//                "Alpha",
//                "Bravo",
//                "Charlie",
//                "Delta",
//                "Echo",
//                "Hotel",
//                "Julia",
//                "Kilo",
//                "Lima"
//        );
//        return new LinkedBlockingDeque<>(nicknameList);
//    }
}
