package io.eberlein.contacts.objects;

import io.realm.Realm;

public class SyncTaskParameters {
    private Realm realm;
    
    public SyncTaskParameters(Realm realm){
        this.realm = realm;
    }

    public Realm getRealm() {
        return realm;
    }
}
