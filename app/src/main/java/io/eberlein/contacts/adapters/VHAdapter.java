package io.eberlein.contacts.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import io.eberlein.contacts.R;
import io.eberlein.contacts.viewholders.VH;


public class VHAdapter<T, V extends VH<T>> extends RecyclerView.Adapter<V> {
    private Class<V> cls;
    private List<T> data;

    public VHAdapter(Class<V> cls, List<T> data){
        this.cls = cls;
        this.data = data;
    }

    @NonNull
    @Override
    public V onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return VH.create(cls, LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull V holder, int position) {
        holder.setObject(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

}
