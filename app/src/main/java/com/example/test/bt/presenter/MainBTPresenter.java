package com.example.test.bt.presenter;

import android.util.Log;

import com.example.test.bt.BTView;
import com.example.test.bt.model.DataManager;
import com.example.test.bt.model.interchange.Command;
import com.example.test.bt.model.interchange.GetPropertiesCommand;
import com.example.test.bt.model.interchange.WriteDBRecordCommand;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
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
                .timeout(DataManager.SEND_TIMEOUT_S, TimeUnit.SECONDS)
                .onErrorReturn(throwable -> "N\\A".getBytes())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bytes -> btView.updateProperties(new String(bytes)));
    }


    /*@Override
    public void writeDBRecord(int tableId, int recordId, String data) {
        DataManager.getInstance()
                .send(new WriteDBRecordCommand(tableId, recordId, data))
                .map(Command::getAnswer)
                .timeout(DataManager.SEND_TIMEOUT_S, TimeUnit.SECONDS)
                .onErrorReturn(throwable -> "N\\A".getBytes())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bytes -> btView.updateWriteDBAnswer(new String(bytes)));
    }*/

    @Override
    public void writeDBRecord(int tableId, int recordId, String data) {
        Command command = new WriteDBRecordCommand(tableId, recordId, data);
        int commandId = command.getId();

        put(command)
                .zipWith(DataManager.getInstance().busIn(), (command3, command4) -> {
                    if (command3.isErr()) {
                        return command3;
                    } else {
                        return command4;
                    }
                })
                .filter(command1 -> {
                    Log.d(TAG, "writeDBRecord: filter: command1.getId() == commandId: " + command1.getId() + "=" + commandId);
                    return command1.getId() == commandId || command1.isErr();
                })
                .timeout(10, TimeUnit.SECONDS)
                .onErrorReturn(throwable -> null)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(command2 -> {
                    if (command2 == null) {
                        Log.d(TAG, "writeDBRecord: command2: Err");
                        btView.updateWriteDBAnswer("N\\A");
                    } else if (command2.isErr()) {
                        Log.d(TAG, "writeDBRecord: command2.isErr()=true");
                        btView.indicateWriteDBAnswer(false);
                    } else {
                        Log.d(TAG, "writeDBRecord: command2: " + command2.getId());
                        btView.updateWriteDBAnswer(new String(command2.getData()));
                        btView.indicateWriteDBAnswer(true);
                    }
                });
    }

    /*private void put(Command command) {
        Observable.just(command)
                .subscribeOn(Schedulers.newThread())
                .subscribe(command1 -> {
                    try {
                        boolean offered = DataManager.getInstance().drop
                                .offer(command1, 1, TimeUnit.SECONDS);
                        Log.d(TAG, "put: offered = " + offered);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
    }*/

    private Observable<Command> put(final Command command) {
        return Observable.create(new Observable.OnSubscribe<Command>() {
            @Override
            public void call(Subscriber<? super Command> subscriber) {
                boolean offered = false;
                try {
                    offered = DataManager.getInstance().drop
                            .offer(command, 1, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Log.d(TAG, "put: offered = " + offered);

                if (!offered) {
                    command.setErr(true);
                }
                subscriber.onNext(command);
                subscriber.onCompleted();
            }
        });
    }
}



