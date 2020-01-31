package io.eberlein.contacts.objects.events;

import java.util.List;

import io.eberlein.contacts.objects.Contact;

public class EventSyncSelectedContacts extends EventWithObject<List<Contact>> {
    public EventSyncSelectedContacts(List<Contact> contacts){super(contacts);}
}
