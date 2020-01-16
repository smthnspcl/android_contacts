package io.eberlein.contacts;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import io.eberlein.contacts.objects.Settings;
import io.realm.Realm;
import io.realm.RealmConfiguration;


public class Static {
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
}
