package com.example.test.bt.model.io;

import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Iterator;

public abstract class Client{

    private static final String TAG = Client.class.getName();

    private byte[] message;
    private Selector selector;

    public Client(byte[] message) {
        this.message = message;
        init();
    }

    public abstract void onDataReceived(byte[] data);

    public void init() {
        SocketChannel channel;
        try {
            selector = Selector.open();
            channel = SocketChannel.open();
            channel.configureBlocking(false);

            channel.register(selector, SelectionKey.OP_CONNECT);
            channel.connect(new InetSocketAddress("127.0.0.1", 8181));

            while (!Thread.interrupted()) {

                selector.select(1000);

                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    keys.remove();

                    if (!key.isValid()) continue;

                    if (key.isConnectable()) {
                        Log.d(TAG, "run: I am connected to the server");
                        connect(key);
                    }
                    if (key.isWritable()) {
                        write(key);
                    }
                    if (key.isReadable()) {
                        read(key);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close();
        }
    }


    private void close() {
        try {
            selector.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void read(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer readBuffer = ByteBuffer.allocate(1000);
        readBuffer.clear();
        int length;
        try {
            length = channel.read(readBuffer);
        } catch (IOException e) {
            Log.d(TAG, "read: Reading problem, closing connection");
            key.cancel();
            channel.close();
            return;
        }
        if (length == -1) {
            Log.d(TAG, "read: Nothing was read from server");
            channel.close();
            key.cancel();
            return;
        }
        readBuffer.flip();
        byte[] buff = new byte[1024];
        readBuffer.get(buff, 0, length);
        System.out.println("Server said: " + new String(trim(buff)));

        onDataReceived(trim(buff));
    }

    private void write(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        channel.write(ByteBuffer.wrap(message));

        // lets get ready to read.
        key.interestOps(SelectionKey.OP_READ);
    }

    private void connect(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        if (channel.isConnectionPending()) {
            channel.finishConnect();
        }
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_WRITE);
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
