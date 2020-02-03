package io.eberlein.contacts.objects;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmObject;

public class PhoneNumber extends RealmObject {
    private String uuid;
    private String name;
    private Date lastNameModified;
    private String number;
    private Date lastNumberModified;

    public String getNumber() {
        return number;
    }

    public String getName() {
        return name;
    }

    public String getUuid() {
        return uuid;
    }

    public Date getLastNameModified() {
        return lastNameModified;
    }

    public Date getLastNumberModified() {
        return lastNumberModified;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setNumber(String number) {
        this.number = number;
        this.lastNumberModified = new Date();
    }

    public void setName(String name) {
        this.name = name;
        this.lastNameModified = new Date();
    }

    public void delete(){
        Realm r = getRealm();
        r.beginTransaction();
        deleteFromRealm();
        r.commitTransaction();
    }

    public void sync(PhoneNumber phoneNumber){
        if(lastNameModified.before(phoneNumber.lastNameModified)){
            name = phoneNumber.getName();
            lastNameModified = phoneNumber.lastNameModified;
        }
        if(lastNumberModified.before(phoneNumber.lastNumberModified)){
            number = phoneNumber.getNumber();
            lastNumberModified = phoneNumber.getLastNumberModified();
        }
    }

    public static PhoneNumber create(Realm realm){
        realm.beginTransaction();
        PhoneNumber r = realm.createObject(PhoneNumber.class);
        r.setUuid(UUID.randomUUID().toString());
        realm.commitTransaction();
        return r;
    }

    public static PhoneNumber findByNumber(Realm realm, String number){
        return realm.where(PhoneNumber.class).equalTo("number", number).findFirst();
    }

    public static PhoneNumber convert(Realm realm, com.github.tamir7.contacts.PhoneNumber phoneNumber){
        PhoneNumber p = findByNumber(realm, phoneNumber.getNumber());
        if(p == null){
            p = PhoneNumber.create(realm);
            realm.beginTransaction();
            p.setName(phoneNumber.getLabel());
            p.setNumber(phoneNumber.getNumber());
            realm.commitTransaction();
        }
        return p;
    }

    public static List<PhoneNumber> convert(Realm realm, List<com.github.tamir7.contacts.PhoneNumber> phoneNumbers){
        List<PhoneNumber> r = new ArrayList<>();
        for(com.github.tamir7.contacts.PhoneNumber p : phoneNumbers) r.add(convert(realm, p));
        return r;
    }
}
