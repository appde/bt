package com.example.test.bt.model;

import android.icu.util.TimeUnit;
import android.os.Bundle;
import android.util.Log;

import com.example.test.bt.device.Server;
import com.example.test.bt.model.interchange.Command;
import com.example.test.bt.model.interchange.GetPropertiesCommand;
import com.example.test.bt.model.io.Client;

import rx.Observable;
import rx.Subscriber;
import rx.observables.BlockingObservable;
import rx.schedulers.Schedulers;

public class DataManager {

    private static final String TAG = DataManager.class.getName();
    private static DataManager dataManager;
    private volatile static boolean lock = false;

    private DataManager() {
        Server device = new Server();
        Thread deviceThread = new Thread(device);
        deviceThread.start();

        initClient();
    }

    private void initClient() {
        String string1 = "Sending a test message";
        String string2 = "Second message";
        /*Client test1 = new Client(string1);
        Thread thread = new Thread(test1);
        thread.start();*/

        //send(new GetPropertiesCommand()).subscribe();
    }

    public static DataManager getInstance() {
        if (dataManager == null) {
            dataManager = new DataManager();
        }
        return dataManager;
    }

    private synchronized static void lock() {
        lock = true;
    }

    private synchronized static void unlock() {
        lock = false;
    }

    public synchronized static boolean readLock() {
        return lock;
    }


    public Observable<Command> send(final Command command) {
        //lock();
        Log.d(TAG, "send: Thread" + Thread.currentThread());
        return Observable.create(new Observable.OnSubscribe<Command>() {
            @Override
            public void call(final Subscriber<? super Command> subscriber) {
                new Client(command.getData()) {
                    @Override
                    public void onDataReceived(byte[] data) {
                        Log.d(TAG, "send: onDataReceived: Thread" + Thread.currentThread());
                        Log.d(TAG, "send: onDataReceived: " + new String(data));
                        command.setAnswer(data);
                        subscriber.onNext(command);
                        subscriber.onCompleted();
                    }
                };
            }
        }).subscribeOn(Schedulers.newThread())
                .timeout(5, java.util.concurrent.TimeUnit.SECONDS)
                //.doOnTerminate(() -> unlock())
                .single();
    }
}