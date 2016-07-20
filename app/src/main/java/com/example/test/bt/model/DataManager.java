package com.example.test.bt.model;

import android.util.Log;

import com.example.test.bt.device.Server;
import com.example.test.bt.model.interchange.Command;
import com.example.test.bt.model.io.Client;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

public class DataManager {

    public static final int SERVER_TIMEOUT_MIN_MS = 100;
    public static final int SEND_TIMEOUT_S = 1;

    private static final String TAG = DataManager.class.getName();
    private static DataManager dataManager;

    private DataManager() {
        Server device = new Server();
        Thread deviceThread = new Thread(device);
        deviceThread.start();
    }

    public static DataManager getInstance() {
        if (dataManager == null) {
            dataManager = new DataManager();
        }
        return dataManager;
    }

    // TODO: 20/07/2016 add consecutive performing, one command at a time
    public Observable<Command> send(final Command command) {
        return Observable.create(new Observable.OnSubscribe<Command>() {
            @Override
            public void call(final Subscriber<? super Command> subscriber) {
                new Client(command.getData()) {
                    @Override
                    public void onDataReceived(byte[] data) {
                        Log.d(TAG, "send: onDataReceived: Thread: " + Thread.currentThread());
                        Log.d(TAG, "send: onDataReceived: " + new String(data));
                        command.setAnswer(data);
                        subscriber.onNext(command);
                        subscriber.onCompleted();
                    }
                };
            }
        }).subscribeOn(Schedulers.newThread())
                .timeout(DataManager.SEND_TIMEOUT_S, java.util.concurrent.TimeUnit.SECONDS)
                .single();
    }
}