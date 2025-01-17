package com.artoch.tlm_app;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.util.Arrays;

public class TelemetryService {
    private final DataValidator validator;
    private final DataReceiver receiver;
    private boolean isRunning = false;

    public TelemetryService(TLMController controller) {
        this.validator = new DataValidator();
        this.receiver = new DataReceiver();
        validator.addObserver(controller);
    }

    public void startDataReceiving() {
        new Thread(() -> {
            isRunning = true;
            try {
                receiver.openSocket();
                System.out.println("Receiver thread runs");
            } catch (SocketException e) {
                e.printStackTrace();
            }

            while (isRunning && !receiver.getSocketStatus()) {
                DatagramPacket data;

                try {
                    data = receiver.receiveData();
                    System.out.println("incoming data: " + Arrays.toString(data.getData()));
                    validator.normalizeData(data);
                } catch (SocketException e) {
                    if (e.getMessage() != null && e.getMessage().contains("Socket closed")) {
                        System.out.println("Handled: socket closed");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

    public void stopDataReceiving() {
        isRunning = false;
        receiver.closeSocket();
        validator.resetQueue();
        validator.resetCounterValue();
        validator.resetMarkerFoundFlag();
        System.out.println("Receiving stopped");
    }
}
