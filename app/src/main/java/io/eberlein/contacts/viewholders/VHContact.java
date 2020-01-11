package io.eberlein.contacts.viewholders;

import android.view.View;

import butterknife.OnClick;
import io.eberlein.contacts.R;
import io.eberlein.contacts.objects.Contact;

public class VHContact extends VH<Contact> {
    @OnClick(R.id.btn_one)
    void onBtnOneClicked(){
        // todo intent to phone
    }

    @OnClick(R.id.btn_two)
    void onBtnTwoClicked(){
        // todo intent to sms
    }

    public VHContact(View v){
        super(v);
    }

    @Override
    void onSetObject() {
        left_middle.setText(object.getName());
        one.setText(R.string.call);
        two.setText(R.string.sms);
    }
}
