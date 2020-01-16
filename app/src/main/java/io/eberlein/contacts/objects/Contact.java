package io.eberlein.contacts.objects;

import java.util.Date;
import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;


public class Contact extends RealmObject{
    private String uuid;
    private boolean middleNameDisplayed;
    private Date lastModifiedFirstName;
    private String firstName;
    private Date lastModifiedMiddleName;
    private String middleName;
    private Date lastModifiedLastName;
    private String lastName;
    private Date createdDate;
    private Date lastModifiedBirthDate;
    private String birthDate;
    private RealmList<Address> addresses;
    private RealmList<PhoneNumber> phoneNumbers;
    private RealmList<EmailAddress> emailAddresses;
    private RealmList<Note> notes;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
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

    public String getName(){
        String r = lastName + ", " + firstName;
        if(middleNameDisplayed) r += " " + middleName;
        return r;
    }

    public Date getCreatedDate() {
        return createdDate;
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

    public Date getLastModifiedFirstName() {
        return lastModifiedFirstName;
    }

    public Date getLastModifiedMiddleName() {
        return lastModifiedMiddleName;
    }

    public Date getLastModifiedLastName() {
        return lastModifiedLastName;
    }

    public Date getLastModifiedBirthDate() {
        return lastModifiedBirthDate;
    }

    public boolean isMiddleNameDisplayed() {
        return middleNameDisplayed;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
        this.lastModifiedFirstName = new Date();
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
        this.lastModifiedMiddleName = new Date();
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
        this.lastModifiedLastName = new Date();
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
        this.lastModifiedBirthDate = new Date();
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
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

    public void sync(Contact contact){
        if(lastModifiedFirstName.before(contact.getLastModifiedFirstName())){
            firstName = contact.getFirstName();
            lastModifiedFirstName = contact.getLastModifiedFirstName();
        }
        if(lastModifiedMiddleName.before(contact.getLastModifiedMiddleName())){
            middleName = contact.getMiddleName();
            lastModifiedMiddleName = contact.getLastModifiedMiddleName();
        }
        if(lastModifiedLastName.before(contact.getLastModifiedLastName())){
            lastName = contact.getLastName();
            lastModifiedLastName = contact.getLastModifiedLastName();
        }
        if(lastModifiedBirthDate.before(contact.getLastModifiedBirthDate())){
            birthDate = contact.getBirthDate();
            lastModifiedBirthDate = contact.getLastModifiedBirthDate();
        }

        for(Address a : contact.getAddresses()){
            boolean foundAddress = false;
            for(Address oa : addresses) {
                if(a.getUuid().equals(oa.getUuid())) {
                    oa.sync(a);
                    foundAddress = true;
                    break;
                }
            }
            if(!foundAddress) addresses.add(a);
        }

        for(EmailAddress a : contact.getEmailAddresses()){
            boolean foundAddress = false;
            for(EmailAddress ea : emailAddresses){
                if(a.getUuid().equals(ea.getUuid())){
                    ea.sync(a);
                    foundAddress = true;
                    break;
                }
            }
            if(!foundAddress) emailAddresses.add(a);
        }

        for(PhoneNumber p : contact.getPhoneNumbers()){
            boolean foundNumber = false;
            for(PhoneNumber op : phoneNumbers){
                if(p.getUuid().equals(op.getUuid())){
                    op.sync(p);
                    foundNumber = true;
                    break;
                }
            }
            if(!foundNumber) phoneNumbers.add(p);
        }

        for(Note n : contact.getNotes()){
            boolean foundNote = false;
            for(Note on : notes){
                if(n.getUuid().equals(on.getUuid())){
                    on.sync(n);
                    foundNote = true;
                    break;
                }
            }
            if(!foundNote) notes.add(n);
        }
    }

    public static Contact create(Realm realm){
        realm.beginTransaction();
        Contact r = realm.createObject(Contact.class);
        r.setUuid(UUID.randomUUID().toString());
        realm.commitTransaction();
        return r;
    }
}
