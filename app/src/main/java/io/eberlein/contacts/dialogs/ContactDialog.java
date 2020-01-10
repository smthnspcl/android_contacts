package io.eberlein.contacts.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import io.eberlein.contacts.R;
import io.eberlein.contacts.adapters.AddressAdapter;
import io.eberlein.contacts.objects.Address;
import io.eberlein.contacts.objects.Contact;
import io.eberlein.contacts.objects.events.EventChoosePicture;
import io.realm.Realm;


// todo option to set picture as dialog background

public class ContactDialog {
    private Contact contact;
    private Context context;

    @BindView(R.id.et_firstName) EditText firstName;
    @BindView(R.id.et_middleName) EditText middleName;
    @BindView(R.id.et_lastName) EditText lastName;
    @BindView(R.id.et_birthDay) EditText birthDay;
    @BindView(R.id.recycler) RecyclerView recycler;

    @OnLongClick(R.id.iv_picture)
    void onIVPictureLongClicked(){
        EventBus.getDefault().post(new EventChoosePicture()); // todo
    }

    @OnClick(R.id.iv_picture)
    void onIVPictureClicked(){
        // todo show picture fullscreen
    }

    @OnClick(R.id.btn_add_address)
    void onBtnAddAddressClicked(){
        Realm r = contact.getRealm();
        Address address = Address.create(r);
        r.beginTransaction();
        contact.getAddresses().add(address);
        r.commitTransaction();
        new AddressDialog(context, address).build();
    }

    public ContactDialog(@NonNull Context context, @NonNull Contact contact){
        this.contact = contact;
        this.context = context;
    }

    public void build(){
        View v = LayoutInflater.from(context).inflate(R.layout.dialog_contact, null, false);
        ButterKnife.bind(this, v);
        recycler.setLayoutManager(new LinearLayoutManager(context));
        recycler.setAdapter(new AddressAdapter(contact.getAddresses()));
        new AlertDialog.Builder(context)
                .setTitle(contact.getFullname(false))
                .setView(v)
                .setPositiveButton("save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        contact.getRealm().beginTransaction();
                        contact.setFirstName(firstName.getText().toString());
                        contact.setMiddleName(middleName.getText().toString());
                        contact.setLastName(lastName.getText().toString());
                        contact.setBirthDate(birthDay.getText().toString());
                        contact.getRealm().commitTransaction();
                    }
                }).setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }
}
