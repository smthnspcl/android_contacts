package io.eberlein.contacts.dialogs;

import android.content.Context;

import io.eberlein.contacts.R;
import io.eberlein.contacts.objects.EmailAddress;

public class EmailAddressDialog extends BaseDialog<EmailAddress> {
    public EmailAddressDialog(Context context, EmailAddress emailAddress){super(context, emailAddress, R.layout.dialog_email_address);}
}
