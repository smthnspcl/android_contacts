package io.eberlein.contacts.objects.events;

public class EventEncryptionDone extends EventWithObject<String>{
    public EventEncryptionDone(String password){
        super(password);
    }
}
