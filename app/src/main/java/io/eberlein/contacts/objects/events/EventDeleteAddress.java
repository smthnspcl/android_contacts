package io.eberlein.contacts.objects.events;

import io.eberlein.contacts.objects.Address;

public class EventDeleteAddress extends EventWithObject<Address> {
    public EventDeleteAddress(Address address){super(address);}
}