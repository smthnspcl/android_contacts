package io.eberlein.contacts.objects;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Set;

import io.eberlein.contacts.AES;
import io.realm.Realm;
import io.realm.RealmObject;

public class Settings extends RealmObject {
    private boolean firstRun;
    private boolean encrypted;
    private boolean masterKeyGenerated;
    private String encryptionKey;
    private String encryptionSalt;


    public Settings(){
        firstRun = true;
        encrypted = false;
        masterKeyGenerated = false;
    }

    public boolean isEncrypted() {
        return encrypted;
    }

    public void setEncrypted(boolean encrypted) {
        this.encrypted = encrypted;
    }

    public String getEncryptionKey(String password) throws GeneralSecurityException, UnsupportedEncodingException {
        return AES.decryptString(new AES.CipherTextIvMac(encryptionKey), AES.generateKeyFromPassword(password, encryptionSalt));
    }

    public boolean isFirstRun() {
        return firstRun;
    }

    public void setFirstRun(boolean firstRun) {
        this.firstRun = firstRun;
    }

    public boolean isMasterKeyGenerated() {
        return masterKeyGenerated;
    }

    public void setMasterKeyGenerated(boolean masterKeyGenerated) {
        this.masterKeyGenerated = masterKeyGenerated;
    }

    public void setEncryptionKey(String password) throws GeneralSecurityException, UnsupportedEncodingException {
        byte[] tmp = new byte[4096];
        new SecureRandom().nextBytes(tmp);
        getRealm().beginTransaction();
        encryptionSalt = AES.saltString(AES.generateSalt());
        encryptionKey = new String(AES.encrypt(encryptionKey, AES.generateKeyFromPassword(password, encryptionSalt)).getCipherText());
        getRealm().commitTransaction();
    }

    public static Settings get(){
        return Realm.getDefaultInstance().where(Settings.class).findFirst();
    }

    public static Settings create(){
        Realm r = Realm.getDefaultInstance();
        r.beginTransaction();
        Settings _r = r.createObject(Settings.class);
        r.commitTransaction();
        return _r;
    }
}
