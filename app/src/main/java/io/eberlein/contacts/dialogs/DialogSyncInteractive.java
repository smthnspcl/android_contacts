package io.eberlein.contacts.dialogs;

import android.content.Context;

import io.eberlein.contacts.R;
import io.eberlein.contacts.objects.Contact;

public class DialogSyncInteractive extends DialogBase<Contact> {
    public DialogSyncInteractive(Context context, Contact contact){
        super(context, contact, R.layout.dialog_sync_interactive);
    }

    @Override
    public void show() {
        // todo
    }
}
