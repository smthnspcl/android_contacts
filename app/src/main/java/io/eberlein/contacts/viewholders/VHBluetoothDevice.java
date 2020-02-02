package io.eberlein.contacts.viewholders;

import android.bluetooth.BluetoothDevice;
import android.view.View;

import org.greenrobot.eventbus.EventBus;

import io.eberlein.contacts.objects.events.EventSelectedBluetoothDevice;

public class VHBluetoothDevice extends VH<BluetoothDevice> {
    public VHBluetoothDevice(View v){
        super(v);
    }

    @Override
    public void onSetObject() {
        left_up.setText(object.getAddress());
        left_middle.setText(object.getName());
    }

    @Override
    public void onOpenExtraMenu() {
        delete.setVisibility(View.GONE);
        edit.setVisibility(View.GONE);
    }

    @Override
    public void onCloseExtraMenu() {
        one.setVisibility(View.GONE);
        two.setVisibility(View.GONE);
    }

    @Override
    public void onSelected() {
        EventBus.getDefault().post(new EventSelectedBluetoothDevice(object));
    }
}
