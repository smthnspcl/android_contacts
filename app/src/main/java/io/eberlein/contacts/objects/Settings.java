package io.eberlein.contacts.objects;


import io.realm.Realm;
import io.realm.RealmObject;


public class Settings extends RealmObject {
    private boolean firstRun;
    private boolean encrypted;

    public Settings(){
        firstRun = true;
        encrypted = false;
    }

    public boolean isEncrypted() {
        return encrypted;
    }

    public void setEncrypted(boolean encrypted) {
        this.encrypted = encrypted;
    }

    public boolean isFirstRun() {
        return firstRun;
    }

    public void setFirstRun(boolean firstRun) {
        this.firstRun = firstRun;
    }

    public static Settings get(){
        return Realm.getDefaultInstance().where(Settings.class).findFirst();
    }

    public static Settings create(){
        Realm r = Realm.getDefaultInstance();
        r.beginTransaction();
        Settings _r = r.createObject(Settings.class);
        r.commitTransaction();
        return _r;
    }
}
