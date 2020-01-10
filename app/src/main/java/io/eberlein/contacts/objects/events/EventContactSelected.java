package io.eberlein.contacts.objects.events;

import io.eberlein.contacts.objects.Contact;

public class EventContactSelected extends EventWithObject<Contact> {
    public EventContactSelected(Contact contact){
        super(contact);
    }
}
