package com.harvey.common;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Harvey Suen
 */
public class SequenceIdUtil {
    private static final AtomicInteger id = new AtomicInteger(1);
    
    public static int getSequenceId() {
        return id.incrementAndGet();
    }
}
