package io.eberlein.contacts.viewholders;

import android.view.View;

import org.greenrobot.eventbus.EventBus;

import io.eberlein.contacts.objects.PhoneNumber;
import io.eberlein.contacts.objects.events.EventDeletePhoneNumber;
import io.eberlein.contacts.objects.events.EventSelectedPhoneNumber;

public class VHPhoneNumber extends VH<PhoneNumber> {
    public VHPhoneNumber(View v){
        super(v);
    }

    @Override
    public void onSetObject() {
        left_middle.setText(object.getNumber());
        one.setVisibility(View.GONE);
        two.setVisibility(View.GONE);
    }

    @Override
    public void onEdit() {
        EventBus.getDefault().post(new EventSelectedPhoneNumber(object));
    }

    @Override
    public void onSelected() {
        EventBus.getDefault().post(new EventSelectedPhoneNumber(object));
    }

    @Override
    public void onDelete() {
        EventBus.getDefault().post(new EventDeletePhoneNumber(object));
    }
}
