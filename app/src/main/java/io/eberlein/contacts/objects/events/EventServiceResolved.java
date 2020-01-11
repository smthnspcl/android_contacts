package io.eberlein.contacts.objects.events;

import android.net.nsd.NsdServiceInfo;

public class EventServiceResolved extends EventWithObject<NsdServiceInfo> {
    public EventServiceResolved(NsdServiceInfo object){super(object);}
}
