package io.eberlein.contacts.dialogs;

import android.content.Context;

import androidx.annotation.NonNull;

import io.eberlein.contacts.objects.Contact;

public class DialogContactDelete extends DialogDelete<Contact> {
    public DialogContactDelete(@NonNull Context context, @NonNull Contact contact){
        super(context, contact);
    }

    @Override
    public String getName() {
        return object.getName();
    }

    @Override
    public void delete(Contact object) {
        object.delete();
    }
}
