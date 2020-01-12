package io.eberlein.contacts.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.widget.CheckBox;
import android.widget.EditText;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.OnLongClick;
import io.eberlein.contacts.R;
import io.eberlein.contacts.adapters.VHRealmAdapter;
import io.eberlein.contacts.objects.Address;
import io.eberlein.contacts.objects.Contact;
import io.eberlein.contacts.objects.EmailAddress;
import io.eberlein.contacts.objects.Note;
import io.eberlein.contacts.objects.PhoneNumber;
import io.eberlein.contacts.objects.events.EventChoosePicture;
import io.eberlein.contacts.viewholders.VHAddress;
import io.eberlein.contacts.viewholders.VHEmailAddress;
import io.eberlein.contacts.viewholders.VHNote;
import io.eberlein.contacts.viewholders.VHPhoneNumber;
import io.realm.Realm;


// todo option to set picture as dialog background

public class ContactDialog extends BaseDialog<Contact>{
    @BindView(R.id.et_firstName) EditText firstName;
    @BindView(R.id.et_middleName) EditText middleName;
    @BindView(R.id.cb_show_middle_name) CheckBox displayMiddleName;
    @BindView(R.id.et_lastName) EditText lastName;
    @BindView(R.id.et_birthDay) EditText birthDay;
    @BindView(R.id.rv_addresses) RecyclerView addresses;
    @BindView(R.id.rv_phone_numbers) RecyclerView phoneNumbers;
    @BindView(R.id.rv_email_addresses) RecyclerView emailAddresses;
    @BindView(R.id.rv_notes) RecyclerView notes;

    @OnLongClick(R.id.iv_picture)
    void onIVPictureLongClicked(){
        EventBus.getDefault().post(new EventChoosePicture()); // todo
    }

    @OnClick(R.id.iv_picture)
    void onIVPictureClicked(){
        // todo show picture fullscreen
    }

    @OnCheckedChanged(R.id.cb_show_middle_name)
    void onCheckBoxShowMiddleNameCheckedChanged(){
        Contact contact = getObject();
        contact.getRealm().beginTransaction();
        contact.setMiddleNameDisplayed(displayMiddleName.isChecked());
        contact.getRealm().commitTransaction();
    }

    @OnClick(R.id.btn_add_address)
    void onBtnAddAddressClicked(){
        Contact contact = getObject();
        Realm r = contact.getRealm();
        Address address = Address.create(r);
        r.beginTransaction();
        contact.getAddresses().add(address);
        r.commitTransaction();
        new AddressDialog(getContext(), address).show();
    }

    @OnClick(R.id.btn_add_phone_number)
    void onBtnAddPhoneNumberClicked(){
        Contact contact = getObject();
        Realm r = contact.getRealm();
        PhoneNumber phoneNumber = PhoneNumber.create(r);
        r.beginTransaction();
        contact.getPhoneNumbers().add(phoneNumber);
        r.commitTransaction();
        new PhoneNumberDialog(getContext(), phoneNumber).show();
    }

    @OnClick(R.id.btn_add_email_address)
    void onBtnAddEmailAddressClicked(){
        Contact contact = getObject();
        Realm r = contact.getRealm();
        EmailAddress emailAddress = EmailAddress.create(r);
        r.beginTransaction();
        contact.getEmailAddresses().add(emailAddress);
        r.commitTransaction();
        new EmailAddressDialog(getContext(), emailAddress).show();
    }

    @OnClick(R.id.btn_add_note)
    void onBtnAddNoteClicked(){
        Contact contact = getObject();
        Realm r = contact.getRealm();
        Note note = Note.create(r);
        r.beginTransaction();
        contact.getNotes().add(note);
        r.commitTransaction();
        new NoteDialog(getContext(), note).show();
    }


    public ContactDialog(Context context, Contact contact){
        super(context, contact, R.layout.dialog_contact);
    }

    private void populate(){
        Contact contact = getObject();
        firstName.setText(contact.getFirstName());
        middleName.setText(contact.getMiddleName());
        lastName.setText(contact.getLastName());
        displayMiddleName.setChecked(contact.isMiddleNameDisplayed());
        birthDay.setText(contact.getBirthDate());
        addresses.setLayoutManager(new LinearLayoutManager(getContext()));
        addresses.setAdapter(new VHRealmAdapter<>(VHAddress.class, contact.getAddresses()));
        phoneNumbers.setLayoutManager(new LinearLayoutManager(getContext()));
        phoneNumbers.setAdapter(new VHRealmAdapter<>(VHPhoneNumber.class, contact.getPhoneNumbers()));
        emailAddresses.setLayoutManager(new LinearLayoutManager(getContext()));
        emailAddresses.setAdapter(new VHRealmAdapter<>(VHEmailAddress.class, contact.getEmailAddresses()));
        notes.setLayoutManager(new LinearLayoutManager(getContext()));
        notes.setAdapter(new VHRealmAdapter<>(VHNote.class, contact.getNotes()));
    }

    public void show(){
        populate();
        Contact contact = getObject();
        builder.setTitle(contact.getName())
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
