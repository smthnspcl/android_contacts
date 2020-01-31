package io.eberlein.contacts.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.GsonUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import io.eberlein.contacts.BT;
import io.eberlein.contacts.R;
import io.eberlein.contacts.adapters.VHAdapter;
import io.eberlein.contacts.dialogs.DialogProgress;
import io.eberlein.contacts.dialogs.DialogSyncConfiguration;
import io.eberlein.contacts.dialogs.DialogSyncInteractive;
import io.eberlein.contacts.dialogs.DialogSyncNonInteractive;
import io.eberlein.contacts.objects.ClientSyncConfiguration;
import io.eberlein.contacts.objects.Contact;
import io.eberlein.contacts.objects.events.EventClientSync;
import io.eberlein.contacts.objects.events.EventSelectedBluetoothDevice;
import io.eberlein.contacts.objects.events.EventSyncFinished;
import io.eberlein.contacts.objects.events.EventSyncReceived;
import io.eberlein.contacts.objects.events.EventSyncReceiving;
import io.eberlein.contacts.objects.events.EventSyncSending;
import io.eberlein.contacts.objects.events.EventSyncSent;
import io.eberlein.contacts.viewholders.VHBluetoothDevice;
import io.realm.Realm;


public class FragmentSync extends Fragment {
    private static final String TAG = "FragmentService";

    private Context ctx;
    private Realm realm;

    private static final int DISCOVERABLE_TIME = 420;
    private static final String SERVICE_NAME = "contactSyncServer";
    private static final UUID SERVICE_UUID = UUID.fromString("2b61e90c-161b-4683-b197-d8129f0fa8d0");

    private List<BluetoothDevice> devices;

    private ClientSyncConfiguration clientSyncConfiguration = null;
    private String savedContacts;

    private AlertDialog dialogSending;
    private AlertDialog dialogReceiving;

    private Server server;
    private Client client;

    private Handler handler = new Handler();

    private Runnable disableServerRunnable = new Runnable() {
        @Override
        public void run() {
            server.cancel(true);
        }
    };

    @BindView(R.id.btn_search_devices) FloatingActionButton btnScan;
    @BindView(R.id.rv_remote_devices) RecyclerView deviceRecycler;
    @BindView(R.id.pb_search) ProgressBar progressBar;
    @BindView(R.id.cb_server) CheckBox hostServer;

    private BT.ClassicScanner.OnEventListener eventListener = new BT.ClassicScanner.OnEventListener() {
        @Override
        public void onDeviceFound(BluetoothDevice device) {

        }

        @Override
        public void onDiscoveryFinished(List<BluetoothDevice> devs) {
            devices.addAll(devs);
            btnScan.show();
            progressBar.setVisibility(View.GONE);
            hostServer.setVisibility(View.VISIBLE);
        }

        @Override
        public void onDiscoveryStarted() {
            btnScan.hide();
            progressBar.setVisibility(View.VISIBLE);
            hostServer.setVisibility(View.GONE);
        }
    };

    private BT.OnDataReceivedInterface onDataReceivedInterface = new BT.OnDataReceivedInterface() {
        @Override
        public void onReceived(String data) {
            Log.d(TAG, "onReceived: " + data);
            // todo
            // makes deserialize data obsolete
        }
    };

    @SuppressLint("StaticFieldLeak")
    private class Client extends BT.Client {
        Client(BluetoothSocket socket){
            super(socket, onDataReceivedInterface);
        }

        @Override
        public void onReady() {
            Log.d(TAG, "onReady");
            addSendData(savedContacts);
        }

        @Override
        public void onFinished() {
            Log.d(TAG, "onFinished");
        }
    }

    private void createExecuteClient(BluetoothSocket socket){
        client = new Client(socket);
        client.execute();
    }

    @SuppressLint("StaticFieldLeak")
    private class Server extends BT.Server {
        Server(){
            super(SERVICE_NAME, SERVICE_UUID);
        }

        @Override
        public void manageSocket(BluetoothSocket socket) {
            createExecuteClient(socket);
        }
    }

    @OnCheckedChanged(R.id.cb_server)
    void onCheckedChangedServer(){
        if(hostServer.isChecked()){
            BT.setDiscoverable(getContext(), DISCOVERABLE_TIME);
            handler.postDelayed(disableServerRunnable, DISCOVERABLE_TIME * 1000);
            server = new Server();
            btnScan.hide();
        } else {
            server.cancel(true);
            btnScan.show();
        }
    }

    @OnClick(R.id.btn_search_devices)
    void onBtnSearchDevicesClicked(){
        if(!BT.isDiscovering()) BT.ClassicScanner.startDiscovery(eventListener);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventSelectedBluetoothDevice(EventSelectedBluetoothDevice e){
        new DialogSyncConfiguration(getContext(), e.getObject()).show();
    }

    private BT.ConnectionInterface connectionInterface = new BT.ConnectionInterface() {
        @Override
        public void onConnected() {
            Log.d(TAG, "onConnected");
            createExecuteClient(BT.Connector.getSocket());
        }

        @Override
        public void onDisconnected() {
            client.stop();
        }
    };

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventClientSync(EventClientSync e){
        clientSyncConfiguration = e.getObject();
        if(!BT.isDeviceBonded(clientSyncConfiguration.getDevice())){
            clientSyncConfiguration.getDevice().createBond();
        } else {
            BT.Connector.register(getContext(), connectionInterface);
            BT.Connector.connect(clientSyncConfiguration.getDevice(), SERVICE_UUID);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventSyncSending(EventSyncSending e){
        dialogSending = new DialogProgress().show(ctx, getString(R.string.sending), getString(R.string.sending));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventSyncSent(EventSyncSent e){
        dialogSending.dismiss();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventSyncReceiving(EventSyncReceiving e){
        dialogReceiving = new DialogProgress().show(ctx, getString(R.string.receiving), getString(R.string.receiving));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventSyncReceived(EventSyncReceived e){
        dialogReceiving.dismiss();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventSyncFinished(EventSyncFinished e){
        List<Contact> receivedContacts = new ArrayList<>(); // todo
        Log.d(TAG, "received " + receivedContacts.size() + " contacts");
        if(hostServer.isChecked()){
            new DialogSyncNonInteractive(ctx, receivedContacts, realm).show();
        } else {
            if(clientSyncConfiguration.isInteractive()){
                new DialogSyncInteractive(ctx, receivedContacts, realm).show();
            } else {
                new DialogSyncNonInteractive(ctx, receivedContacts, realm).show();
            }
        }
    }

    public FragmentSync(Context ctx, Realm realm){
        this.ctx = ctx;
        this.realm = realm;
        devices = new ArrayList<>();
        savedContacts = GsonUtils.toJson(realm.copyFromRealm(realm.where(Contact.class).findAll()));
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        BT.enable();
        EventBus.getDefault().register(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_sync, container, false);
        ButterKnife.bind(this, v);
        deviceRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        VHAdapter deviceAdapter = new VHAdapter<>(VHBluetoothDevice.class, devices);
        deviceRecycler.setAdapter(deviceAdapter);
        return v;
    }
}
