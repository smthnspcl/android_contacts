package io.eberlein.contacts;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;

import io.eberlein.contacts.objects.Contact;
import io.eberlein.contacts.objects.Settings;
import io.realm.Realm;
import io.realm.RealmConfiguration;


public class Static {
    public static Realm getRealm(String password) {
        Settings settings = Realm.getDefaultInstance().where(Settings.class).findFirst();
        return getRealm(settings, password);
    }

    public static Realm getRealm(Settings settings, String password){
        RealmConfiguration.Builder rcb = new RealmConfiguration.Builder();

        if(settings.isEncrypted()){
            if(password != null && !password.isEmpty()){
                try {
                    rcb.encryptionKey(settings.getEncryptionKey(password).getBytes());
                } catch (GeneralSecurityException | UnsupportedEncodingException e){
                    e.printStackTrace();
                    return null;
                }
            }
        }

        return Realm.getInstance(rcb.build());
    }

    public static void syncContact(Contact nc, Realm realm){
        Contact oc = realm.where(Contact.class).equalTo("uuid", nc.getUuid()).findFirst();
        if(oc != null) oc.sync(nc);
        else realm.copyToRealm(nc);
    }
}
