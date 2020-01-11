package io.eberlein.contacts.viewholders;

import android.view.View;

import io.eberlein.contacts.objects.Address;

public class VHAddress extends VH<Address> {
    public VHAddress(View v){
        super(v);
    }

    @Override
    void onSetObject() {
        left_up.setText(object.getAddressFine());
        left_bottom.setText(object.getAddressCourse());
        one.setVisibility(View.GONE);
        two.setVisibility(View.GONE);
    }
}
