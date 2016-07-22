package com.example.test.bt;

public interface BTView {

    void updateProperties(byte[] properties);

    void indicateProperties(boolean isQueueOk);

    void updateWriteDBAnswer(byte[] answer);

    void indicateWriteDBAnswer(boolean isQueueOk);

}
