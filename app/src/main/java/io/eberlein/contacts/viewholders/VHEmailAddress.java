package io.eberlein.contacts.viewholders;

import android.view.View;

import org.greenrobot.eventbus.EventBus;

import io.eberlein.contacts.objects.EmailAddress;
import io.eberlein.contacts.objects.events.EventDeleteEmailAddress;
import io.eberlein.contacts.objects.events.EventSelectedEmailAddress;

public class VHEmailAddress extends VH<EmailAddress> {
    public VHEmailAddress(View v){
        super(v);
    }

    @Override
    public void onSetObject() {
        left_middle.setText(object.getEmail());
        one.setVisibility(View.GONE);
        two.setVisibility(View.GONE);
    }

    @Override
    public void onSelected() {
        EventBus.getDefault().post(new EventSelectedEmailAddress(object));
    }

    @Override
    public void onEdit() {
        EventBus.getDefault().post(new EventSelectedEmailAddress(object));
    }

    @Override
    public void onDelete() {
        EventBus.getDefault().post(new EventDeleteEmailAddress(object));
    }
}
