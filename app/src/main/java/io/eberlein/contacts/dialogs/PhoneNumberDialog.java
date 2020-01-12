package io.eberlein.contacts.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.widget.EditText;

import butterknife.BindView;
import io.eberlein.contacts.R;
import io.eberlein.contacts.objects.PhoneNumber;

public class PhoneNumberDialog extends BaseDialog<PhoneNumber> {
    @BindView(R.id.et_name) EditText name;
    @BindView(R.id.et_phone_number) EditText phoneNumber;

    public PhoneNumberDialog(Context context, PhoneNumber phoneNumber){super(context, phoneNumber, R.layout.dialog_phone_number);}

    @Override
    public void show() {
        PhoneNumber number = getObject();
        name.setText(number.getName());
        phoneNumber.setText(number.getNumber());
        builder.setTitle("phone number")
                .setPositiveButton("save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        number.getRealm().beginTransaction();
                        number.setName(name.getText().toString());
                        number.setNumber(phoneNumber.getText().toString());
                        number.getRealm().commitTransaction();
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
