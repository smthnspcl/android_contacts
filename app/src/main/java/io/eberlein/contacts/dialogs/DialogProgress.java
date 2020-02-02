package io.eberlein.contacts.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.eberlein.contacts.R;

public class DialogProgress {
    @BindView(R.id.tv_text)
    TextView text;

    public AlertDialog show(Context ctx, String title, String msg){
        View v = LayoutInflater.from(ctx).inflate(R.layout.dialog_progress, null, false);
        ButterKnife.bind(this, v);
        text.setText(msg);
        return new AlertDialog.Builder(ctx).setTitle(title).setView(v).setCancelable(false).show();
    }
}
