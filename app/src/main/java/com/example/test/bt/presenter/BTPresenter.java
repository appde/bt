package com.example.test.bt.presenter;

import com.example.test.bt.BTView;

public interface BTPresenter {

    void attachView(BTView btView);

    void getProperties();

    void writeDBRecord(int tableId, int recordId, String data);

}
