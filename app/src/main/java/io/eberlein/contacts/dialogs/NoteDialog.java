package io.eberlein.contacts.dialogs;

import android.content.Context;

import io.eberlein.contacts.R;
import io.eberlein.contacts.objects.Note;

public class NoteDialog extends BaseDialog<Note> {
    public NoteDialog(Context context, Note note){
        super(context, note, R.layout.dialog_note);
    }
}
