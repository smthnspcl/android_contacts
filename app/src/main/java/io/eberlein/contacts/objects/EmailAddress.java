package io.eberlein.contacts.objects;

import com.github.tamir7.contacts.Email;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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

    public static EmailAddress findByEmail(Realm realm, String email){
        return realm.where(EmailAddress.class).equalTo("email", email).findFirst();
    }

    public static EmailAddress convert(Realm realm, Email email){
        EmailAddress r = findByEmail(realm, email.getAddress());
        if(r == null) {
            r = EmailAddress.create(realm);
            realm.beginTransaction();
            r.setEmail(email.getAddress());
            r.setName(email.getLabel());
            realm.commitTransaction();
        }
        return r;
    }

    public static List<EmailAddress> convert(Realm realm, List<Email> emails){
        List<EmailAddress> r = new ArrayList<>();
        for(Email e : emails) r.add(convert(realm, e));
        return r;
    }
}
