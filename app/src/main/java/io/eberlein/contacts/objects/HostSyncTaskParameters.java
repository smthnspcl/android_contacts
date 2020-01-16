package io.eberlein.contacts.objects;

import android.net.wifi.p2p.WifiP2pManager;

import io.realm.Realm;

public class HostSyncTaskParameters extends SyncTaskParameters {
    private int port;
    private String serviceName;
    private WifiP2pManager p2pManager;
    private WifiP2pManager.Channel channel;
    private WifiP2pManager.ActionListener actionListener;

    public HostSyncTaskParameters(Realm realm, int port, String serviceName,
                                  WifiP2pManager p2pManager, WifiP2pManager.Channel channel,
                                  WifiP2pManager.ActionListener actionListener){
        super(realm);
        this.port = port;
        this.serviceName = serviceName;
        this.p2pManager = p2pManager;
        this.channel = channel;
        this.actionListener = actionListener;
    }

    public int getPort() {
        return port;
    }

    public String getServiceName() {
        return serviceName;
    }

    public WifiP2pManager getP2pManager() {
        return p2pManager;
    }

    public WifiP2pManager.Channel getChannel() {
        return channel;
    }

    public WifiP2pManager.ActionListener getActionListener() {
        return actionListener;
    }
}
