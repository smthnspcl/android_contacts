package io.eberlein.contacts.viewholders;

import android.view.View;

import org.greenrobot.eventbus.EventBus;

import io.eberlein.contacts.objects.Address;
import io.eberlein.contacts.objects.events.EventDeleteAddress;
import io.eberlein.contacts.objects.events.EventSelectedAddress;

public class VHAddress extends VH<Address> {
    public VHAddress(View v){
        super(v);
    }

    @Override
    public void onSetObject() {
        left_up.setText(object.getAddressFine());
        left_bottom.setText(object.getAddressCourse());
        one.setVisibility(View.GONE);
        two.setVisibility(View.GONE);
    }

    @Override
    public void onEdit() {
        EventBus.getDefault().post(new EventSelectedAddress(object));
    }

    @Override
    public void onSelected() {
        EventBus.getDefault().post(new EventSelectedAddress(object));
    }

    @Override
    public void onDelete() {
        EventBus.getDefault().post(new EventDeleteAddress(object));
    }
}
