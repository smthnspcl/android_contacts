package io.eberlein.contacts.dialogs;

import android.content.Context;

import java.util.List;

import io.eberlein.contacts.R;
import io.eberlein.contacts.objects.Contact;
import io.realm.Realm;

public class DialogSyncInteractive extends DialogBase<List<Contact>> {
    private Realm realm;

    public DialogSyncInteractive(Context context, List<Contact> contacts, Realm realm){
        super(context, contacts, R.layout.dialog_sync_interactive);
        this.realm = realm;
    }

    @Override
    public void show() {
        // todo
    }
}
