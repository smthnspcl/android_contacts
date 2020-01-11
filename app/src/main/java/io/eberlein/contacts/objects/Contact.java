package io.eberlein.contacts.objects;

import java.util.Date;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;


public class Contact extends RealmObject{
    private boolean middleNameDisplayed;
    private String firstName;
    private String middleName;
    private String lastName;
    private Date createdDate;
    private Date lastModifiedDate;
    private String birthDate;
    private RealmList<Address> addresses;
    private RealmList<PhoneNumber> phoneNumbers;
    private RealmList<EmailAddress> emailAddresses;
    private RealmList<Note> notes;

    public String getFirstName() {
        return firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getName(){
        String r = lastName + ", " + firstName;
        if(middleNameDisplayed) r += " " + middleName;
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

    public RealmList<PhoneNumber> getPhoneNumbers() {
        return phoneNumbers;
    }

    public RealmList<EmailAddress> getEmailAddresses() {
        return emailAddresses;
    }

    public RealmList<Note> getNotes() {
        return notes;
    }

    public boolean isMiddleNameDisplayed() {
        return middleNameDisplayed;
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

    public void setPhoneNumbers(RealmList<PhoneNumber> phoneNumbers) {
        this.phoneNumbers = phoneNumbers;
    }

    public void setEmailAddresses(RealmList<EmailAddress> emailAddresses) {
        this.emailAddresses = emailAddresses;
    }

    public void setNotes(RealmList<Note> notes) {
        this.notes = notes;
    }

    public void setMiddleNameDisplayed(boolean middleNameDisplayed) {
        this.middleNameDisplayed = middleNameDisplayed;
    }

    public void setName(String name) {}

    public void delete() {
        Realm r = getRealm();
        r.beginTransaction();
        deleteFromRealm();
        r.commitTransaction();
    }

    public static Contact create(Realm realm){
        realm.beginTransaction();
        Contact r = realm.createObject(Contact.class);
        realm.commitTransaction();
        return r;
    }
}
