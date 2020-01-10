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
import io.eberlein.contacts.objects.Address;
import io.eberlein.contacts.objects.events.EventAddressSelected;
import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

public class AddressAdapter extends RealmRecyclerViewAdapter<Address, AddressAdapter.ViewHolder> {
    public AddressAdapter(OrderedRealmCollection<Address> data){
        super(data, true);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private boolean extraMenuOpen = false;
        private Address address;

        @BindView(R.id.tv_address_fine) TextView addressFine;
        @BindView(R.id.tv_address_course) TextView addressCourse;
        @BindView(R.id.btn_edit) Button edit;
        @BindView(R.id.btn_delete) Button delete;

        @OnClick
        void onClick(){
            if(extraMenuOpen) closeExtraMenu();
            else EventBus.getDefault().post(new EventAddressSelected(address));
        }

        @OnLongClick
        void onLongClick(){
            if(extraMenuOpen) closeExtraMenu();
            else openExtraMenu();
        }

        void openExtraMenu(){
            extraMenuOpen = true;
            edit.setVisibility(View.VISIBLE);
            delete.setVisibility(View.VISIBLE);
        }

        void closeExtraMenu(){
            extraMenuOpen = false;
            edit.setVisibility(View.GONE);
            delete.setVisibility(View.GONE);
        }

        ViewHolder(View v){
            super(v);
            ButterKnife.bind(this, v);
        }

        void setAddress(Address address){
            this.address = address;
            addressFine.setText(address.getAddressFine());
            addressCourse.setText(address.getAddressCourse());
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setAddress(getItem(position));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_address, parent, false));
    }
}
