package io.eberlein.contacts;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;

import com.blankj.utilcode.util.GsonUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.eberlein.contacts.objects.Contact;


public class BT {
    private static IntentFilter filterActionFound = new IntentFilter(BluetoothDevice.ACTION_FOUND);
    private static IntentFilter filterScanFinished = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
    private static boolean isDiscovering = false;
    private static BluetoothAdapter adapter;
    private static List<BluetoothDevice> scanResults;

    private static BroadcastReceiver deviceFoundReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctx, Intent i) {
            if(BluetoothDevice.ACTION_FOUND.equals(i.getAction())) scanResults.add(i.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE));
        }
    };

    private static BroadcastReceiver scanFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(intent.getAction())) isDiscovering = false;
        }
    };

    public static void init(Context context){
        adapter = BluetoothAdapter.getDefaultAdapter();
        scanResults = new ArrayList<>();
        context.registerReceiver(deviceFoundReceiver, filterActionFound);
        context.registerReceiver(scanFinishedReceiver, filterScanFinished);
    }

    public static void uninit(Context ctx){
        ctx.unregisterReceiver(deviceFoundReceiver);
        ctx.unregisterReceiver(scanFinishedReceiver);
    }

    public static boolean supported(){
        return adapter != null;
    }

    public static boolean enable(){
        if(adapter.isEnabled()) return true;
        return adapter.enable();
    }

    public static boolean disable(){
        if(!adapter.isEnabled()) return true;
        return adapter.disable();
    }

    public static List<BluetoothDevice> getBondedDevices(){
        List<BluetoothDevice> r = new ArrayList<>();
        for(BluetoothDevice d : adapter.getBondedDevices()) r.add(d);
        return r;
    }

    public static void setDiscoverable(Context ctx, int duration){
        Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        i.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, duration);
        ctx.startActivity(i);
    }

    public static void replaceDeviceFoundReceiver(Context ctx, BroadcastReceiver dfr){
        ctx.unregisterReceiver(deviceFoundReceiver);
        deviceFoundReceiver = dfr;
        ctx.registerReceiver(deviceFoundReceiver, filterActionFound);
    }

    public static void replaceScanFinishedReceiver(Context ctx, BroadcastReceiver sfr){
        ctx.unregisterReceiver(scanFinishedReceiver);
        scanFinishedReceiver = sfr;
        ctx.registerReceiver(scanFinishedReceiver, filterScanFinished);
    }

    public static void discover(){
        scanResults = new ArrayList<>();
        if(!isDiscovering) isDiscovering = adapter.startDiscovery();
    }

    public static void cancelDiscovery(){
        if(isDiscovering) adapter.cancelDiscovery();
    }

    public static List<BluetoothDevice> getScanResults(){
        return scanResults;
    }

    public static boolean isDiscovering(){
        return isDiscovering;
    }

    @SuppressLint("HardwareIds")
    public static String getMac(){
        return adapter.getAddress();
    }

    public static String getName(){
        return adapter.getName();
    }

    public static BluetoothAdapter getAdapter(){
        return adapter;
    }

    public static void setName(String name){
        adapter.setName(name);
    }

    private static BluetoothSocket _connect(BluetoothDevice device, UUID uuid){
        try {
            return device.createRfcommSocketToServiceRecord(uuid);
        } catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }

    public static BluetoothSocket connect(String address, UUID uuid){
        return _connect(adapter.getRemoteDevice(address), uuid);
    }

    public static BluetoothSocket connect(BluetoothDevice device, UUID uuid){
        return _connect(device, uuid);
    }

    public interface ServerInterface {
        void createServerSocket(String name, UUID uuid);
        BluetoothSocket acceptServerSocket();
        void manageSocket(BluetoothSocket socket);
        void closeServerSocket();
        void onServerSocketCreated();
        void onAcceptException(IOException e);
        void onServerSocketCloseException(IOException e);
        void onServerSocketCreateException(IOException e);
    }

    public static abstract class Server extends AsyncTask<Void, Void, Void> implements ServerInterface {
        private BluetoothServerSocket serverSocket;

        public Server(String name, UUID uuid){
            createServerSocket(name, uuid);
        }

        @Override
        public void createServerSocket(String name, UUID uuid) {
            try{
                serverSocket = adapter.listenUsingInsecureRfcommWithServiceRecord(name, uuid);
                onServerSocketCreated();
            } catch (IOException e){
                onServerSocketCreateException(e);
            }
        }

        @Override
        public void onServerSocketCreated() {

        }

        @Override
        public void onServerSocketCreateException(IOException e) {
            e.printStackTrace();
        }

        @Override
        public void onAcceptException(IOException e) {
            e.printStackTrace();
        }

        @Override
        public void onServerSocketCloseException(IOException e) {
            e.printStackTrace();
        }

        @Override
        public BluetoothSocket acceptServerSocket() {
            try {
                return serverSocket.accept();
            } catch (IOException e){
                onAcceptException(e);
                return null;
            }
        }

        @Override
        public void closeServerSocket() {
            try {
                serverSocket.close();
            } catch (IOException e){
                onServerSocketCloseException(e);
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            run();
            return null;
        }

        void run() {
            BluetoothSocket socket = acceptServerSocket();
            if(socket != null) manageSocket(socket);
            closeServerSocket();
        }
    }

    interface ClientInterface<T> {
        void write(OutputStream os);
        void read(BufferedReader br);
        T deserializeData(String data);
    }

    @SuppressLint("StaticFieldLeak")
    public static abstract class Client<T> extends AsyncTask<Void, Void, Void> implements ClientInterface<T> {
        private BluetoothSocket socket;
        private boolean isServer;
        private List<T> received;

        public Client(BluetoothSocket socket, boolean isServer){
            this.socket = socket;
            this.isServer = isServer;
            received = new ArrayList<>();
        }

        public void read(BufferedReader br){
            try {
                for (String line; (line = br.readLine()) != null; ) {
                    received.add(deserializeData(line));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void writeFlush(OutputStream os, String data){
            try {
                os.write(data.getBytes());
                os.flush();
            } catch (IOException e){
                e.printStackTrace();
            }
        }

        void run() {
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
                os.close();
                br.close();
            } catch (IOException e){
                e.printStackTrace();
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            run();
            return null;
        }

        public List<T> getReceived() {
            return received;
        }
    }
}
