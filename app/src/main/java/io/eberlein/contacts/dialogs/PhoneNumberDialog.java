package io.eberlein.contacts.dialogs;

import android.content.Context;

import io.eberlein.contacts.R;
import io.eberlein.contacts.objects.PhoneNumber;

public class PhoneNumberDialog extends BaseDialog<PhoneNumber> {
    public PhoneNumberDialog(Context context, PhoneNumber phoneNumber){super(context, phoneNumber, R.layout.dialog_phone_number);}
}
