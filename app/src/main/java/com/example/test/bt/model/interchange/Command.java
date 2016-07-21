package com.example.test.bt.model.interchange;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class Command {

    private int id;
    private boolean err = false;
    private byte[] data = new byte[0];
    private byte[] answer = new byte[0];
    private static final AtomicInteger atomicInteger = new AtomicInteger();

    public Command() {
        genId();
    }

    public static int getCurId() {
        return atomicInteger.get();
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public byte[] getAnswer() {
        return answer;
    }

    public void setAnswer(byte[] answer) {
        this.answer = answer;
    }

    private void genId() {
        id = atomicInteger.getAndIncrement();
    }

    public int getId() {
        return id;
    }

    public boolean isErr() {
        return err;
    }

    public void setErr(boolean err) {
        this.err = err;
    }

}
