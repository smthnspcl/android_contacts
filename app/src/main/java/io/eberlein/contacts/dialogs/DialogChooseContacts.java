package io.eberlein.contacts.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import io.eberlein.contacts.R;
import io.eberlein.contacts.objects.Contact;
import io.eberlein.contacts.objects.events.EventSyncSelectedContacts;

public class DialogChooseContacts extends DialogBase<List<Contact>> {
    @BindView(R.id.rv_contacts) RecyclerView recyclerView;

    private List<Contact> selectedContacts = new ArrayList<>();

    class ViewHolder extends RecyclerView.ViewHolder {
        private Contact contact;

        @BindView(R.id.tv_name) TextView name;
        @BindView(R.id.cb_sync) CheckBox sync;

        @OnCheckedChanged(R.id.cb_sync)
        void onCheckboxSyncChanged(){
            if(sync.isChecked()) selectedContacts.add(contact);
            else selectedContacts.remove(contact);
        }

        ViewHolder(View v){
            super(v);
            ButterKnife.bind(this, v);
        }

        void setContact(Contact contact){
            this.contact = contact;
            name.setText(contact.getName());
        }
    }

    public DialogChooseContacts(Context ctx, List<Contact> contacts){
        super(ctx, contacts, R.layout.dialog_choose_contacts);
        recyclerView.setAdapter(new RecyclerView.Adapter<ViewHolder>(){
            @Override
            public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
                holder.setContact(getObject().get(position));
            }

            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new ViewHolder(LayoutInflater.from(getContext()).inflate(R.layout.item_choose_contact, parent, false));
            }

            @Override
            public int getItemCount() {
                return getObject().size();
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
    }

    @Override
    public void show() {
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EventBus.getDefault().post(new EventSyncSelectedContacts(selectedContacts));
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }
}
