package io.eberlein.contacts.objects;

import io.realm.Realm;
import io.realm.RealmObject;

public class EmailAddress extends RealmObject {
    private String name;
    private String email;

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void delete(){
        Realm r = getRealm();
        r.beginTransaction();
        deleteFromRealm();
        r.commitTransaction();
    }

    public static EmailAddress create(Realm realm){
        realm.beginTransaction();
        EmailAddress r = realm.createObject(EmailAddress.class);
        realm.commitTransaction();
        return r;
    }
}
