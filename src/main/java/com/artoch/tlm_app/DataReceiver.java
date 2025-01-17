package com.artoch.tlm_app;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class DataReceiver {

    private static final int PORT = 15000;
    private DatagramSocket socket;


    public DatagramPacket receiveData() throws IOException {
        byte[] buffer = new byte[64];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet);
        return packet;
    }


    public void openSocket() throws SocketException {
        socket = new DatagramSocket(PORT);
        System.out.println("The socket has been opened");
    }

    public void closeSocket() {
        if (!socket.isClosed()) {
            socket.close();
            System.out.println("The socket has been closed");
        }
    }

    public boolean getSocketStatus() {
        return socket.isClosed();
    }


}

