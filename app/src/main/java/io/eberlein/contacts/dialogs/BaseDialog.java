package io.eberlein.contacts.dialogs;

import android.app.AlertDialog;
import android.content.Context;

import io.realm.RealmObject;

public class BaseDialog<T extends RealmObject> {
    private Context context;
    private T object;
    private AlertDialog.Builder builder;

    public BaseDialog(Context context, T object, int layout){
        this.context = context;
        this.object = object;
        this.builder = new AlertDialog.Builder(context).setView(layout);
    }

    public void show(){

    }

    public Context getContext() {
        return context;
    }

    public T getObject() {
        return object;
    }
}
