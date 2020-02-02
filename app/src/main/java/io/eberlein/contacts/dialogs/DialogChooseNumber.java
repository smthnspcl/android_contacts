package io.eberlein.contacts.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.PhoneUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.eberlein.contacts.R;
import io.eberlein.contacts.objects.Contact;
import io.eberlein.contacts.objects.PhoneNumber;


public class DialogChooseNumber extends DialogBase<Contact> {
    private boolean isCall = false;
    @BindView(R.id.rv_phone_numbers) RecyclerView recyclerView;

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_name) TextView name;
        @BindView(R.id.tv_number) TextView number;

        @OnClick
        void onClick(){
            if(isCall) PhoneUtils.dial(number.getText().toString());
            else {
                getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", number.getText().toString(), null)));
            }
        }

        ViewHolder(View v){
            super(v);
            ButterKnife.bind(this, v);
        }
    }

    public DialogChooseNumber(Context ctx, Contact contact, boolean isCall){ // else sms
        super(ctx, contact, R.layout.dialog_choose_number);
        this.isCall = isCall;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new RecyclerView.Adapter<ViewHolder>() {
            @Override
            public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
                PhoneNumber pn = getObject().getPhoneNumbers().get(position);
                holder.name.setText(pn.getName());
                holder.number.setText(pn.getNumber());
            }

            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new ViewHolder(LayoutInflater.from(getContext()).inflate(R.layout.item_choose_number, parent, false));
            }

            @Override
            public int getItemCount() {
                return contact.getPhoneNumbers().size();
            }
        });
    }

    @Override
    public void show() {
        builder.setTitle(getObject().getName());
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }
}
