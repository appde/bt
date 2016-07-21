package com.example.test.bt;

public interface BTView {

    void updateProperties(String properties);

    void indicateProperties(boolean isQueueOk);

    void updateWriteDBAnswer(String properties);

    void indicateWriteDBAnswer(boolean isQueueOk);

}
