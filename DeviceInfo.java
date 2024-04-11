package com.example.bluetoothscanner;

public class DeviceInfo {
    private int rssi;
    private String address;
    private long timestamp;

    public DeviceInfo(int rssi, String address, long timestamp) {
        this.rssi = rssi;
        this.address = address;
        this.timestamp = timestamp;
    }

    public String getRssi() {
        return String.valueOf(this.rssi);
    }
}




