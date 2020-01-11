package io.eberlein.contacts.viewholders;

import android.view.View;

import io.eberlein.contacts.objects.PhoneNumber;

public class VHPhoneNumber extends VH<PhoneNumber> {
    public VHPhoneNumber(View v){
        super(v);
    }

    @Override
    void onSetObject() {
        left_middle.setText(object.getNumber());
        one.setVisibility(View.GONE);
        two.setVisibility(View.GONE);
    }
}
