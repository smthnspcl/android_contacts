package io.eberlein.contacts.objects.events;

import io.eberlein.contacts.objects.Address;

public class EventSelectedAddress extends EventWithObject<Address> {
    public EventSelectedAddress(Address address){
        super(address);
    }
}
