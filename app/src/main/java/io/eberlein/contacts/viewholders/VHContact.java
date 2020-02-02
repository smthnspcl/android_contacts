package io.eberlein.contacts.viewholders;

import android.view.View;

import org.greenrobot.eventbus.EventBus;

import butterknife.OnClick;
import io.eberlein.contacts.R;
import io.eberlein.contacts.dialogs.DialogChooseNumber;
import io.eberlein.contacts.objects.Contact;
import io.eberlein.contacts.objects.events.EventCall;
import io.eberlein.contacts.objects.events.EventDeleteContact;
import io.eberlein.contacts.objects.events.EventSelectedContact;
import io.eberlein.contacts.objects.events.EventSms;

public class VHContact extends VH<Contact> {
    @OnClick(R.id.btn_one)
    void onBtnOneClicked(){
        EventBus.getDefault().post(new EventCall(object));
    }

    @OnClick(R.id.btn_two)
    void onBtnTwoClicked(){
        EventBus.getDefault().post(new EventSms(object));
    }

    public VHContact(View v){
        super(v);
    }

    @Override
    public void onSetObject() {
        left_middle.setText(object.getName());
    }

    @Override
    public void onBind() {
        one.setVisibility(View.VISIBLE);
        two.setVisibility(View.VISIBLE);
        one.setText(R.string.call);
        two.setText(R.string.sms);
    }

    @Override
    public void onCloseExtraMenu() {
        one.setVisibility(View.VISIBLE);
        two.setVisibility(View.VISIBLE);
    }

    @Override
    public void onOpenExtraMenu() {
        one.setVisibility(View.GONE);
        two.setVisibility(View.GONE);
    }

    @Override
    public void onSelected() {
        EventBus.getDefault().post(new EventSelectedContact(object));
    }

    @Override
    public void onEdit() {
        EventBus.getDefault().post(new EventSelectedContact(object));
    }

    @Override
    public void onDelete() {
        EventBus.getDefault().post(new EventDeleteContact(object));
    }
}
