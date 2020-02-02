package io.eberlein.contacts.objects.events;

import io.eberlein.contacts.objects.Note;

public class EventSelectedNote extends EventWithObject<Note> {
    public EventSelectedNote(Note note){
        super(note);
    }
}
