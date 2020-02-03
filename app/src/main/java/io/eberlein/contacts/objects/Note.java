package io.eberlein.contacts.objects;

import java.util.Date;
import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmObject;

public class Note extends RealmObject {
    private String uuid;
    private String name;
    private Date lastNameModified;
    private String note;
    private Date lastNoteModified;

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }

    public Date getLastNameModified() {
        return lastNameModified;
    }

    public Date getLastNoteModified() {
        return lastNoteModified;
    }

    public String getName() {
        return name;
    }

    public String getNote() {
        return note;
    }

    public void setName(String name) {
        this.name = name;
        lastNameModified = new Date();
    }

    public void setNote(String note) {
        this.note = note;
        lastNoteModified = new Date();
    }

    public void delete(){
        Realm r = getRealm();
        r.beginTransaction();
        deleteFromRealm();
        r.commitTransaction();
    }

    public void sync(Note n){
        if(lastNameModified.before(n.lastNameModified)){
            name = n.getName();
            lastNameModified = n.getLastNameModified();
        }
        if(lastNoteModified.before(n.lastNoteModified)){
            note = n.getNote();
            lastNoteModified = n.lastNoteModified;
        }
    }

    public static Note create(Realm realm){
        realm.beginTransaction();
        Note r = realm.createObject(Note.class);
        r.setUuid(UUID.randomUUID().toString());
        realm.commitTransaction();
        return r;
    }

    public static Note findByNote(Realm realm, String note){
        return realm.where(Note.class).equalTo("note", note).findFirst();
    }

    public static Note convert(Realm realm, String note){
        Note n = findByNote(realm, note);
        if(n == null){
            n = Note.create(realm);
            realm.beginTransaction();
            n.setName("imported");
            n.setNote(note);
            realm.commitTransaction();
        }
        return n;
    }
}
