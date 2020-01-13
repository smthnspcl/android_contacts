package io.eberlein.contacts.objects.events;

import io.eberlein.contacts.objects.SyncConfiguration;

public class EventSync extends EventWithObject<SyncConfiguration> {
    public EventSync(SyncConfiguration c){
        super(c);
    }
}
