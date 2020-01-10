package io.eberlein.contacts.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.greenrobot.eventbus.EventBus;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.eberlein.contacts.R;
import io.eberlein.contacts.objects.Settings;
import io.eberlein.contacts.objects.events.EventEncryptionDone;
import io.realm.Realm;

public class FragmentEncrypt extends Fragment {
    private Realm realm;
    private Settings settings;

    @BindView(R.id.et_password) EditText password;

    public FragmentEncrypt(Settings settings){
        this.settings = settings;
        this.realm = settings.getRealm();
    }

    @OnClick(R.id.btn_generate)
    void btnGenerateClicked(){
        realm.beginTransaction();
        settings.setMasterKeyGenerated(true);
        realm.commitTransaction();
    }

    @OnClick(R.id.btn_cancel)
    void btnCancelClicked(){
        new AlertDialog.Builder(getActivity())
                .setTitle("warning!")
                .setMessage("the contact data you enter won't be protected.")
                .setPositiveButton("i understand", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        realm.beginTransaction();
                        settings.setEncrypted(false);
                        settings.setFirstRun(false);
                        realm.commitTransaction();
                        dialog.dismiss();
                        EventBus.getDefault().post(new EventEncryptionDone(null));
                    }
                })
                .setNegativeButton("let me enter a password", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    @OnClick(R.id.btn_encrypt)
    void btnEncryptClicked(){
        boolean done = false;
        String password = this.password.getText().toString();
        if(password.isEmpty()) Toast.makeText(getContext(), "no password specified", Toast.LENGTH_LONG).show();
        else {
            realm.beginTransaction();
            try {
                settings.setEncrypted(true);
                settings.setEncryptionKey(password);
                settings.setFirstRun(false);
                done = true;
            } catch (GeneralSecurityException | UnsupportedEncodingException e){
                e.printStackTrace();
                Toast.makeText(getContext(), "could not encrypt with password", Toast.LENGTH_LONG).show();
            }
            realm.commitTransaction();
            if(done) EventBus.getDefault().post(new EventEncryptionDone(password));
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_encrypt, container, false);
        ButterKnife.bind(this, v);
        return v;
    }
}
