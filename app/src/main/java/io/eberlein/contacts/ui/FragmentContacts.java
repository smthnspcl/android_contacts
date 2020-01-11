package io.eberlein.contacts.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.eberlein.contacts.R;
import io.eberlein.contacts.adapters.VHAdapter;
import io.eberlein.contacts.objects.Contact;
import io.eberlein.contacts.objects.events.EventOpenDialog;
import io.eberlein.contacts.viewholders.VHContact;
import io.realm.Realm;

public class FragmentContacts extends Fragment {
    private Realm realm;

    @BindView(R.id.recycler) RecyclerView recycler;

    @OnClick(R.id.btn_add_contact)
    void btnAddContactClicked(){
        EventBus.getDefault().post(new EventOpenDialog<Contact>(Contact.create(realm)));
    }

    public FragmentContacts(Realm realm){
        this.realm = realm;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_contacts, container, false);
        ButterKnife.bind(this, v);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        recycler.setAdapter(new VHAdapter<Contact, VHContact>(VHContact.class, realm.where(Contact.class).findAll()));
        return v;
    }
}
