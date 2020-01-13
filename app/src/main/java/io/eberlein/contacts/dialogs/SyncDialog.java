package io.eberlein.contacts.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.p2p.WifiP2pDevice;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import io.eberlein.contacts.R;
import io.eberlein.contacts.objects.SyncConfiguration;
import io.eberlein.contacts.objects.events.EventSync;


public class SyncDialog extends BaseDialog<WifiP2pDevice> {
    @BindView(R.id.cb_encrypt) CheckBox encrypt;
    @BindView(R.id.cb_interactive) CheckBox interactive;
    @BindView(R.id.et_password) EditText password;

    public SyncDialog(Context context, WifiP2pDevice wifiP2pDevice){
        super(context, wifiP2pDevice, R.layout.dialog_sync);
    }

    @Override
    public void show() {
        builder.setTitle(getObject().deviceName)
                .setPositiveButton(R.string.sync, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(encrypt.isChecked() && password.getText().toString().isEmpty()) {
                            Toast.makeText(getContext(), "cant encrypt without a password", Toast.LENGTH_LONG).show();
                        }
                        if(encrypt.isChecked() && !password.getText().toString().isEmpty() || !encrypt.isChecked()) {
                            SyncConfiguration c = new SyncConfiguration();
                            c.setDevice(getObject());
                            c.setEncrypted(encrypt.isChecked());
                            c.setInteractive(interactive.isChecked());
                            EventBus.getDefault().post(new EventSync(c));
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
