package io.eberlein.contacts.objects;

import com.github.tamir7.contacts.Email;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmQuery;


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

    public static void sync(Realm realm, Contact nc){
        Contact oc = realm.where(Contact.class).equalTo("uuid", nc.getUuid()).findFirst();
        if(oc != null) oc.sync(nc);
        else {
            realm.beginTransaction();
            realm.copyToRealm(nc);
            realm.commitTransaction();
        }
    }

    public static void sync(Realm realm, List<Contact> nc){
        for(Contact c : nc) sync(realm, c);
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

    public static Contact findByName(Realm realm, String firstName, String middleName, String lastName){
        RealmQuery<Contact> rq = realm.where(Contact.class);
        if(firstName != null) rq.equalTo("firstName", firstName);
        if(middleName != null) rq.equalTo("middleName", middleName);
        if(lastName != null) rq.equalTo("lastName", lastName);
        return rq.findFirst();
    }

    public static Contact convert(Realm realm, com.github.tamir7.contacts.Contact contact){
        Contact c = findByNameOrCreate(realm, contact);
        List<Address> addresses = Address.convert(realm, contact.getAddresses());
        List<EmailAddress> emailAddresses = EmailAddress.convert(realm, contact.getEmails());
        List<PhoneNumber> phoneNumbers = PhoneNumber.convert(realm, contact.getPhoneNumbers());
        Note note = Note.convert(realm, contact.getNote());

        realm.beginTransaction();

        if(contact.getBirthday() != null) c.setBirthDate(contact.getBirthday().toString());
        for(Address a : addresses) c.getAddresses().add(a);
        for(EmailAddress e : emailAddresses) c.getEmailAddresses().add(e);
        for(PhoneNumber p : phoneNumbers) c.getPhoneNumbers().add(p);
        c.getNotes().add(note);

        realm.commitTransaction();
        return c;
    }

    public static Contact findByNameOrCreate(Realm realm, com.github.tamir7.contacts.Contact contact){
        Contact c = findByName(realm, contact.getGivenName(), null, contact.getFamilyName());
        if(c == null) {
            c = Contact.create(realm);
            realm.beginTransaction();
            c.setFirstName(contact.getGivenName());
            c.setLastName(contact.getFamilyName());
            realm.commitTransaction();
        }
        return c;
    }

    public static List<Contact> convert(Realm realm, List<com.github.tamir7.contacts.Contact> contacts){
        List<Contact> r = new ArrayList<>();
        for(com.github.tamir7.contacts.Contact c : contacts) r.add(convert(realm, c));
        return r;
    }
}
