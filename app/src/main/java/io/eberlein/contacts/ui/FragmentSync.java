package io.eberlein.contacts.ui;

import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.eberlein.contacts.R;
import io.eberlein.contacts.adapters.VHAdapter;
import io.eberlein.contacts.objects.Settings;
import io.eberlein.contacts.objects.events.EventWithObject;
import io.eberlein.contacts.viewholders.VHNsdServiceInfo;
import io.realm.Realm;


public class FragmentSync extends Fragment {
    private static final String SERVICE_NAME = "contactSync";
    private boolean registrationListenerRegistered = false;
    private Realm realm;
    private ServerSocket serverSocket;
    private NsdServiceInfo localDevice;
    private List<NsdServiceInfo> remoteDevices;
    private VHAdapter<NsdServiceInfo, VHNsdServiceInfo> adapter;
    private NsdManager nsdManager;

    @BindView(R.id.rv_remote_devices) RecyclerView recyclerRemoteDevices;
    @BindView(R.id.btn_discover) Button discover;

    @OnClick(R.id.btn_discover)
    void onSwitchEnableClicked(){
        nsdManager.discoverServices(SERVICE_NAME, NsdManager.PROTOCOL_DNS_SD, discoveryListener);
        discover.setClickable(false);
    }

    private NsdManager.ResolveListener resolveListener = new NsdManager.ResolveListener() {
        @Override
        public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
            // Called when the resolve fails. Use the error code to debug.
            Toast.makeText(getContext(), "resolving '" + serviceInfo.getServiceName() + "' failed", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onServiceResolved(NsdServiceInfo serviceInfo) {
            if (serviceInfo.getServiceName().equals(localDevice.getServiceName())) return;
            EventBus.getDefault().post(new EventWithObject<>(serviceInfo));
        }
    };

    private NsdManager.DiscoveryListener discoveryListener = new NsdManager.DiscoveryListener() {
        @Override
        public void onStartDiscoveryFailed(String serviceType, int errorCode) {
            nsdManager.stopServiceDiscovery(this);
            Toast.makeText(getContext(), "could not start discovery", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onStopDiscoveryFailed(String serviceType, int errorCode) {
            nsdManager.stopServiceDiscovery(this);
            Toast.makeText(getContext(), "could not stop discovery", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onDiscoveryStarted(String serviceType) {
            Toast.makeText(getContext(), "discovery started", Toast.LENGTH_LONG).show();
            discover.setClickable(false);
        }

        @Override
        public void onDiscoveryStopped(String serviceType) {
            discover.setClickable(true);
            nsdManager.discoverServices(serviceType, NsdManager.PROTOCOL_DNS_SD, discoveryListener);
        }

        @Override
        public void onServiceFound(NsdServiceInfo service) {
            if (service.getServiceName().equals(localDevice.getServiceName())) return;
            if (service.getServiceName().contains(SERVICE_NAME)) nsdManager.resolveService(service, resolveListener);
        }

        @Override
        public void onServiceLost(NsdServiceInfo serviceInfo) {

        }
    };

    private NsdManager.RegistrationListener registrationListener = new NsdManager.RegistrationListener() {
        @Override
        public void onServiceRegistered(NsdServiceInfo si) {
            localDevice = si;
        }

        @Override
        public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
            // Registration failed! Put debugging code here to determine why.
        }

        @Override
        public void onServiceUnregistered(NsdServiceInfo arg0) {
            // Service has been unregistered. This only happens when you call
            // NsdManager.unregisterService() and pass in this listener.
        }

        @Override
        public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
            // Unregistration failed. Put debugging code here to determine why.
        }
    };


    public FragmentSync(Realm realm, NsdManager nsdManager){
        this.remoteDevices = new ArrayList<>();
        this.realm = realm;
        this.nsdManager = nsdManager;
    }

    private static String getHostName(String defValue) {
        try {
            Method getString = Build.class.getDeclaredMethod("getString", String.class);
            getString.setAccessible(true);
            return getString.invoke(null, "net.hostname").toString();
        } catch (Exception ex) {
            return defValue;
        }
    }

    private void registerService(){
        try {
            serverSocket = new ServerSocket(0);
            localDevice= new NsdServiceInfo();
            localDevice.setServiceName(SERVICE_NAME + ":" + getHostName(String.valueOf(new Random().nextInt())));
            localDevice.setServiceType("_nsdcontactsync._tcp");
            localDevice.setPort(serverSocket.getLocalPort());
            nsdManager.registerService(localDevice, NsdManager.PROTOCOL_DNS_SD, registrationListener);
            nsdManager.discoverServices(localDevice.getServiceType(), NsdManager.PROTOCOL_DNS_SD, discoveryListener);
            registrationListenerRegistered = true;
            Toast.makeText(getContext(), "registered service '" + localDevice.getServiceName() + "'", Toast.LENGTH_LONG).show();
        } catch (IOException e){
            e.printStackTrace();
            Toast.makeText(getContext(), "could not create server socket", Toast.LENGTH_LONG).show();
        }
    }

    private void unregisterService(){
        if(registrationListenerRegistered) nsdManager.unregisterService(registrationListener);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(realm.where(Settings.class).findFirst().isDiscoverable()) registerService();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventWithServiceObject(EventWithObject<NsdServiceInfo> e){
        if(!remoteDevices.contains(e.getObject())){
            remoteDevices.add(e.getObject());
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        registerService();
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        unregisterService();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_sync, container, false);
        ButterKnife.bind(this, v);
        recyclerRemoteDevices.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new VHAdapter<NsdServiceInfo, VHNsdServiceInfo>(VHNsdServiceInfo.class, remoteDevices);
        recyclerRemoteDevices.setAdapter(adapter);
        return v;
    }
}
