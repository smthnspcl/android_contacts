package io.eberlein.contacts.dialogs;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import io.eberlein.contacts.R;
import io.eberlein.contacts.objects.ClientSyncConfiguration;
import io.eberlein.contacts.objects.events.EventClientSync;


public class DialogBaseSyncConfiguration extends DialogBase<BluetoothDevice> {
    @BindView(R.id.cb_encrypt) CheckBox encrypt;
    @BindView(R.id.cb_interactive) CheckBox interactive;
    @BindView(R.id.et_password) EditText password;

    public DialogBaseSyncConfiguration(Context context, BluetoothDevice device){
        super(context, device, R.layout.dialog_sync_configuration);
    }

    @Override
    public void show() {
        builder.setTitle(getObject().getName())
                .setPositiveButton(R.string.sync, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(encrypt.isChecked() && password.getText().toString().isEmpty()) {
                            Toast.makeText(getContext(), "cant encrypt without a password", Toast.LENGTH_LONG).show();
                        }
                        if(encrypt.isChecked() && !password.getText().toString().isEmpty() || !encrypt.isChecked()) {
                            ClientSyncConfiguration c = new ClientSyncConfiguration(
                                    getObject(),
                                    interactive.isChecked(),
                                    password.getText().toString()
                            );
                            EventBus.getDefault().post(new EventClientSync(c));
                            dialog.dismiss();
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }
}
