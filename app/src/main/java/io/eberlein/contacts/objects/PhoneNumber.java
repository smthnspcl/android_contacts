package io.eberlein.contacts.objects;


import io.realm.Realm;
import io.realm.RealmObject;

public class PhoneNumber extends RealmObject {
    private String name;
    private String countryCode;
    private String number;

    public String getNumber() {
        return number;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public String getName() {
        return name;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
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

    public static PhoneNumber create(Realm realm){
        realm.beginTransaction();
        PhoneNumber r = realm.createObject(PhoneNumber.class);
        realm.commitTransaction();
        return r;
    }
}
