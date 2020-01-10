package io.eberlein.contacts.objects.events;

public class EventWithObject<T> {
    private T object;

    public EventWithObject(T object){
        this.object = object;
    }

    public T getObject() {
        return object;
    }
}
