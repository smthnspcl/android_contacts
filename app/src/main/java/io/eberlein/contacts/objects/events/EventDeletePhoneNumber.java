package io.eberlein.contacts.objects.events;

import io.eberlein.contacts.objects.PhoneNumber;

public class EventDeletePhoneNumber extends EventWithObject<PhoneNumber> {
    public EventDeletePhoneNumber(PhoneNumber phoneNumber){
        super(phoneNumber);
    }
}
