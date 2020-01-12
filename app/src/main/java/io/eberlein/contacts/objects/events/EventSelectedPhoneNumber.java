package io.eberlein.contacts.objects.events;

import io.eberlein.contacts.objects.PhoneNumber;

public class EventSelectedPhoneNumber extends EventWithObject<PhoneNumber> {
    public EventSelectedPhoneNumber(PhoneNumber phoneNumber){
        super(phoneNumber);
    }
}
