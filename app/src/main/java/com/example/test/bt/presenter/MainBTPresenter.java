package com.example.test.bt.presenter;

import android.util.Log;

import com.example.test.bt.BTView;
import com.example.test.bt.model.DataManager;
import com.example.test.bt.model.interchange.Command;
import com.example.test.bt.model.interchange.GetPropertiesCommand;
import com.example.test.bt.model.interchange.WriteDBRecordCommand;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainBTPresenter implements BTPresenter {

    private static final String TAG = MainBTPresenter.class.getName();
    private BTView btView;
    private static final AtomicInteger maxDBWriteCommandId = new AtomicInteger(0);
    private static final AtomicInteger maxPropCommandId = new AtomicInteger(0);

    private static final AtomicInteger lastDBWriteCommandId = new AtomicInteger(0);
    private static final AtomicInteger lastPropCommandId = new AtomicInteger(0);

    @Override
    public void attachView(BTView btView) {
        this.btView = btView;
    }

    @Override
    public void getProperties() {
        perform(new GetPropertiesCommand());
    }

    @Override
    public void writeDBRecord(int tableId, int recordId, String data) {
        perform(new WriteDBRecordCommand(tableId, recordId, data));
    }

    public void perform(Command command) {
        boolean propType = command instanceof GetPropertiesCommand;
        if (propType) {
            if (command.getId() > maxPropCommandId.get()) {
                maxPropCommandId.set(command.getId());
            }
        } else {
            if (command.getId() > maxDBWriteCommandId.get()) {
                maxDBWriteCommandId.set(command.getId());
            }
        }

        put(command)
                .subscribe(command0 -> {
                            boolean isPropType = command0 instanceof GetPropertiesCommand;
                            if (isPropType) {
                                if (command0.getId() > lastPropCommandId.get()) {
                                    lastPropCommandId.set(command0.getId());
                                }
                            } else {
                                if (command0.getId() > lastDBWriteCommandId.get()) {
                                    lastDBWriteCommandId.set(command0.getId());
                                }
                            }

                            if (command0.isErr()) {
                                Log.d(TAG, "perform: command0.isErr()=true command0.getId()=" + command0.getId());
                                if (isPropType && command0.getId() >= lastPropCommandId.get()) {
                                    btView.indicateProperties(false);
                                } else if (!isPropType && command0.getId() >= lastDBWriteCommandId.get()){
                                    btView.indicateWriteDBAnswer(false);
                                }
                            } else {
                                DataManager.getInstance().busIn()
                                        .filter(command1 -> command1.getId() == command0.getId())
                                        .take(1)
                                        .timeout(DataManager.SEND_TIMEOUT_S, TimeUnit.SECONDS)
                                        .onErrorReturn(throwable -> {
                                            command0.setErr(true);
                                            return command;
                                        })
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(command2 -> {
                                            if (command2.isErr()) {
                                                Log.d(TAG, "perform: command2: Err: " + command2.getId());
                                                if (isPropType && command0.getId() >= lastPropCommandId.get()) {
                                                    btView.updateProperties("N\\A".getBytes());
                                                    btView.indicateProperties(false);
                                                } else if (!isPropType && command0.getId() >= lastDBWriteCommandId.get()){
                                                    btView.updateWriteDBAnswer("N\\A".getBytes());
                                                    btView.indicateWriteDBAnswer(false);
                                                }
                                            } else {
                                                Log.d(TAG, "perform: command2: " + command2.getId() + " " + new String(command2.getAnswer()));
                                                if (isPropType && command0.getId() >= lastPropCommandId.get()) {
                                                    btView.updateProperties(command2.getAnswer());
                                                } else if (!isPropType && command0.getId() >= lastDBWriteCommandId.get()){
                                                    btView.updateWriteDBAnswer(command2.getAnswer());
                                                }
                                                if (isPropType && command0.getId() >= lastPropCommandId.get()) {
                                                    btView.indicateProperties(true);
                                                } else if (!isPropType && command0.getId() >= lastDBWriteCommandId.get()) {
                                                    btView.indicateWriteDBAnswer(true);
                                                }
                                            }
                                        });
                            }


                        }

                );
    }

    private Observable<Command> put(final Command command) {

        return Observable.create(new Observable.OnSubscribe<Command>() {
            @Override
            public void call(Subscriber<? super Command> subscriber) {
                boolean offered = false;
                try {
                    offered = DataManager.getInstance().drop
                            .offer(command, DataManager.SEND_TIMEOUT_S, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Log.d(TAG, "put: offered = " + offered + " id: " + command.getId());

                if (!offered) {
                    command.setErr(true);
                }
                subscriber.onNext(command);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }


}



