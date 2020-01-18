package io.eberlein.contacts.dialogs;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import androidx.annotation.NonNull;

import io.eberlein.contacts.interfaces.DeleteDialogInterface;
import io.realm.RealmObject;

public class DialogDelete<T extends RealmObject> implements DeleteDialogInterface<T> {
    protected T object;
    private Context context;

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void delete(T object) {

    }

    public DialogDelete(@NonNull Context context, @NonNull T object){
        this.context = context;
        this.object = object;
    }

    public void build(){
        new AlertDialog.Builder(context)
                .setTitle("warning")
                .setMessage("are you sure you want to delete '" + getName() + "'?")
                .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        delete(object);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }
}
