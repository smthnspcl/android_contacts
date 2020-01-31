package io.eberlein.contacts.objects.events;

import io.eberlein.contacts.objects.Contact;

public class EventCall extends EventWithObject<Contact> {
    public EventCall(Contact c){
        super(c);
    }
}
