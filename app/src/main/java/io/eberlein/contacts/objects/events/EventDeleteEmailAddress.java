package io.eberlein.contacts.objects.events;

import io.eberlein.contacts.objects.EmailAddress;

public class EventDeleteEmailAddress extends EventWithObject<EmailAddress> {
    public EventDeleteEmailAddress(EmailAddress address){
        super(address);
    }
}
