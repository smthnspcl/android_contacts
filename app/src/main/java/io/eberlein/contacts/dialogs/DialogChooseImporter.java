package io.eberlein.contacts.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.eberlein.contacts.R;
import io.eberlein.contacts.Static;
import io.eberlein.contacts.objects.events.EventImporterSelected;

public class DialogChooseImporter {
    @BindView(R.id.btn_contact_app) Button contactApp;
    @BindView(R.id.btn_vcf) Button vcf;
    @BindView(R.id.btn_csv) Button csv;

    private AlertDialog.Builder builder;
    private AlertDialog dialog;

    private void importerSelected(int type){
        dialog.dismiss();
        EventBus.getDefault().post(new EventImporterSelected(type));
    }

    @OnClick(R.id.btn_contact_app)
    void onBtnContactAppClicked(){
        importerSelected(Static.IMPORTER_CONTACTS);
    }

    @OnClick(R.id.btn_vcf)
    void onBtnVcfClicked(){
        importerSelected(Static.IMPORTER_VCF);
    }

    @OnClick(R.id.btn_csv)
    void onBtnCsvClicked(){
        importerSelected(Static.IMPORTER_CSV);
    }

    public DialogChooseImporter(Context ctx){
        builder = new AlertDialog.Builder(ctx);
        builder.setTitle(R.string.import_);
        View v = LayoutInflater.from(ctx).inflate(R.layout.dialog_import, null, false);
        ButterKnife.bind(this, v);
        builder.setView(v);
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
    }

    public void show(){
        dialog = builder.show();
    }
}
