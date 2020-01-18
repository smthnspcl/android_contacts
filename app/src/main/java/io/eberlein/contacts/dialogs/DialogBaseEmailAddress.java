package io.eberlein.contacts.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.widget.EditText;

import butterknife.BindView;
import io.eberlein.contacts.R;
import io.eberlein.contacts.objects.EmailAddress;

public class DialogBaseEmailAddress extends DialogBase<EmailAddress> {
    @BindView(R.id.et_name) EditText name;
    @BindView(R.id.et_email) EditText email;

    public DialogBaseEmailAddress(Context context, EmailAddress emailAddress){super(context, emailAddress, R.layout.dialog_email_address);}

    @Override
    public void show() {
        EmailAddress emailAddress = getObject();
        name.setText(emailAddress.getName());
        email.setText(emailAddress.getEmail());
        builder.setTitle("email address")
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        emailAddress.getRealm().beginTransaction();
                        emailAddress.setName(name.getText().toString());
                        emailAddress.setEmail(email.getText().toString());
                        emailAddress.getRealm().commitTransaction();
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
