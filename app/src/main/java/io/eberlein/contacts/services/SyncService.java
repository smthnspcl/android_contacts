package io.eberlein.contacts.services;

import android.app.IntentService;
import android.content.Intent;

import androidx.annotation.Nullable;

import io.realm.Realm;

public class SyncService extends IntentService {
    public static final String EXTRA_REMOTE_HOST = "remote_host";
    public static final String EXTRA_LOCAL_PORT = "local_port";
    public static final String EXTRA_REMOTE_PORT = "remote_port";
    public static final String EXTRA_REALM_ENCRYPTION_KEY = "realm_encryption_key";
    public static final String EXTRA_ENCRYPTION_KEY = "encryption_key";

    private static final String LOG_TAG = "SyncService";
    private Realm realm;

    public SyncService(){
        super(LOG_TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

    }
}
