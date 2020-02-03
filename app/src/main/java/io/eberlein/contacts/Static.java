package io.eberlein.contacts;

import java.util.Date;

import io.eberlein.contacts.objects.Settings;
import io.realm.DynamicRealm;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;


class Migration implements RealmMigration {
    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
        RealmSchema s = realm.getSchema();

        if(oldVersion == 0){
            oldVersion++;
        }

        if(oldVersion == 1){
            RealmObjectSchema a = s.create("Address");
            a.addField("country", String.class);
            a.addField("lastModifiedCountry", Date.class);
            a.addField("region", String.class);
            a.addField("lastModifiedRegion", Date.class);
        }
    }
}


public class Static {
    private static final int DB_VERSION = 1;

    public static final int IMPORTER_CONTACTS = 0;
    public static final int IMPORTER_VCF = 1;
    public static final int IMPORTER_CSV = 2;

    public static boolean PERMISSION_CONTACTS = false;
    public static boolean PERMISSION_BLUETOOTH = false;
    public static boolean PERMISSION_LOCATION = false;

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
        rcb.schemaVersion(DB_VERSION);
        rcb.migration(new Migration());
        if (settings.isEncrypted()) {
            if (password != null && !password.isEmpty()) rcb.encryptionKey(password.getBytes());
            else return null;
        }
        RealmConfiguration rc = rcb.build();
        try {
            return Realm.getInstance(rc);
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }

    }
}
