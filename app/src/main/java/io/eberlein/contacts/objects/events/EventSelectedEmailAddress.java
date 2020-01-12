package io.eberlein.contacts.objects.events;

import io.eberlein.contacts.objects.EmailAddress;

public class EventSelectedEmailAddress extends EventWithObject<EmailAddress> {
    public EventSelectedEmailAddress(EmailAddress address){
        super(address);
    }
}
