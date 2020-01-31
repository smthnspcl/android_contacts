package io.eberlein.contacts;

import io.eberlein.contacts.objects.Contact;
import io.eberlein.contacts.objects.Settings;
import io.realm.Realm;
import io.realm.RealmConfiguration;


public class Static {
    public static String fill64Bytes(String data){
        while(data.getBytes().length != 64){
            data += "6";
        }
        return data;
    }

    public static Realm getRealm(String password) {
        return getRealm(Realm.getDefaultInstance().where(Settings.class).findFirst(), password);
    }

    public static Realm getRealm(Settings settings, String password) {
        RealmConfiguration.Builder rcb = new RealmConfiguration.Builder();
        rcb.name("contacts");
        if (settings.isEncrypted()) {
            if (password != null && !password.isEmpty()) rcb.encryptionKey(password.getBytes());
            else return null;
        }
        RealmConfiguration rc = rcb.build();
        Realm.setDefaultConfiguration(rc);
        try {
            return Realm.getInstance(rc);
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }

    }

    public static void syncContact(Contact nc, Realm realm){
        Contact oc = realm.where(Contact.class).equalTo("uuid", nc.getUuid()).findFirst();
        if(oc != null) oc.sync(nc);
        else {
            realm.beginTransaction();
            realm.copyToRealm(nc);
            realm.commitTransaction();
        }
    }
}
