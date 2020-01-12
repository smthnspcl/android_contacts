package io.eberlein.contacts.objects.events;

import io.eberlein.contacts.objects.Contact;

public class EventSelectedContact extends EventWithObject<Contact> {
    public EventSelectedContact(Contact contact){super(contact);}
}
