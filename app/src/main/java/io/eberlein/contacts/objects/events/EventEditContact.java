package io.eberlein.contacts.objects.events;

import io.eberlein.contacts.objects.Contact;

public class EventEditContact extends EventWithObject<Contact> {
    public EventEditContact(Contact contact){super(contact);}
}
