package io.eberlein.contacts.objects.events;

import android.net.nsd.NsdServiceInfo;

public class EventSelectedSyncDevice extends EventWithObject<NsdServiceInfo> {
    public EventSelectedSyncDevice(NsdServiceInfo nsi){
        super(nsi);
    }
}
