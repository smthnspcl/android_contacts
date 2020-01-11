package io.eberlein.contacts.viewholders;

import android.view.View;

import io.eberlein.contacts.objects.EmailAddress;

public class VHEmailAddress extends VH<EmailAddress> {
    public VHEmailAddress(View v){
        super(v);
    }

    @Override
    void onSetObject() {
        left_middle.setText(object.getEmail());
    }
}
