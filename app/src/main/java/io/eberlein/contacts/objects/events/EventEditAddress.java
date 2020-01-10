package io.eberlein.contacts.objects.events;

import io.eberlein.contacts.objects.Address;

public class EventEditAddress extends EventWithObject<Address> {
    public EventEditAddress(Address address){
        super(address);
    }
}
