package io.eberlein.contacts.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.eberlein.contacts.R;
import io.eberlein.contacts.objects.Address;

public class AddressDialog {
    private Context context;
    private Address address;

    @BindView(R.id.et_name) EditText name;
    @BindView(R.id.et_street) EditText street;
    @BindView(R.id.et_house_nr) EditText houseNr;
    @BindView(R.id.et_postal_code) EditText postalCode;
    @BindView(R.id.et_city) EditText city;
    @BindView(R.id.et_notes) EditText notes;

    public AddressDialog(@NonNull Context context, @NonNull Address address){
        this.context = context;
        this.address = address;
    }

    public void build(){
        View v = LayoutInflater.from(context).inflate(R.layout.dialog_address, null, false);
        ButterKnife.bind(this, v);
        new AlertDialog.Builder(context)
                .setTitle("address")
                .setView(v)
                .setPositiveButton("save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        address.getRealm().beginTransaction();
                        address.setName(name.getText().toString());
                        address.setStreetName(street.getText().toString());
                        address.setHouseNumber(houseNr.getText().toString());
                        address.setPostalCode(postalCode.getText().toString());
                        address.setCity(city.getText().toString());
                        address.setNotes(notes.getText().toString());
                        address.getRealm().commitTransaction();
                        dialog.dismiss();
                    }
                }).setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }
}
