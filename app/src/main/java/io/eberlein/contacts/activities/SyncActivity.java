package io.eberlein.contacts.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.eberlein.contacts.R;
import io.eberlein.contacts.adapters.VHAdapter;
import io.eberlein.contacts.dialogs.SyncDialog;
import io.eberlein.contacts.objects.SyncConfiguration;
import io.eberlein.contacts.objects.events.EventSelectedWifiP2pDevice;
import io.eberlein.contacts.objects.events.EventSync;
import io.eberlein.contacts.viewholders.VHWifiP2pDevice;


// todo find out if device is capable of wifi direct
// action listener P2PUNSUPPORTED CODE
// todo indicate if scanning with progress circle
/*
manager.requestGroupInfo(channel, new GroupInfoListener() {
  @Override
  public void onGroupInfoAvailable(WifiP2pGroup group) {
      String groupPassword = group.getPassphrase();
  }
});
 */

public class SyncActivity extends AppCompatActivity {
    private static final String LOG_TAG = "SyncActivity";
    private boolean isScanning = false;
    private boolean isSyncing = false;
    private WifiP2pManager p2pManager;
    private WifiP2pManager.Channel channel;
    private IntentFilter p2pIntentFilter = new IntentFilter();
    private BroadcastReceiver wifiP2pBroadcastReceiver;
    private VHAdapter adapter;
    private List<WifiP2pDevice> p2pDevices;
    private WifiP2pDevice thisDevice;

    @BindView(R.id.rv_remote_devices)
    RecyclerView recyclerRemoteDevices;

    @OnClick(R.id.btn_search_devices)
    void onBtnSearchDevicesClicked(){
        if(!isScanning) {
            isScanning = true;
            p2pManager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Toast.makeText(getApplicationContext(), "initiated scan", Toast.LENGTH_SHORT).show();
                    Log.d(LOG_TAG, "p2pManager.discoverPeers:onSuccess");
                }

                @Override
                public void onFailure(int reason) {
                    Toast.makeText(getApplicationContext(), "failed to scan", Toast.LENGTH_SHORT).show();
                    Log.w(LOG_TAG, "p2pManager.discoverPeers:onFailure");
                }
            });
        } else {
            Toast.makeText(getApplicationContext(), "already scanning", Toast.LENGTH_SHORT).show();
        }
    }

    private WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peers) {
            isScanning = false;
            Collection<WifiP2pDevice> rPeers = peers.getDeviceList();
            if(rPeers.size() == 0) {
                Log.d(LOG_TAG, "no devices found");
                Toast.makeText(getApplicationContext(), "no devices found", Toast.LENGTH_SHORT).show();
                return;
            }
            p2pDevices.clear();
            p2pDevices.addAll(rPeers);
            adapter.notifyDataSetChanged();
        }
    };

    private WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo info) {
            String groupOwnerAddress = info.groupOwnerAddress.getHostAddress();
            if(isSyncing){
                // todo connect, handshake (check password) / dialog on other site with password

            }
        }
    };

    private class WiFiP2PBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)){
                int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                if(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                    // wifi p2p enabled
                } else {
                    // not
                }
            } else if(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)){
                if(p2pManager != null) p2pManager.requestPeers(channel, peerListListener);
            } else if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)){
                if(p2pManager == null) return;
                NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                if(networkInfo.isConnected()) p2pManager.requestConnectionInfo(channel, connectionInfoListener);
            } else if(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)){
                thisDevice = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
            }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync);

        ButterKnife.bind(this);
        recyclerRemoteDevices.setLayoutManager(new LinearLayoutManager(this));
        p2pDevices = new ArrayList<>();
        adapter = new VHAdapter<>(VHWifiP2pDevice.class, p2pDevices);
        recyclerRemoteDevices.setAdapter(adapter);

        p2pIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        p2pIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        p2pIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        p2pIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        this.p2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        this.channel = p2pManager.initialize(this, getMainLooper(), null);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventSync(EventSync e){
        SyncConfiguration cfg = e.getObject();
        if(cfg.isInvalid()) {
            Toast.makeText(getApplicationContext(), "sync configuration invalid", Toast.LENGTH_LONG).show();
            return;
        }

        WifiP2pConfig c = new WifiP2pConfig();
        c.deviceAddress = cfg.getDevice().deviceAddress;
        c.wps.setup = WpsInfo.PBC;
        p2pManager.connect(channel, c, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                isSyncing = true;
                Log.d(LOG_TAG, "connected to device");
            }

            @Override
            public void onFailure(int reason) {
                Log.w(LOG_TAG, "failed to connect to device");
                Toast.makeText(getApplicationContext(), "could not connect to device", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventSelectedWifiP2pDevice(EventSelectedWifiP2pDevice e){
        new SyncDialog(this, e.getObject()).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        wifiP2pBroadcastReceiver = new WiFiP2PBroadcastReceiver();
        registerReceiver(wifiP2pBroadcastReceiver, p2pIntentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(wifiP2pBroadcastReceiver);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }
}
