package io.eberlein.contacts.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import butterknife.ButterKnife;


interface DialogInterface {
    void show();
}

public abstract class DialogBase<T> implements DialogInterface {
    private Context context;
    private T object;
    AlertDialog.Builder builder;

    DialogBase(Context context, T object, int layout){
        this.context = context;
        this.object = object;
        View v = LayoutInflater.from(context).inflate(layout, null, false);
        ButterKnife.bind(this, v);
        this.builder = new AlertDialog.Builder(context).setView(v);
    }

    public Context getContext() {
        return context;
    }

    public T getObject() {
        return object;
    }
}
