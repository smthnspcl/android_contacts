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
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import io.eberlein.contacts.R;
import io.eberlein.contacts.adapters.VHAdapter;
import io.eberlein.contacts.dialogs.SyncDialog;
import io.eberlein.contacts.objects.Contact;
import io.eberlein.contacts.objects.SyncConfiguration;
import io.eberlein.contacts.objects.events.EventSelectedWifiP2pDevice;
import io.eberlein.contacts.objects.events.EventSync;
import io.eberlein.contacts.viewholders.VHWifiP2pDevice;
import io.realm.Realm;
import io.realm.RealmConfiguration;

import static android.net.wifi.p2p.WifiP2pManager.P2P_UNSUPPORTED;


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
    private Realm realm;
    private static final String LOG_TAG = "SyncActivity";
    private boolean isScanning = false;
    private boolean isSyncing = false;
    private WifiP2pManager p2pManager;
    private WifiP2pManager.Channel channel;
    private IntentFilter p2pIntentFilter = new IntentFilter();
    private BroadcastReceiver wifiP2pBroadcastReceiver;
    private VHAdapter adapter;
    private List<WifiP2pDevice> p2pDevices;
    private ServerSocket serverSocket;
    private SyncConfiguration syncConfiguration;
    private WifiP2pInfo wifiP2pInfo;

    @BindView(R.id.rv_remote_devices)
    RecyclerView recyclerRemoteDevices;

    @BindView(R.id.btn_search_devices)
    FloatingActionButton searchBtn;

    @BindView(R.id.cb_is_host)
    CheckBox isHost;

    @BindView(R.id.pb_search)
    ProgressBar progressBar;

    @OnCheckedChanged(R.id.cb_is_host)
    void onCheckedChangedIsHost(){
        if(isHost.isChecked()) {
            searchBtn.hide();
            createServerSocket();
        } else {
            searchBtn.show();
            destroyServerSocket();
        }
    }

    private WifiP2pManager.ActionListener addLocalServiceListener = new WifiP2pManager.ActionListener() {
        @Override
        public void onSuccess() {

        }

        @Override
        public void onFailure(int reason) {
            if(reason == P2P_UNSUPPORTED) Toast.makeText(getApplicationContext(), "p2p is not supported on this device", Toast.LENGTH_SHORT).show();
        }
    };

    private void destroyServerSocket(){
        p2pManager.clearLocalServices(channel, addLocalServiceListener);
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private void createServerSocket(){
        try {
            serverSocket = new ServerSocket(4337); // 0 todo
        } catch (IOException e){
            e.printStackTrace();
            Toast.makeText(this, "could not open server port", Toast.LENGTH_SHORT).show();
        }
        Map<String, String> serviceRecord = new HashMap<>();
        serviceRecord.put("listenport", String.valueOf(serverSocket.getLocalPort()));
        serviceRecord.put("name", getHostName("contactSyncHost"));
        WifiP2pDnsSdServiceInfo serviceInfo = WifiP2pDnsSdServiceInfo.newInstance("_contacts", "_presence._tcp", serviceRecord);
        p2pManager.addLocalService(channel, serviceInfo, addLocalServiceListener);
    }

    @OnClick(R.id.btn_search_devices)
    void onBtnSearchDevicesClicked(){
        if(!isScanning) {
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(42);
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
                    progressBar.setVisibility(View.GONE);
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
            progressBar.setVisibility(View.GONE);
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

    private void tryWriteOutputStream(OutputStream os, byte[] data){
        try {
            os.write(data);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    // NetworkOnMainThreadException
    private void doSync(OutputStream os, InputStream is){
        tryWriteOutputStream(os, "START".getBytes(StandardCharsets.UTF_8));
        for(Contact c : realm.where(Contact.class).findAll()){
            Log.d(LOG_TAG, "syncing: " + c.getName());
            tryWriteOutputStream(os, JSON.toJSONString(c).getBytes(StandardCharsets.UTF_8));
        }
        tryWriteOutputStream(os, "END".getBytes(StandardCharsets.UTF_8));
        byte[] in = new byte[4096];
        try {
            while (is.read(in) != -1){
                String strData = new String(in);
                if(strData.equals("END")) break;
                if(strData.equals("START")) continue;
                Contact c = JSON.parseObject(strData, Contact.class);
                Contact ec = realm.where(Contact.class).equalTo("uuid", c.getUuid()).findFirst();
                Log.d(LOG_TAG, "doSync: remote: " + c.getName() + " : " + c.getLastModifiedDate());
                Log.d(LOG_TAG, "doSync: local:  " + (ec != null));
                if(ec == null) realm.copyToRealm(c);
                else if(ec.getLastModifiedDate().before(c.getLastModifiedDate())) realm.copyToRealmOrUpdate(c);
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private void syncWithDevice(){
        if(syncConfiguration == null){
            Log.wtf(LOG_TAG, "syncWithDevice:syncConfiguration == null");
            return;
        }
        isSyncing = true;
        String address = wifiP2pInfo.groupOwnerAddress.getHostAddress(); // gethostname?
        // todo get port via settings
        int PORT = 4337;
        Log.d(LOG_TAG, "creating socketaddress for " + address + ":" + PORT);
        SocketAddress e = new InetSocketAddress(address, PORT);
        Socket s = new Socket();
        try {
            s.connect(e);
            Toast.makeText(this, "connected to " + address + ":" + PORT, Toast.LENGTH_SHORT).show();
            doSync(s.getOutputStream(), s.getInputStream());
            s.close();
        } catch (IOException ex){
            ex.printStackTrace();
            Toast.makeText(this, "could not connect to " + address + ":" + PORT, Toast.LENGTH_LONG).show();
        }
        isSyncing = false;
    }

    private WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo info) {
           wifiP2pInfo = info;
           if(!isSyncing) syncWithDevice();
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
                // thisDevice = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
            }
        }
    }

    private void initDB(){
        Intent i = getIntent();
        RealmConfiguration.Builder rcb = new RealmConfiguration.Builder();
        if(i.hasExtra("encryptionKey")) {
            byte[] key = getIntent().getByteArrayExtra("encryptionKey");
            if(key != null) rcb.encryptionKey(key);
        }
        realm = Realm.getInstance(rcb.build());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync);

        initDB();

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

        syncConfiguration = cfg;

        WifiP2pConfig c = new WifiP2pConfig();
        c.deviceAddress = cfg.getDevice().deviceAddress;
        c.wps.setup = WpsInfo.PBC;
        p2pManager.connect(channel, c, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(LOG_TAG, "connected to device");
                if(wifiP2pInfo != null && !isSyncing) syncWithDevice();
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

    // https://stackoverflow.com/questions/21898456/get-android-wifi-net-hostname-from-code
    public static String getHostName(String defValue) {
        try {
            Method getString = Build.class.getDeclaredMethod("getString", String.class);
            getString.setAccessible(true);
            return getString.invoke(null, "net.hostname").toString();
        } catch (Exception ex) {
            return defValue;
        }
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
