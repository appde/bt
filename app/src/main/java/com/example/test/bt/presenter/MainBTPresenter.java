package com.example.test.bt.presenter;

import com.example.test.bt.BTView;
import com.example.test.bt.model.DataManager;
import com.example.test.bt.model.interchange.Command;
import com.example.test.bt.model.interchange.GetPropertiesCommand;
import com.example.test.bt.model.interchange.WriteDBRecordCommand;

import java.util.concurrent.TimeUnit;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainBTPresenter implements BTPresenter {

    private static final String TAG = MainBTPresenter.class.getName();
    private BTView btView;

    @Override
    public void attachView(BTView btView) {
        this.btView = btView;
    }

    @Override
    public void getProperties() {
        DataManager.getInstance()
                .send(new GetPropertiesCommand())
                .map(Command::getAnswer)
                .timeout(1, TimeUnit.SECONDS)
                .onErrorReturn(throwable -> "N\\A".getBytes())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bytes -> btView.updateProperties(new String(bytes)));
    }

    @Override
    public void writeDBRecord(int tableId, int recordId, String data) {
        DataManager.getInstance()
                .send(new WriteDBRecordCommand(tableId, recordId, data))
                .map(Command::getAnswer)
                .timeout(1, TimeUnit.SECONDS)
                .onErrorReturn(throwable -> "N\\A".getBytes())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bytes -> btView.updateWriteDBAnswer(new String(bytes)));
    }


}
