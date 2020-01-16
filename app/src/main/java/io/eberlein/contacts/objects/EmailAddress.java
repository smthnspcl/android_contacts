package io.eberlein.contacts.objects;

import java.util.Date;
import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmObject;

public class EmailAddress extends RealmObject {
    private String uuid;
    private String name;
    private Date lastModifiedName;
    private String email;
    private Date lastModifiedEmail;

    public String getEmail() {
        return email;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setEmail(String email) {
        this.email = email;
        lastModifiedEmail = new Date();
    }

    public void setName(String name) {
        this.name = name;
        lastModifiedName = new Date();
    }

    public void delete(){
        Realm r = getRealm();
        r.beginTransaction();
        deleteFromRealm();
        r.commitTransaction();
    }

    public void sync(EmailAddress address){
        if(lastModifiedName.before(address.lastModifiedName)){
            name = address.name;
            lastModifiedName = address.lastModifiedName;
        }
        if(lastModifiedEmail.before(address.lastModifiedEmail)){
            email = address.email;
            lastModifiedEmail = address.lastModifiedEmail;
        }
    }

    public static EmailAddress create(Realm realm){
        realm.beginTransaction();
        EmailAddress r = realm.createObject(EmailAddress.class);
        r.setUuid(UUID.randomUUID().toString());
        realm.commitTransaction();
        return r;
    }
}
