package io.eberlein.contacts.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.Toast;

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

import java.io.IOException;
import java.io.OutputStream;
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
import io.eberlein.contacts.viewholders.VHBluetoothDevice;
import io.realm.Realm;

// https://github.com/realm/realm-java/issues/812

public class FragmentSync extends Fragment {
    private static final String TAG = "FragmentService";

    private Context ctx;
    private Realm realm;

    private static final String SERVICE_NAME = "contactSyncServer";
    private static final UUID SERVICE_UUID = UUID.fromString("2b61e90c-161b-4683-b197-d8129f0fa8d0");
    private static final int DISCOVERABLE_TIME = 420;

    private List<BluetoothDevice> devices;
    private VHAdapter deviceAdapter;

    private ClientSyncConfiguration clientSyncConfiguration = null;
    private Server server;
    private Client client;
    private List<Contact> savedContacts;

    @BindView(R.id.btn_search_devices) FloatingActionButton btnScan;
    @BindView(R.id.rv_remote_devices) RecyclerView deviceRecycler;
    @BindView(R.id.pb_search) ProgressBar progressBar;
    @BindView(R.id.cb_server) CheckBox hostServer;

    private Handler makeDiscoverableHandler = new Handler();

    private Runnable makeDiscoverable = new Runnable() {
        @Override
        public void run() {
            BT.setDiscoverable(getContext(), DISCOVERABLE_TIME);
            makeDiscoverableHandler.postDelayed(makeDiscoverable, DISCOVERABLE_TIME * 1000);
        }
    };

    @SuppressLint("StaticFieldLeak")
    private class Server extends BT.Server {

        Server(){
            super(SERVICE_NAME, SERVICE_UUID);
        }

        @Override
        public void onServerSocketCreateException(IOException e) {
            e.printStackTrace();
            hostServer.setChecked(false);
            Toast.makeText(getContext(), R.string.could_not_create_server, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onServerSocketCreated() {
            Toast.makeText(getContext(), R.string.created_server, Toast.LENGTH_SHORT).show();
            makeDiscoverableHandler.post(makeDiscoverable);
        }

        @Override
        public void manageSocket(BluetoothSocket socket) {
            sync(socket);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class Client extends BT.Client<Contact> {
        private AlertDialog sendingDialog;
        private AlertDialog receivingDialog;

        Client(BluetoothSocket socket, boolean isServer){
            super(socket, isServer);
        }

        @Override
        public Contact deserializeData(String data) {
            return GsonUtils.fromJson(data, GsonUtils.getListType(Contact.class));
        }

        @Override
        public void write(OutputStream os) {
            write(os, GsonUtils.toJson(savedContacts));
        }

        @Override
        public void onSending() {
            super.onSending();
            sendingDialog = new DialogProgress().show(ctx, getString(R.string.sending), getString(R.string.sending));
        }

        @Override
        public void onSent() {
            super.onSent();
            sendingDialog.dismiss();
        }

        @Override
        public void onReceiving() {
            super.onReceiving();
            receivingDialog = new DialogProgress().show(ctx, getString(R.string.receiving), getString(R.string.receiving));
        }

        @Override
        public void onReceived(String data) {
            super.onReceived(data);
            receivingDialog.dismiss();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            EventBus.getDefault().post(new EventSyncFinished());
        }
    }

    @OnCheckedChanged(R.id.cb_server)
    void onCheckedChangedServer(){
        if(hostServer.isChecked()){
            btnScan.hide();
            server = new Server();
            server.execute();
        } else {
            server.cancel(true);
            makeDiscoverableHandler.removeCallbacks(makeDiscoverable);
            btnScan.show();
        }
    }

    @OnClick(R.id.btn_search_devices)
    void onBtnSearchDevicesClicked(){
        if(!BT.isDiscovering()){
            btnScan.hide();
            progressBar.setVisibility(View.VISIBLE);
            hostServer.setVisibility(View.GONE);
            BT.Scanner.startScan();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BT.enable();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventSelectedBluetoothDevice(EventSelectedBluetoothDevice e){
        new DialogSyncConfiguration(getContext(), e.getObject()).show();
    }

    private BroadcastReceiver bondReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(intent.getAction())){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(device.getBondState() == BluetoothDevice.BOND_BONDED){
                    EventBus.getDefault().post(new EventClientSync(clientSyncConfiguration));
                }
            }
        }
    };

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventClientSync(EventClientSync e){
        clientSyncConfiguration = e.getObject();
        if(!BT.isDeviceBonded(clientSyncConfiguration.getDevice())){
            clientSyncConfiguration.getDevice().createBond();
        } else {
            sync(BT.connect(clientSyncConfiguration.getDevice(), SERVICE_UUID));
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventSyncFinished(EventSyncFinished e){
        List<Contact> receivedContacts = client.getReceived();
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

    private void sync(BluetoothSocket socket){
        if(socket != null) {
            client = new Client(socket, hostServer.isChecked());  // todo fix encrypted
            client.execute();
        } else {
            Toast.makeText(ctx, "socket is null", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "socket is null");
        }
    }

    private BroadcastReceiver onDiscoveryFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(intent.getAction())){
                btnScan.show();
                progressBar.setVisibility(View.GONE);
                hostServer.setVisibility(View.VISIBLE);
                devices.addAll(BT.Scanner.getDevices());
            }
        }
    };

    private void initBT(){
        BT.Scanner.init(ctx);
        BT.Scanner.addReceiver(ctx, onDiscoveryFinishedReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
        BT.Scanner.addReceiver(ctx, bondReceiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
    }

    public FragmentSync(Context ctx, Realm realm){
        this.ctx = ctx;
        initBT();
        this.realm = realm;
        devices = new ArrayList<>();
        savedContacts = realm.copyFromRealm(realm.where(Contact.class).findAll());
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        makeDiscoverableHandler.removeCallbacks(makeDiscoverable);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BT.Scanner.unregisterReceivers(ctx);
        BT.disable();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_sync, container, false);
        ButterKnife.bind(this, v);
        deviceRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        deviceAdapter = new VHAdapter<>(VHBluetoothDevice.class, devices);
        deviceRecycler.setAdapter(deviceAdapter);
        return v;
    }
}
