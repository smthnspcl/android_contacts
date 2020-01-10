package io.eberlein.contacts.objects;

import java.util.Date;

import io.eberlein.contacts.interfaces.NamedObjectInterface;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;

public class Contact extends RealmObject implements NamedObjectInterface<Contact> {
    private String firstName;
    private String middleName;
    private String lastName;
    private Date createdDate;
    private Date lastModifiedDate;
    private String birthDate;
    private RealmList<Address> addresses;

    public Contact(){
        createdDate = new Date();
        lastModifiedDate = createdDate;
        addresses = new RealmList<>();
    }

    public String getFirstName() {
        return firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFullname(boolean withMiddleName){
        String r = lastName + ", " + firstName;
        if(withMiddleName) r += " " + middleName;
        return r;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public RealmList<Address> getAddresses() {
        return addresses;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public void setLastModifiedDate(Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public void setAddresses(RealmList<Address> addresses) {
        this.addresses = addresses;
    }

    public static Contact create(Realm r){
        r.beginTransaction();
        Contact c = r.createObject(Contact.class);
        r.commitTransaction();
        return c;
    }

    public String getName() {
        return getFullname(false);
    }

    @Override
    public void delete() {
        Realm r = getRealm();
        r.beginTransaction();
        deleteFromRealm();
        r.commitTransaction();
    }
}
