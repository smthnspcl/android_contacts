package io.eberlein.contacts.ui;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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

import com.alibaba.fastjson.JSON;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
import io.eberlein.contacts.dialogs.DialogBaseSyncConfiguration;
import io.eberlein.contacts.objects.Contact;
import io.eberlein.contacts.objects.events.EventClientSync;
import io.eberlein.contacts.objects.events.EventSelectedBluetoothDevice;
import io.eberlein.contacts.objects.events.EventSentContact;
import io.eberlein.contacts.viewholders.VHBluetoothDevice;
import io.realm.Realm;


public class FragmentSync extends Fragment {
    private static final String SERVICE_NAME = "contactSyncServer";
    private static final UUID SERVICE_UUID = UUID.fromString("2b61e90c-161b-4683-b197-d8129f0fa8d0");
    private static final int DISCOVERABLE_TIME = 420;
    private Server server;
    private Realm realm;
    private List<BluetoothDevice> devices;
    private VHAdapter deviceAdapter;

    @BindView(R.id.btn_search_devices) FloatingActionButton btnScan;
    @BindView(R.id.rv_remote_devices) RecyclerView deviceRecycler;
    @BindView(R.id.pb_search) ProgressBar progressBar;
    @BindView(R.id.cb_server) CheckBox hostServer;

    private BroadcastReceiver deviceFoundReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(BluetoothDevice.ACTION_FOUND.equals(intent.getAction())){
                devices.add(intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE));
            }
        }
    };

    private BroadcastReceiver scanDoneReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(intent.getAction())){
                btnScan.show();
                progressBar.setVisibility(View.GONE);
                hostServer.setVisibility(View.VISIBLE);
            }
        }
    };

    private Handler makeDiscoverableHandler = new Handler();

    private Runnable makeDiscoverable = new Runnable() {
        @Override
        public void run() {
            BT.setDiscoverable(getContext(), DISCOVERABLE_TIME);
            makeDiscoverableHandler.postDelayed(makeDiscoverable, DISCOVERABLE_TIME * 1000);
        }
    };

    private class SyncThread extends Thread {
        private static final String MSG_END = "END OF TRANSMISSION";
        private BluetoothSocket socket;
        private boolean isServer;

        SyncThread(BluetoothSocket socket, boolean isServer){
            this.socket = socket;
            this.isServer = isServer;
        }

        private void syncContact(Contact nc){
            Contact oc = realm.where(Contact.class).equalTo("uuid", nc.getUuid()).findFirst();
            if(oc == null) realm.copyToRealm(nc);
            else oc.sync(nc);
        }

        private void read(BufferedReader br){
            boolean done = false;
            while(!done) {
                try {
                    for (String line; (line = br.readLine()) != null; ) {
                        if(line.equals(MSG_END)) done = true;
                        else syncContact(JSON.parseObject(line, Contact.class));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void writeFlush(OutputStream os, String data){
            try {
                os.write(data.getBytes());
                os.flush();
            } catch (IOException e){
                e.printStackTrace();
            }
        }

        private void write(OutputStream os){
            for(Contact c : realm.where(Contact.class).findAll()){
                writeFlush(os, JSON.toJSONString(c));
                EventBus.getDefault().post(new EventSentContact(c));
            }
            writeFlush(os, MSG_END);
        }

        @Override
        public void run() {
            try {
                OutputStream os = socket.getOutputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                if(isServer) {
                    read(br);
                    write(os);
                } else {
                    write(os);
                    read(br);
                }
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    private void sync(BluetoothSocket socket){
        SyncThread st = new SyncThread(socket, hostServer.isChecked());
        st.run();
    }

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

    @OnCheckedChanged(R.id.cb_server)
    void onCheckedChangedServer(){
        if(hostServer.isChecked()){
            server = new Server();
        } else {
            server.stopServer();
            makeDiscoverableHandler.removeCallbacks(makeDiscoverable);
        }
    }

    @OnClick(R.id.btn_search_devices)
    void onBtnSearchDevicesClicked(){
        if(!BT.isDiscovering()) {
            BT.discover();
            btnScan.hide();
            progressBar.setVisibility(View.VISIBLE);
            hostServer.setVisibility(View.GONE);
        }
    }

    public FragmentSync(Realm realm){
        this.realm = realm;
        devices = new ArrayList<>();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BT.enable();
        BT.replaceDeviceFoundReceiver(getContext(), deviceFoundReceiver);
        BT.replaceScanFinishedReceiver(getContext(), scanDoneReceiver);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventSelectedBluetoothDevice(EventSelectedBluetoothDevice e){
        new DialogBaseSyncConfiguration(getContext(), e.getObject()).show();
        // sync(BT.connect(e.getObject(), SERVICE_UUID));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventClientSync(EventClientSync e){

    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
