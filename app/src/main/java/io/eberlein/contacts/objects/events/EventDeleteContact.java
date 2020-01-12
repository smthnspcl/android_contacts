package io.eberlein.contacts.objects.events;

import io.eberlein.contacts.objects.Contact;

public class EventDeleteContact extends EventWithObject<Contact> {
    public EventDeleteContact(Contact contact){super(contact);}
}
