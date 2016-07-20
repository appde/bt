package com.example.test.bt.device;

import android.util.Log;

import com.example.test.bt.model.DataManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

public class Server implements Runnable {

    private static final String TAG = Server.class.getName();

    public final static String ADDRESS = "127.0.0.1";
    public final static int PORT = 8181;
    public final static long TIMEOUT = 10000;


    private ServerSocketChannel serverChannel;
    private Selector selector;

    private Map<SocketChannel, byte[]> dataTracking = new HashMap<>();

    public Server() {
        init();
    }

    private void init() {
        Log.d(TAG, "init: initializing server");
        // We do not want to call init() twice and recreate the selector or the serverChannel.
        if (selector != null) return;
        if (serverChannel != null) return;

        try {

            selector = Selector.open();
            serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false);
            serverChannel.socket().bind(new InetSocketAddress(ADDRESS, PORT));
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        Log.d(TAG, "run: Now accepting connections...");
        try {
            // A run the server as long as the thread is not interrupted.
            while (!Thread.currentThread().isInterrupted()) {
                selector.select(TIMEOUT);

                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    keys.remove();

                    if (!key.isValid()) {
                        continue;
                    }

                    if (key.isAcceptable()) {
                        Log.d(TAG, "run: Accepting connection");
                        accept(key);
                    }

                    if (key.isWritable()) {
                        Log.d(TAG, "run: Writing...");
                        write(key);
                    }

                    if (key.isReadable()) {
                        Log.d(TAG, "run: Reading connection");
                        read(key);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }

    }

    private void write(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();

        byte[] data = dataTracking.get(channel);
        dataTracking.remove(channel);

        channel.write(ByteBuffer.wrap(data));
        key.interestOps(SelectionKey.OP_READ);

    }

    private void closeConnection() {
        Log.d(TAG, "closeConnection: Closing server down");
        if (selector != null) {
            try {
                selector.close();
                serverChannel.socket().close();
                serverChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void accept(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);

        Log.d(TAG, "accept: ");
        socketChannel.register(selector, SelectionKey.OP_READ);
    }

    private void read(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer readBuffer = ByteBuffer.allocate(1024);
        readBuffer.clear();
        int read;
        try {
            read = channel.read(readBuffer);
        } catch (IOException e) {
            Log.d(TAG, "read: Reading problem, closing connection");
            key.cancel();
            channel.close();
            return;
        }
        if (read == -1) {
            Log.d(TAG, "read: Nothing was there to be read, closing connection");
            channel.close();
            key.cancel();
            return;
        }

        readBuffer.flip();
        byte[] data = new byte[1000];
        readBuffer.get(data, 0, read);
        Log.d(TAG, "read: Received: " + new String(trim(data)));

        answer(key, data);
    }

    //Preparing answer of command
    private void answer(SelectionKey key, byte[] data) {

        JSONObject root;

        try {
            //Adding some answering delay: 100-500 ms
            Thread.sleep(DataManager.SERVER_TIMEOUT_MIN_MS + new Random().nextInt(4) * 100);

            root = new JSONObject(new String(data));

            if (root.has("getProperties")) {
                int id = root.getInt("getProperties");
                root.put("answer", "properties description " + id);

                data = root.toString().getBytes();
            }
            if (root.has("writeDBRecord")) {
                int id = root.getInt("writeDBRecord");
                int tableId = root.getInt("tableId");
                int recordId = root.getInt("recordId");
                String d = root.getString("data");
                root.put("answer", "tableId=" + tableId + ", recordId=" + recordId + ", data=" + d + ", record was successfully recorded, commandId: " + id);

                data = root.toString().getBytes();
            }

        } catch (InterruptedException | JSONException e) {
            e.printStackTrace();
        }

        SocketChannel socketChannel = (SocketChannel) key.channel();
        dataTracking.put(socketChannel, data);
        key.interestOps(SelectionKey.OP_WRITE);
    }

    private byte[] trim(byte[] data) {
        for (int i = 0; i < data.length; i++) {
            if (data[i] == 0) {
                return Arrays.copyOfRange(data, 0, i);
            }
        }
        return data;
    }

}