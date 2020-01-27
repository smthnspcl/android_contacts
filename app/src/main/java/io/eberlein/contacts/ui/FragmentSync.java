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

    private static final String SERVICE_NAME = "contactSyncServer";
    private static final UUID SERVICE_UUID = UUID.fromString("2b61e90c-161b-4683-b197-d8129f0fa8d0");
    private static final int DISCOVERABLE_TIME = 420;

    private List<BluetoothDevice> devices;

    private ClientSyncConfiguration clientSyncConfiguration = null;
    private Server server;
    private Client client;
    private List<Contact> savedContacts;

    private AlertDialog dialogSending;
    private AlertDialog dialogReceiving;

    @BindView(R.id.btn_search_devices) FloatingActionButton btnScan;
    @BindView(R.id.rv_remote_devices) RecyclerView deviceRecycler;
    @BindView(R.id.pb_search) ProgressBar progressBar;
    @BindView(R.id.cb_server) CheckBox hostServer;

    private Handler handler = new Handler();

    private Runnable disableServerAfterXSeconds = new Runnable() {
        @Override
        public void run() {
            server.cancel(true);
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
            handler.post(disableServerAfterXSeconds);
        }

        @Override
        public void manageSocket(BluetoothSocket socket) {
            sync(socket);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class Client extends BT.Client<Contact> {
        Client(BluetoothSocket socket){
            super(socket);
        }

        @Override
        public Contact deserializeData(String data) {
            return GsonUtils.fromJson(data, GsonUtils.getListType(Contact.class));
        }

        @Override
        public void onSending() {
            super.onSending();
            EventBus.getDefault().post(new EventSyncSending());
        }

        @Override
        public void onReady() {
            super.onReady();
            try {
                addWriterData("nyees");
                addWriterData("boi");
                addWriterData("gimmefuks\n");
                addWriterData("nignog\r\n");
                addWriterData(GsonUtils.toJson(savedContacts));
            } catch (Exception e){
                e.printStackTrace();
            }
        }

        @Override
        public void onFinished() {
            super.onFinished();
        }

        @Override
        public void onSent() {
            super.onSent();
            EventBus.getDefault().post(new EventSyncSent());
        }

        @Override
        public void onReceiving() {
            super.onReceiving();
            EventBus.getDefault().post(new EventSyncReceiving());
        }

        @Override
        public void onReceived(String data) {
            super.onReceived(data);
            EventBus.getDefault().post(new EventSyncReceived());
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            EventBus.getDefault().post(new EventSyncFinished());
        }
    }

    @OnCheckedChanged(R.id.cb_server)
    void onCheckedChangedServer(){
        if(hostServer.isChecked()){
            BT.setDiscoverable(ctx, DISCOVERABLE_TIME);
            handler.postDelayed(disableServerAfterXSeconds, DISCOVERABLE_TIME * 1000);
            btnScan.hide();
        } else {
            server.cancel(true);
            handler.removeCallbacks(disableServerAfterXSeconds);
            btnScan.show();
        }
    }

    @OnClick(R.id.btn_search_devices)
    void onBtnSearchDevicesClicked(){
        if(!BT.isDiscovering()){
            btnScan.hide();
            progressBar.setVisibility(View.VISIBLE);
            hostServer.setVisibility(View.GONE);
            BT.ClassicScanner.startScan();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventSelectedBluetoothDevice(EventSelectedBluetoothDevice e){
        new DialogSyncConfiguration(getContext(), e.getObject()).show();
    }

    private BroadcastReceiver onBondedReceiver = new BroadcastReceiver() {
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
            client = new Client(socket);  // todo fix encrypted
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
                devices.addAll(BT.ClassicScanner.getDevices());
            }
        }
    };

    private void initBT(){
        BT.ClassicScanner.init(ctx);
        BT.ClassicScanner.addReceiver(ctx, onDiscoveryFinishedReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
        BT.ClassicScanner.addReceiver(ctx, onBondedReceiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
        BT.enable();
    }

    private void uninitBT(){
        BT.ClassicScanner.unregisterReceivers(ctx);
        BT.disable();
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
        handler.removeCallbacks(disableServerAfterXSeconds);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uninitBT();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
