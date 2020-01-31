package io.eberlein.contacts.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import io.eberlein.contacts.R;
import io.eberlein.contacts.Static;
import io.eberlein.contacts.objects.Contact;
import io.realm.Realm;

public class DialogSyncNonInteractive extends DialogBase<List<Contact>> {
    private Realm realm;
    private AlertDialog dialog;

    @BindView(R.id.tv_progress) TextView progress;
    @BindView(R.id.tv_progress_max) TextView progressMax;
    @BindView(R.id.btn_ok) Button ok;
    @BindView(R.id.progressBar) ProgressBar progressBar;

    public DialogSyncNonInteractive(Context context, List<Contact> contacts, Realm realm){
        super(context, contacts, R.layout.dialog_sync_noninteractive);
        this.realm = realm;
    }

    @OnClick(R.id.btn_ok)
    void onBtnOkClicked(){
        dialog.dismiss();
    }

    @Override
    public void show() {
        dialog = builder.setTitle(R.string.sync).setCancelable(false).show();
        int max = getObject().size();
        progressMax.setText(String.valueOf(max));
        for(int i=0; i<max; i++){
            progress.setText(String.valueOf(i + 1));
            Static.syncContact(getObject().get(i), realm);
        }
        progressBar.setVisibility(View.GONE);
        ok.setVisibility(View.VISIBLE);
    }
}
