package com.example.test.bt.model;

import com.example.test.bt.device.Server;
import com.example.test.bt.model.io.Client;

public class DataManager {

    private static DataManager dataManager;

    private DataManager() {
        Server device = new Server();
        Thread deviceThread = new Thread(device);
        deviceThread.start();

        initClient();
    }

    private void initClient() {
        String string1 = "Sending a test message";
        String string2 = "Second message";
        Client test1 = new Client(string1);
        Thread thread = new Thread(test1);
        thread.start();
    }

    public static DataManager getInstance() {
        if (dataManager == null) {
            dataManager = new DataManager();
        }
        return dataManager;
    }

}
