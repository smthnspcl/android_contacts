package io.eberlein.contacts.objects;


import io.realm.Realm;
import io.realm.RealmObject;

public class PhoneNumber extends RealmObject {
    private String countryCode;
    private String number;

    public String getNumber() {
        return number;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public static PhoneNumber create(Realm realm){
        realm.beginTransaction();
        PhoneNumber r = realm.createObject(PhoneNumber.class);
        realm.commitTransaction();
        return r;
    }
}
