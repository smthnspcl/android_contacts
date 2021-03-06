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
import io.eberlein.abt.BT;
import io.eberlein.contacts.R;
import io.eberlein.contacts.adapters.VHAdapter;
import io.eberlein.contacts.dialogs.DialogChooseContacts;
import io.eberlein.contacts.dialogs.DialogProgress;
import io.eberlein.contacts.dialogs.DialogSyncConfiguration;
import io.eberlein.contacts.dialogs.DialogSyncNonInteractive;
import io.eberlein.contacts.objects.ClientSyncConfiguration;
import io.eberlein.contacts.objects.Contact;
import io.eberlein.contacts.objects.events.EventClientFinished;
import io.eberlein.contacts.objects.events.EventClientReady;
import io.eberlein.contacts.objects.events.EventClientSync;
import io.eberlein.contacts.objects.events.EventReceivedData;
import io.eberlein.contacts.objects.events.EventSelectedBluetoothDevice;
import io.eberlein.contacts.objects.events.EventSyncSelectedContacts;
import io.eberlein.contacts.viewholders.VHBluetoothDevice;
import io.realm.Realm;


public class FragmentSync extends Fragment {
    private static final String TAG = "FragmentService";

    private Context ctx;
    private Realm realm;

    private static final int DISCOVERABLE_TIME = 420;
    private static final String SERVICE_NAME = "contactSyncServer";
    private static final UUID SERVICE_UUID = UUID.fromString("2b61e90c-161b-4683-b197-d8129f0fa8d0");

    private List<BluetoothDevice> devices = new ArrayList<>();
    private List<Contact> savedContacts = new ArrayList<>();

    private ClientSyncConfiguration clientSyncConfiguration = null;

    private AlertDialog dialogProgress;

    private Server server = null;
    private Client client = null;

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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventReceivedData(EventReceivedData e){
        List<Contact> receivedContacts = e.getObject();
        client.stop();
        Log.d(TAG, "received " + receivedContacts.size() + " contacts");
        new DialogSyncNonInteractive(ctx, receivedContacts, realm).show();
    }

    private BT.OnDataReceivedInterface onDataReceivedInterface = new BT.OnDataReceivedInterface() {
        @Override
        public void onReceived(String data) {
            Log.d(TAG, "onReceived: " + data);
            List<Contact> contacts = GsonUtils.fromJson(data, GsonUtils.getListType(Contact.class));
            Log.d(TAG, "received " + contacts.size() + " contacts");
            EventBus.getDefault().post(new EventReceivedData(contacts));
        }
    };

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventClientReady(EventClientReady e){
        dialogProgress = new DialogProgress().show(ctx, getString(R.string.sync), getString(R.string.sending) + " / " + getString(R.string.receiving));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventClientFinished(EventClientFinished e){
        dialogProgress.dismiss();
    }

    @SuppressLint("StaticFieldLeak")
    private class Client extends BT.Client {
        Client(BluetoothSocket socket){
            super(socket, onDataReceivedInterface);
        }

        @Override
        public void onReady() {
            Log.d(TAG, "onReady");
            EventBus.getDefault().post(new EventClientReady());
            addSendData(GsonUtils.toJson(savedContacts));
        }

        @Override
        public void onFinished() {
            Log.d(TAG, "onFinished");
            EventBus.getDefault().post(new EventClientFinished());
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
            new DialogChooseContacts(ctx, savedContacts).show();
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
            if(client != null) client.stop();
        }
    };

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventSyncSelectedContacts(EventSyncSelectedContacts e){
        savedContacts = e.getObject();
        if(hostServer.isChecked()){
            BT.setDiscoverable(ctx, DISCOVERABLE_TIME);
            handler.postDelayed(disableServerRunnable, DISCOVERABLE_TIME * 1000);
            server = new Server();
            server.execute();
            btnScan.hide();
        } else {
            connect();
        }
    }

    private void connect(){
        if (!BT.isDeviceBonded(clientSyncConfiguration.getDevice())) {
            clientSyncConfiguration.getDevice().createBond();
        } else {
            BT.Connector.register(ctx, connectionInterface);
            BT.Connector.connect(clientSyncConfiguration.getDevice(), SERVICE_UUID);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventClientSync(EventClientSync e){
        clientSyncConfiguration = e.getObject();
        if(clientSyncConfiguration.isInteractive()){
            new DialogChooseContacts(ctx, savedContacts).show();
        } else {
            connect();
        }
    }

    public FragmentSync(Context ctx, Realm realm){
        this.ctx = ctx;
        this.realm = realm;
        savedContacts = realm.copyFromRealm(realm.where(Contact.class).findAll());
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        BT.Connector.unregister(ctx);
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
