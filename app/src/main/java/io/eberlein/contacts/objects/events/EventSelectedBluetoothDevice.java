package io.eberlein.contacts.objects.events;

import android.bluetooth.BluetoothDevice;

public class EventSelectedBluetoothDevice extends EventWithObject<BluetoothDevice> {
    public EventSelectedBluetoothDevice(BluetoothDevice device){
        super(device);
    }
}
