package com.artoch.tlm_app;

import org.apache.commons.lang3.ArrayUtils;

import java.net.DatagramPacket;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DataValidator {
    private final List<com.artoch.tlm_app.Observer> observers = new ArrayList<>();
    private final Queue<Byte> dataQueue;
    private final int packetSize = 26;
    private static final long SYNC_MARKER = 0x12345678;
    private final byte[] marker = {120, 86, 52, 18};
    private Long previousPacketNumber = 0L;
    private boolean isMarkerFound;

    public DataValidator() {
        dataQueue = new LinkedList<>();
    }

    public void addObserver(com.artoch.tlm_app.Observer observer) {
        observers.add(observer);
    }

    public void notifyObservers(DataModel data) {
        for (Observer observer : observers) {
            observer.update(data);
        }
    }



    public void normalizeData(DatagramPacket packet) {

        byte[] packetArr = packet.getData();

        for (int i = 0; i < packet.getLength(); i++) {
            dataQueue.add(packetArr[i]);
        }

        System.out.println("Queue: " + dataQueue);
        System.out.println("Queue size: " + dataQueue.size());

        byte[] dataToDecode = new byte[packetSize];

        // Минимальный размер очереди, в который гарантированно поместится один полный пакет заданного размера
        if (dataQueue.size() >= packetSize * 2 - 1) {

            if (!isMarkerFound) {
                System.out.println("Looking for a marker...");
                byte[] tempArr = new byte[dataQueue.size()];
                Iterator<Byte> byteIterator = dataQueue.iterator();

                int index = 0;
                while (byteIterator.hasNext()) {
                    tempArr[index] = byteIterator.next();
                    index++;
                }

                byte[] noTrashPrefixData = findMarker(tempArr);
                int queueSize = dataQueue.size();
                System.out.println("Queue size at the moment: " + queueSize);
                System.out.println("Queue size without prefix: " + noTrashPrefixData.length);
                for (int i = 0; i < queueSize - noTrashPrefixData.length; i++) {
                    dataQueue.remove();
                }
                System.out.println("Queue after prefix was removed: " + dataQueue);

                for (int i = 0; i < dataToDecode.length; i++) {
                    dataToDecode[i] = dataQueue.remove();
                }

            } else {
                for (int i = 0; i < dataToDecode.length; i++) {
                    dataToDecode[i] = dataQueue.remove();
                }
            }

            System.out.println("Data packet was cut from the queue; queue size is: " + dataQueue.size());
            System.out.println("Packet to decode: " + Arrays.toString(dataToDecode));
            System.out.println("Packet size: " + dataToDecode.length + "\n");

            decodeData(dataToDecode);

        }
    }

    public void decodeData(byte[] tlmData) {

        System.out.println("Packet data:\n");
        // Если синхромаркер бьётся, извлекаем данные
        if (arrayToHexLong(Arrays.copyOfRange(tlmData, 0, 4)) == SYNC_MARKER) {

            long packetCounter = arrayToHexLong(Arrays.copyOfRange(tlmData, 4, 8));
            System.out.println("Counter: " + packetCounter);

            // Сравнение номера текущего пакета с номером предыдущего
            boolean isCounterIncrementCorrect = checkIncrement(packetCounter);
            previousPacketNumber = packetCounter;

            long time = arrayToHexLong(Arrays.copyOfRange(tlmData, 8, 16));
            double timestamp = Double.longBitsToDouble(time);
            long seconds = (long) timestamp;
            int nanos = (int) ((timestamp - seconds) * 1_000_000_000);

            Instant instant = Instant.ofEpochSecond(seconds, nanos);
            ZonedDateTime dateTime = instant.atZone(ZoneId.of("UTC"));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            System.out.println("Date and Time: " + dateTime.format(formatter));

            double sin = Double.longBitsToDouble(arrayToHexLong(
                    Arrays.copyOfRange(tlmData, 16, 24)));

            System.out.println("sin: " + sin);

            int checksum = (int) (arrayToHexLong(
                    Arrays.copyOfRange(tlmData, 24, 26)));

            int calcChecksum = calculateCheckSum(
                    Arrays.copyOfRange(tlmData, 0, 24));

            System.out.println("Checksum: " + checksum);

            boolean isSumsMatch = checksum == calcChecksum;

            System.out.println("Checksums match: " + isSumsMatch + "\n");

            /* Если не бьётся контрольная сумма или порядок номеров пакетов каким-либо образом сбит,
               в контроллер передаётся флаг для выделения строки соответствующим цветом */

            DataModel dataModel = new DataModel(packetCounter, dateTime.format(formatter), sin,
                    checksum, isSumsMatch, setHighlightColour(isCounterIncrementCorrect, isSumsMatch));

            notifyObservers(dataModel);

        }
    }



    private long arrayToHexLong(byte[] arr) {
        ArrayUtils.reverse(arr);

        String hexString = IntStream.range(0, arr.length)
                .mapToObj(i -> String.format("%02x", arr[i] & 0xFF))
                .collect(Collectors.joining());

        return Long.parseUnsignedLong(hexString, 16);
    }

    // CRC16_CCITT-FALSE
    public int calculateCheckSum(byte[] data) {
        int crc = 0xFFFF;
        for (byte b : data) {
            crc ^= (b << 8);

            for (int i = 0; i < 8; i++) {
                if ((crc & 0x8000) != 0) {
                    crc = (crc << 1) ^ 0x1021;
                } else {
                    crc <<= 1;
                }
            }
        }
        return crc & 0xFFFF;
    }

    /* При старте или возобновлении приёма в случае, если данные начинаются не с синхромаркера,
       метод удаляет лишнее начало и возвращает массив, начинающийся с синхромаркера */

    public byte[] findMarker(byte[] data) {
        int index = 0;

        for (int i = 0; i < data.length - 1 - marker.length; ) {
            byte[] potentialMarker = Arrays.copyOfRange(data, i, i + marker.length);
            System.out.println(Arrays.toString(potentialMarker));
            if (Arrays.equals(potentialMarker, marker)) {
                isMarkerFound = true;
                System.out.println("Marker found!");
                index = i;
                break;
            } else {
                i++;
            }
        }
        return Arrays.copyOfRange(data, index, data.length);
    }

    // Проверка соблюдения очерёдности номеров пакетов
    public boolean checkIncrement(Long actualIndex) {
        return actualIndex - 1 == previousPacketNumber;
    }

    // Выделение строки красным, если не бьётся контрольная сумма и жёлтым, если сбит порядковый номер пакета
    public String setHighlightColour(boolean packetsByOrder, boolean checksumsMatch) {

        if (!checksumsMatch) {
            return "lightpink";
        } else if (!packetsByOrder) {
            return "khaki";
        }

        return "";
    }

    // Сброс внутреннего счётчика пакетов в случае остановки приёма
    public void resetCounterValue() {
        previousPacketNumber = 0L;
        System.out.println("Counter reset to " + previousPacketNumber);
    }

    // Очистка очереди
    public void resetQueue() {
        dataQueue.clear();
        System.out.println("The queue has been cleared");
        System.out.println("Queue: " + dataQueue);
    }

    /* Чтобы когда входящие данные начинаются не с синхромаркера,
       найти синхромаркер и обрезать лишнее перед ним */

    public void resetMarkerFoundFlag() {
        isMarkerFound = false;
    }


}

