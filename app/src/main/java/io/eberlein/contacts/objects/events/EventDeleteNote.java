package io.eberlein.contacts.objects.events;

import io.eberlein.contacts.objects.Note;

public class EventDeleteNote extends EventWithObject<Note> {
    public EventDeleteNote(Note note){
        super(note);
    }
}
