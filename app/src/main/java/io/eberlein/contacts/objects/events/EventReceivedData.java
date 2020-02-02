package io.eberlein.contacts.objects.events;

import java.util.List;

import io.eberlein.contacts.objects.Contact;

public class EventReceivedData extends EventWithObject<List<Contact>> {
    public EventReceivedData(List<Contact> data){super(data);}
}
