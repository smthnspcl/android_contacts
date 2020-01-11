package io.eberlein.contacts.ui;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import io.eberlein.contacts.R;
import io.eberlein.contacts.objects.Settings;
import io.realm.Realm;


public class FragmentSync extends Fragment {
    private Realm realm;
    private ServerSocket serverSocket;
    private String serviceName;
    private List<NsdServiceInfo> remoteDevices;
    private NsdManager nsdManager;

    @BindView(R.id.cb_discoverable) CheckBox cbDiscoverable;

    @OnCheckedChanged(R.id.cb_discoverable)
    void onSwitchEnableClicked(){
        if(cbDiscoverable.isChecked()) registerService();
        else unregisterService();
    }

    private NsdManager.ResolveListener resolveListener = new NsdManager.ResolveListener() {
        @Override
        public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
            // Called when the resolve fails. Use the error code to debug.
            Toast.makeText(getContext(), "resolving '" + serviceInfo.getServiceName() + "' failed", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onServiceResolved(NsdServiceInfo serviceInfo) {
            if (serviceInfo.getServiceName().equals(serviceName)) return;
            remoteDevices.add(serviceInfo);
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
        }

        @Override
        public void onDiscoveryStopped(String serviceType) {

        }

        @Override
        public void onServiceFound(NsdServiceInfo service) {
            if (service.getServiceName().contains(serviceName)) {
                nsdManager.resolveService(service, resolveListener);
            }
        }

        @Override
        public void onServiceLost(NsdServiceInfo serviceInfo) {

        }
    };

    private NsdManager.RegistrationListener registrationListener = new NsdManager.RegistrationListener() {
        @Override
        public void onServiceRegistered(NsdServiceInfo si) {
            // Save the service name. Android may have changed it in order to
            // resolve a conflict, so update the name you initially requested
            // with the name Android actually used.
            serviceName = si.getServiceName();
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


    public FragmentSync(Realm realm){
        this.remoteDevices = new ArrayList<>();
        this.realm = realm;
        this.nsdManager = (NsdManager) getActivity().getSystemService(Context.NSD_SERVICE);
    }

    private void registerService(){
        try {
            serverSocket = new ServerSocket(0);
            NsdServiceInfo si = new NsdServiceInfo();
            si.setServiceName("contactSync");
            si.setServiceType("_nsdcontactsync._tcp");
            si.setPort(serverSocket.getLocalPort());
            nsdManager.registerService(si, NsdManager.PROTOCOL_DNS_SD, registrationListener);
        } catch (IOException e){
            e.printStackTrace();
            Toast.makeText(getContext(), "could not create server socket", Toast.LENGTH_LONG).show();
        }
    }

    private void unregisterService(){
        if(registrationListener != null) nsdManager.unregisterService(registrationListener);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(realm.where(Settings.class).findFirst().isDiscoverable()) registerService();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterService();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_sync, container, false);
        ButterKnife.bind(this, v);
        return v;
    }
}
