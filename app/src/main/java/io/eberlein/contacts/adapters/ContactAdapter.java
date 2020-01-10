package io.eberlein.contacts.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import io.eberlein.contacts.R;
import io.eberlein.contacts.objects.Contact;
import io.eberlein.contacts.objects.events.EventContactSelected;
import io.eberlein.contacts.objects.events.EventDeleteContact;
import io.eberlein.contacts.objects.events.EventEditContact;
import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

public class ContactAdapter extends RealmRecyclerViewAdapter<Contact, ContactAdapter.ViewHolder> {
    public ContactAdapter(OrderedRealmCollection<Contact> data){
        super(data, true);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private boolean extraMenuOpen = false;
        private Contact contact;

        @BindView(R.id.tv_name) TextView name;
        @BindView(R.id.btn_delete) Button delete;
        @BindView(R.id.btn_edit) Button edit;

        @OnClick(R.id.btn_delete)
        void onBtnDeleteClicked(){
            EventBus.getDefault().post(new EventDeleteContact(contact));
        }

        @OnClick(R.id.btn_edit)
        void onBtnEditClicked(){
            EventBus.getDefault().post(new EventEditContact(contact));
        }

        @OnClick
        void onClick(){
            if(extraMenuOpen) closeExtraMenu();
            else EventBus.getDefault().post(new EventContactSelected(contact));
        }

        @OnLongClick
        void onLongClick(){
            if(extraMenuOpen) closeExtraMenu();
            else openExtraMenu();
        }

        void openExtraMenu(){
            extraMenuOpen = true;
            delete.setVisibility(View.VISIBLE);
            edit.setVisibility(View.VISIBLE);
        }

        void closeExtraMenu(){
            extraMenuOpen = false;
            delete.setVisibility(View.GONE);
            edit.setVisibility(View.GONE);
        }

        ViewHolder(View v){
            super(v);
            ButterKnife.bind(this, v);
        }

        void setContact(Contact contact){
            this.contact = contact;
            name.setText(contact.getFullname(false)); // todo make withMiddleName settable
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_contact, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setContact(getItem(position));
    }
}
