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
import android.os.AsyncTask;
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
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.AsyncHttpPost;
import com.koushikdutta.async.http.AsyncHttpRequest;
import com.koushikdutta.async.http.AsyncHttpResponse;
import com.koushikdutta.async.http.body.AsyncHttpRequestBody;
import com.koushikdutta.async.http.body.JSONObjectBody;
import com.koushikdutta.async.http.callback.HttpConnectCallback;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.koushikdutta.async.http.server.HttpServerRequestCallback;
import com.koushikdutta.ion.Ion;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import io.eberlein.contacts.R;
import io.eberlein.contacts.adapters.VHAdapter;
import io.eberlein.contacts.dialogs.SyncDialog;
import io.eberlein.contacts.objects.ClientSyncConfiguration;
import io.eberlein.contacts.objects.Contact;
import io.eberlein.contacts.objects.events.EventSelectedWifiP2pDevice;
import io.eberlein.contacts.objects.events.EventClientSync;
import io.eberlein.contacts.viewholders.VHWifiP2pDevice;
import io.realm.Realm;
import io.realm.RealmConfiguration;



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
    private Realm realm;
    private int hostPort;
    private String serviceName;

    private AsyncHttpServer server;

    private ClientSyncConfiguration clientSyncConfiguration;

    private boolean isScanning = false;
    private boolean isSyncing = false;

    private WifiP2pManager p2pManager;
    private WifiP2pManager.Channel channel;
    private IntentFilter p2pIntentFilter = new IntentFilter();
    private VHAdapter adapter;
    private List<WifiP2pDevice> p2pDevices;

    private BroadcastReceiver wifiP2pBroadcastReceiver;
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
            createServer();

        } else {
            searchBtn.show();
            server.stop();
        }
    }

    private void createServer(){
        server = new AsyncHttpServer();
        server.post("/contact", new HttpServerRequestCallback() {
            @Override
            public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
                if(request.getBody() instanceof JSONObjectBody){
                    JSONObjectBody body = (JSONObjectBody) request.getBody();
                    JSONObject obj = body.get();
                    Log.d(LOG_TAG, obj.toString()); // todo
                }
            }
        });
        server.listen(hostPort);
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

    private void startClientSync(){
        if(clientSyncConfiguration == null){
            Log.wtf(LOG_TAG, "syncWithDevice:syncConfiguration == null");
            return;
        }
        isSyncing = true;
        for(Contact c : realm.where(Contact.class).findAll()) {
            Ion.with(this)
                    .load("")
                    .setJsonPojoBody(c)
                    .asString()
                    .setCallback(new FutureCallback<String>() {
                        @Override
                        public void onCompleted(Exception e, String result) {
                            Contact nc = JSON.parseObject(result, Contact.class);
                            if(clientSyncConfiguration.isInteractive()){
                                // todo show difference to user
                            } else c.sync(nc);
                        }
                    });

        }
        isSyncing = false;
    }

    private WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo info) {
           wifiP2pInfo = info;
           if(!isSyncing) startClientSync();
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
    public void onEventSync(EventClientSync e){
       clientSyncConfiguration = e.getObject();

        WifiP2pConfig c = new WifiP2pConfig();
        c.deviceAddress = clientSyncConfiguration.getDevice().deviceAddress;
        c.wps.setup = WpsInfo.PBC;
        p2pManager.connect(channel, c, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(LOG_TAG, "connected to device");
                if(wifiP2pInfo != null && !isSyncing) startClientSync();
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
