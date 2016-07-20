package com.example.test.bt.model;

import android.util.Log;

import com.example.test.bt.device.Server;
import com.example.test.bt.model.interchange.Command;
import com.example.test.bt.model.interchange.GetPropertiesCommand;
import com.example.test.bt.model.interchange.WriteDBRecordCommand;
import com.example.test.bt.model.io.Client;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.observables.BlockingObservable;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public class DataManager {

    public static final int SERVER_TIMEOUT_MIN_MS = 5000;
    public static final int SEND_TIMEOUT_S = 10;

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
        //testCommandGen();
        //testCommandGen2();
        //testCommandSend();

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


    public BlockingObservable<Command> send2(final Command command) {
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
                .single()
                .toBlocking();
    }

    private void testCommandGen() {
        Observable.interval(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.newThread())
                .subscribe(aLong -> {
                    try {
                        Log.d(TAG, "testCommandGen: " + aLong);
                        drop.put(new GetPropertiesCommand());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
    }

    private void testCommandGen2() {
        Observable.interval(2, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.newThread())
                .subscribe(aLong -> {
                    try {
                        Log.d(TAG, "testCommandGen2: " + aLong);
                        drop.put(new WriteDBRecordCommand(1, 3, "Comm"));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
    }

    private Subscription testCommandSend() {
        return Observable.interval(100, TimeUnit.MILLISECONDS)
                .subscribe(aLong -> {
                    try {
                        Log.d(TAG, "testCommandSend: " + aLong);
                        send2(drop.take()).subscribe(command -> {
                            Log.d(TAG, "testCommandSend: subscribe: " + new String(command.getAnswer()));
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
    }
}