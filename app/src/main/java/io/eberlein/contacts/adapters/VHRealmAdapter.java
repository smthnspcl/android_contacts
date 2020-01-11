package io.eberlein.contacts.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import io.eberlein.contacts.R;
import io.eberlein.contacts.viewholders.VH;
import io.realm.OrderedRealmCollection;
import io.realm.RealmObject;
import io.realm.RealmRecyclerViewAdapter;


public class VHRealmAdapter<T extends RealmObject, V extends VH<T>> extends RealmRecyclerViewAdapter<T, V> {
    private Class<V> cls;

    public VHRealmAdapter(Class<V> cls, OrderedRealmCollection<T> data){
        super(data, true);
        this.cls = cls;
    }

    @NonNull
    @Override
    public V onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return VH.create(cls, LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.setObject(getItem(position));
    }
}
