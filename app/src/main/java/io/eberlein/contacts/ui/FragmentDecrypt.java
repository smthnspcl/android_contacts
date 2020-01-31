package io.eberlein.contacts.ui;

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
import io.eberlein.contacts.objects.events.EventEncryptionDone;

public class FragmentDecrypt extends Fragment {
    @BindView(R.id.et_password)
    EditText password;

    @OnClick(R.id.btn_decrypt)
    void onBtnDecryptClicked(){
        String password = this.password.getText().toString();
        if(password.isEmpty()) Toast.makeText(getContext(), R.string.no_password_entered, Toast.LENGTH_LONG).show();
        else {
            password = Static.fill64Bytes(password);
            EventBus.getDefault().post(new EventEncryptionDone(password));
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_decrypt, container, false);
        ButterKnife.bind(this, v);
        return v;
    }
}
