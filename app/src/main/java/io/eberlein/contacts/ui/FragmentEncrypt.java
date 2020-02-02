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

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.eberlein.contacts.R;
import io.eberlein.contacts.Static;
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

    @OnClick(R.id.btn_cancel)
    void btnCancelClicked(){
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.warning)
                .setMessage(R.string.message_data_will_not_be_encrypted)
                .setPositiveButton(R.string.i_understand, new DialogInterface.OnClickListener() {
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
                .setNegativeButton(R.string.let_me_enter_a_password, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    @OnClick(R.id.btn_encrypt)
    void btnEncryptClicked(){
        String password = this.password.getText().toString();
        if(password.isEmpty()) Toast.makeText(getContext(), R.string.no_password_entered, Toast.LENGTH_LONG).show();
        else if(password.length() > 64) Toast.makeText(getContext(), R.string.password_length_must_be_smaller_32, Toast.LENGTH_LONG).show();
        else {
            password = Static.fill64Bytes(password);
            realm.beginTransaction();
            settings.setEncrypted(true);
            settings.setFirstRun(false);
            realm.commitTransaction();
            EventBus.getDefault().post(new EventEncryptionDone(password));
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
