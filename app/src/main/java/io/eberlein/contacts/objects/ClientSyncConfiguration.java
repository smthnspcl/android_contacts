package io.eberlein.contacts.objects;

import android.net.wifi.p2p.WifiP2pDevice;

import androidx.annotation.Nullable;

public class ClientSyncConfiguration {
    private String encryptionKey;
    private boolean isInteractive;
    private WifiP2pDevice device;

    public ClientSyncConfiguration(WifiP2pDevice device, boolean isInteractive, @Nullable String encryptionKey){
        this.device = device;
        this.encryptionKey = encryptionKey;
        this.isInteractive = isInteractive;
    }

    public WifiP2pDevice getDevice() {
        return device;
    }

    public String getEncryptionKey() {
        return encryptionKey;
    }

    public boolean isInteractive() {
        return isInteractive;
    }
}
