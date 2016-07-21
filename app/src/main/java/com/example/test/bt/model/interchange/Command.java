package com.example.test.bt.model.interchange;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class Command {

    private int id;
    private boolean err = false;

    public Command() {
        genId();
    }

    private static final AtomicInteger atomicInteger = new AtomicInteger();

    public abstract byte[] getData();

    public abstract void setData(byte[] data);

    public abstract byte[] getAnswer();

    public abstract void setAnswer(byte[] answer);

    public int getId() {
        return id;
    }

    public boolean isErr() {
        return err;
    }

    public void setErr(boolean err) {
        this.err = err;
    }

    private void genId() {
        id = atomicInteger.getAndIncrement();
    }

}
