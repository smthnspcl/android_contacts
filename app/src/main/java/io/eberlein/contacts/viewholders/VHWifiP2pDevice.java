package io.eberlein.contacts.viewholders;

import android.net.wifi.p2p.WifiP2pDevice;
import android.view.View;

import org.greenrobot.eventbus.EventBus;

import io.eberlein.contacts.objects.events.EventSelectedWifiP2pDevice;

public class VHWifiP2pDevice extends VH<WifiP2pDevice> {
    public VHWifiP2pDevice(View v){
        super(v);
    }

    @Override
    public void onSetObject() {
        left_up.setText(object.deviceName);
        left_bottom.setText(object.deviceAddress);
    }

    @Override
    public void onCloseExtraMenu() {

    }

    @Override
    public void onOpenExtraMenu() {

    }

    @Override
    public void onSelected() {
        EventBus.getDefault().post(new EventSelectedWifiP2pDevice(object));
    }
}
