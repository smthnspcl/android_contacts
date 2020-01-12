package io.eberlein.contacts.objects;

import io.realm.Realm;
import io.realm.RealmObject;

public class Note extends RealmObject {
    private String name;
    private String note;

    public String getName() {
        return name;
    }

    public String getNote() {
        return note;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void delete(){
        Realm r = getRealm();
        r.beginTransaction();
        deleteFromRealm();
        r.commitTransaction();
    }

    public static Note create(Realm realm){
        realm.beginTransaction();
        Note r = realm.createObject(Note.class);
        realm.commitTransaction();
        return r;
    }
}
