package io.eberlein.contacts.dialogs;

import android.content.Context;
import android.content.DialogInterface;

import androidx.appcompat.app.AlertDialog;

import io.eberlein.contacts.R;

public class DialogImport {
    private AlertDialog.Builder builder;

    public DialogImport(Context ctx){
        builder = new AlertDialog.Builder(ctx).setTitle(ctx.getString(R.string.import_));
        builder.setMessage(ctx.getString(R.string.message_contacts_look_empty));
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // todo
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
    }

    public void show(){
        builder.show();
    }
}
