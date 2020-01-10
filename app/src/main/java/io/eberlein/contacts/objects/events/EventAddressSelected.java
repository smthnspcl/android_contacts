package io.eberlein.contacts.objects.events;

import io.eberlein.contacts.objects.Address;

public class EventAddressSelected extends EventWithObject<Address> {
    public EventAddressSelected(Address address){
        super(address);
    }
}
