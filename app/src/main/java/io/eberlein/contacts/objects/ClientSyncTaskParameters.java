package io.eberlein.contacts.objects;

import io.realm.Realm;

public class ClientSyncTaskParameters extends SyncTaskParameters {
    private String host;
    private int port;
    private ClientSyncConfiguration clientSyncConfiguration;

    public ClientSyncTaskParameters(Realm realm, String host, int port, ClientSyncConfiguration clientSyncConfiguration){
        super(realm);
        this.host = host;
        this.port = port;
        this.clientSyncConfiguration = clientSyncConfiguration;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public ClientSyncConfiguration getClientSyncConfiguration() {
        return clientSyncConfiguration;
    }
}
