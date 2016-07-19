package com.example.test.bt.presenter;

import android.util.Log;

import com.example.test.bt.BTView;
import com.example.test.bt.model.DataManager;
import com.example.test.bt.model.interchange.Command;
import com.example.test.bt.model.interchange.GetPropertiesCommand;

import java.util.concurrent.TimeUnit;

import rx.Observable;
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
        /*if (DataManager.getInstance().readLock()){
            Log.d(TAG, "getProperties: readLock() == true");
            return;
        }*/

        DataManager.getInstance()
                .send(new GetPropertiesCommand())
                .map(Command::getAnswer)
                .timeout(1, TimeUnit.SECONDS)
                .onErrorReturn(throwable -> "N\\A".getBytes())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bytes -> btView.setProperties(new String(bytes)));


    }


}
