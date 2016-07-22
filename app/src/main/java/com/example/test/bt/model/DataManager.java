package com.example.test.bt.model;

import android.util.Log;

import com.example.test.bt.device.Server;
import com.example.test.bt.model.interchange.Command;
import com.example.test.bt.model.io.Client;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.observables.BlockingObservable;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public class DataManager {

    public static final int SERVER_TIMEOUT_MIN_MS = 10000;
    public static final int SEND_TIMEOUT_S = 1;

    public BlockingQueue<Command> drop;
    private PublishSubject<Command> busIn;

    private static final String TAG = DataManager.class.getName();
    private static DataManager dataManager;

    private DataManager() {
        Server device = new Server();
        Thread deviceThread = new Thread(device);
        deviceThread.start();

        drop = new SynchronousQueue<>();
        busIn = PublishSubject.create();

        sender();
    }

    public static DataManager getInstance() {
        if (dataManager == null) {
            dataManager = new DataManager();
        }
        return dataManager;
    }

    public Observable<Command> busIn() {
        return busIn;
    }


    public BlockingObservable<Command> send(final Command command) {
        return Observable.create(new Observable.OnSubscribe<Command>() {
            @Override
            public void call(final Subscriber<? super Command> subscriber) {
                new Client(command.getData()) {
                    @Override
                    public void onDataReceived(byte[] data) {
                        Log.d(TAG, "send: onDataReceived: Thread: " + Thread.currentThread());
                        Log.d(TAG, "send: onDataReceived: " + new String(data));
                        busIn.onNext(command);
                        command.setAnswer(data);
                        subscriber.onNext(command);
                        subscriber.onCompleted();
                    }
                };
            }


        }).subscribeOn(Schedulers.newThread())
                .timeout(DataManager.SEND_TIMEOUT_S, java.util.concurrent.TimeUnit.SECONDS)
                .onErrorReturn(throwable -> {
                    command.setErr(true);
                    busIn.onNext(command);
                    return command;
                })
                .single()
                .toBlocking();
    }


    private Subscription sender() {
        return Observable.interval(10, TimeUnit.MILLISECONDS)
                .subscribe(aLong -> {
                    try {
                        send(drop.take()).subscribe(command -> {
                            Log.d(TAG, "sender: subscribe: " + new String(command.getAnswer()));
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
    }
}