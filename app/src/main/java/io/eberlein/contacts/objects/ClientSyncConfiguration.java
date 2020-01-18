package io.eberlein.contacts.objects;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.Nullable;

public class ClientSyncConfiguration {
    private String encryptionKey;
    private boolean isInteractive;
    private BluetoothDevice device;

    public ClientSyncConfiguration(BluetoothDevice device, boolean isInteractive, @Nullable String encryptionKey){
        this.device = device;
        this.encryptionKey = encryptionKey;
        this.isInteractive = isInteractive;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public String getEncryptionKey() {
        return encryptionKey;
    }

    public boolean isInteractive() {
        return isInteractive;
    }
}
