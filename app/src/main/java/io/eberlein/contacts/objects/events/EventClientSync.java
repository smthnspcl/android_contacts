package io.eberlein.contacts.objects.events;

import io.eberlein.contacts.objects.ClientSyncConfiguration;

public class EventClientSync extends EventWithObject<ClientSyncConfiguration> {
    public EventClientSync(ClientSyncConfiguration c){
        super(c);
    }
}
