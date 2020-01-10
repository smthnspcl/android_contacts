package io.eberlein.contacts.dialogs;

import android.content.Context;

import androidx.annotation.NonNull;

import io.eberlein.contacts.objects.Address;

public class DeleteAddressDialog extends DeleteDialog<Address> {
    public DeleteAddressDialog(@NonNull Context context, @NonNull Address address){
        super(context, address);
    }

    @Override
    public String getName() {
        return object.getName();
    }

    @Override
    public void delete(Address object) {
        object.delete();
    }
}
