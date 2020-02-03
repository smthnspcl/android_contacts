package io.eberlein.contacts.objects;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmObject;


public class Address extends RealmObject {
    private String uuid;
    private String name;
    private Date lastModifiedName;
    private String streetName;
    private Date lastModifiedStreetName;
    private String houseNumber;
    private Date lastModifiedHouseNumber;
    private String postalCode;
    private Date lastModifiedPostalCode;
    private String city;
    private Date lastModifiedCity;
    private String country;
    private Date lastModifiedCountry;
    private String region;
    private Date lastModifiedRegion;
    private String notes;
    private Date lastModifiedNotes;


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

    public void setStreetName(String streetName) {
        this.streetName = streetName;
        this.lastModifiedStreetName = new Date();
    }

    public void setHouseNumber(String houseNumber) {
        this.houseNumber = houseNumber;
        this.lastModifiedHouseNumber = new Date();
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
        this.lastModifiedPostalCode = new Date();
    }

    public void setCity(String city) {
        this.city = city;
        this.lastModifiedCity = new Date();
    }

    public void setNotes(String notes) {
        this.notes = notes;
        this.lastModifiedNotes = new Date();
    }

    public void setName(String name) {
        this.name = name;
        this.lastModifiedName = new Date();
    }

    public void setCountry(String country) {
        this.country = country;
        this.lastModifiedCountry = new Date();
    }

    public void setRegion(String region) {
        this.region = region;
        this.lastModifiedRegion = new Date();
    }

    public String getCountry() {
        return country;
    }

    public String getRegion() {
        return region;
    }

    public String getUuid() {
        return uuid;
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

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Date getLastModifiedCity() {
        return lastModifiedCity;
    }

    public Date getLastModifiedHouseNumber() {
        return lastModifiedHouseNumber;
    }

    public Date getLastModifiedName() {
        return lastModifiedName;
    }

    public Date getLastModifiedNotes() {
        return lastModifiedNotes;
    }

    public Date getLastModifiedPostalCode() {
        return lastModifiedPostalCode;
    }

    public Date getLastModifiedStreetName() {
        return lastModifiedStreetName;
    }

    public Date getLastModifiedCountry() {
        return lastModifiedCountry;
    }

    public Date getLastModifiedRegion() {
        return lastModifiedRegion;
    }

    public void delete() {
        Realm r = getRealm();
        r.beginTransaction();
        deleteFromRealm();
        r.commitTransaction();
    }

    public void sync(Address address){
        if(lastModifiedName.before(address.lastModifiedName)){
            name = address.getName();
            lastModifiedName = address.getLastModifiedName();
        }
        if(lastModifiedStreetName.before(address.lastModifiedStreetName)){
            streetName = address.getStreetName();
            lastModifiedStreetName = address.getLastModifiedStreetName();
        }
        if(lastModifiedHouseNumber.before(address.getLastModifiedHouseNumber())){
            houseNumber = address.getHouseNumber();
            lastModifiedHouseNumber = address.getLastModifiedHouseNumber();
        }
        if(lastModifiedCity.before(address.getLastModifiedCity())){
            city = address.getCity();
            lastModifiedCity = address.getLastModifiedCity();
        }
        if(lastModifiedPostalCode.before(address.getLastModifiedPostalCode())){
            postalCode = address.getPostalCode();
            lastModifiedPostalCode = address.getLastModifiedPostalCode();
        }
        if(lastModifiedNotes.before(address.getLastModifiedNotes())){
            notes = address.getNotes();
            lastModifiedNotes = address.getLastModifiedNotes();
        }
        if(lastModifiedCountry.before(address.getLastModifiedCountry())){
            country = address.getCountry();
            lastModifiedCountry = address.getLastModifiedCountry();
        }
        if(lastModifiedRegion.before(address.getLastModifiedRegion())){
            region = address.getRegion();
            lastModifiedRegion = address.getLastModifiedRegion();
        }
    }

    public static Address create(Realm realm){
        realm.beginTransaction();
        Address r = realm.createObject(Address.class);
        r.setUuid(UUID.randomUUID().toString());
        realm.commitTransaction();
        return r;
    }

    public static List<Address> convert(Realm realm, List<com.github.tamir7.contacts.Address> addresses){
        List<Address> r = new ArrayList<>();
        for(com.github.tamir7.contacts.Address a : addresses) r.add(convert(realm, a));
        return r;
    }

    public static Address convert(Realm realm, com.github.tamir7.contacts.Address address){
        Address na = Address.create(realm);
        realm.beginTransaction();
        na.setName(address.getLabel());
        na.setCity(address.getCity());
        na.setStreetName(address.getStreet());
        na.setPostalCode(address.getPostcode());
        na.setCountry(address.getCountry());
        na.setRegion(address.getRegion());
        realm.commitTransaction();
        return na;
    }
}
