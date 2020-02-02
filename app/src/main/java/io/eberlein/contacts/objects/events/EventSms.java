package io.eberlein.contacts.objects.events;

import io.eberlein.contacts.objects.Contact;

public class EventSms extends EventWithObject<Contact> {
    public EventSms(Contact c){super(c);}
}
