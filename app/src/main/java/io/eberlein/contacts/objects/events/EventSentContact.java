package io.eberlein.contacts.objects.events;

import io.eberlein.contacts.objects.Contact;

public class EventSentContact extends EventWithObject<Contact> {
    public EventSentContact(Contact c){
        super(c);
    }
}
