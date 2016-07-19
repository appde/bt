package com.example.test.bt.model.interchange;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class Command {

    private static final AtomicInteger atomicInteger = new AtomicInteger();

    public abstract byte[] getData();

    public abstract void setData(byte[] data);

    public abstract byte[] getAnswer();

    public abstract void setAnswer(byte[] answer);

    public int getId() {
        return atomicInteger.getAndIncrement();
    }

}
