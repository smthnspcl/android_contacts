package io.eberlein.contacts.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.widget.EditText;

import butterknife.BindView;
import io.eberlein.contacts.R;
import io.eberlein.contacts.objects.Note;

public class DialogBaseNote extends DialogBase<Note> {
    @BindView(R.id.et_name) EditText name;
    @BindView(R.id.et_note) EditText note;

    public DialogBaseNote(Context context, Note note){
        super(context, note, R.layout.dialog_note);
    }

    @Override
    public void show() {
        Note n = getObject();
        name.setText(n.getName());
        note.setText(n.getNote());
        builder.setTitle("note")
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        n.getRealm().beginTransaction();
                        n.setName(name.getText().toString());
                        n.setNote(note.getText().toString());
                        n.getRealm().commitTransaction();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }
}
