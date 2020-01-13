package io.eberlein.contacts.objects;

import android.net.wifi.p2p.WifiP2pDevice;

public class SyncConfiguration {
    private boolean encrypted;
    private boolean interactive;
    private WifiP2pDevice device;

    public WifiP2pDevice getDevice() {
        return device;
    }

    public boolean isEncrypted() {
        return encrypted;
    }

    public boolean isInteractive() {
        return interactive;
    }

    public void setDevice(WifiP2pDevice device) {
        this.device = device;
    }

    public void setEncrypted(boolean encrypted) {
        this.encrypted = encrypted;
    }

    public void setInteractive(boolean interactive) {
        this.interactive = interactive;
    }

    public boolean isInvalid(){
        return device == null;
    }
}
