package io.eberlein.contacts.objects;

import io.eberlein.contacts.interfaces.NamedObjectInterface;
import io.realm.Realm;
import io.realm.RealmObject;


public class Address extends RealmObject implements NamedObjectInterface<Address> {
    private String name;
    private String streetName;
    private String houseNumber;
    private String postalCode;
    private String city;
    private String notes;

    public String getStreetName() {
        return streetName;
    }

    public String getHouseNumber() {
        return houseNumber;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getCity() {
        return city;
    }

    public String getNotes() {
        return notes;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }

    public void setHouseNumber(String houseNumber) {
        this.houseNumber = houseNumber;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getAddressFine(){
        return streetName + " " + houseNumber;
    }

    public String getAddressCourse(){
        return postalCode + " " + city;
    }

    public String getName() {
        return name;
    }

    public void delete(){
        Realm r = getRealm();
        r.beginTransaction();
        deleteFromRealm();
        r.commitTransaction();
    }

    public static Address create(Realm realm){
        realm.beginTransaction();
        Address a = realm.createObject(Address.class);
        realm.commitTransaction();
        return a;
    }
}
