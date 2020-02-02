package io.eberlein.contacts.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import butterknife.BindView;
import butterknife.OnClick;
import io.eberlein.contacts.R;

public class DialogChooseImporter {
    @BindView(R.id.btn_contact_app) Button contactApp;
    @BindView(R.id.btn_vcf) Button vcf;
    @BindView(R.id.btn_csv) Button csv;

    private AlertDialog.Builder builder;
    private Context context;

    private void notImplemented(){
        Toast.makeText(context, "not implemented yet", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.btn_contact_app)
    void onBtnContactAppClicked(){
        notImplemented();
    }

    @OnClick(R.id.btn_vcf)
    void onBtnVcfClicked(){
        notImplemented();
    }

    @OnClick(R.id.btn_csv)
    void onBtnCsvClicked(){
        notImplemented();
    }

    public DialogChooseImporter(Context ctx){
        this.context = ctx;
        builder = new AlertDialog.Builder(ctx);
        builder.setTitle(R.string.import_);
        View v = LayoutInflater.from(ctx).inflate(R.layout.dialog_import, null, false);
        builder.setView(v);
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
    }

    public void show(){
        builder.show();
    }
}
