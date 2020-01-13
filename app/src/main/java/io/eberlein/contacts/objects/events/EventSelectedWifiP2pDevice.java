package io.eberlein.contacts.objects.events;

import android.net.wifi.p2p.WifiP2pDevice;

public class EventSelectedWifiP2pDevice extends EventWithObject<WifiP2pDevice> {
    public EventSelectedWifiP2pDevice(WifiP2pDevice device){
        super(device);
    }
}
