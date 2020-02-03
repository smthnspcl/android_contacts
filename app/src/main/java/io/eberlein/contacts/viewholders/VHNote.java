package io.eberlein.contacts.viewholders;

import android.view.View;

import org.greenrobot.eventbus.EventBus;

import io.eberlein.contacts.objects.Note;
import io.eberlein.contacts.objects.events.EventDeleteNote;
import io.eberlein.contacts.objects.events.EventSelectedNote;

public class VHNote extends VH<Note> {
    public VHNote(View v){
        super(v);
    }

    @Override
    public void onSetObject() {
        left_middle.setText(object.getName());
    }

    @Override
    public void onEdit() {
        EventBus.getDefault().post(new EventSelectedNote(object));
    }

    @Override
    public void onSelected() {
        EventBus.getDefault().post(new EventSelectedNote(object));
    }

    @Override
    public void onDelete() {
        EventBus.getDefault().post(new EventDeleteNote(object));
    }
}
