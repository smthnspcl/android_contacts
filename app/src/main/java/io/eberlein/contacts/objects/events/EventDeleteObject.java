package io.eberlein.contacts.objects.events;

public class EventDeleteObject<T> extends EventWithObject<T> {
    public EventDeleteObject(T object){super(object);}
}
