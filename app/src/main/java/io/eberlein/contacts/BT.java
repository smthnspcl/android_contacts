package io.eberlein.contacts;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class BT {
    private static final String TAG = "BT";

    private static BluetoothManager manager;
    private static BluetoothAdapter adapter;

    public static class ClassicScanner {
        private static final String TAG = "BT.Scanner";

        public interface OnEventListener {
            void onDeviceFound(BluetoothDevice device);
            void onDiscoveryFinished(List<BluetoothDevice> devices);
            void onDiscoveryStarted();
        }

        private static OnEventListener onEventListener;
        private static List<BluetoothDevice> devices = new ArrayList<>();
        private static List<BroadcastReceiver> receivers = new ArrayList<>();

        private static BroadcastReceiver deviceFoundReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(BluetoothDevice.ACTION_FOUND.equals(intent.getAction())){
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    Log.d(TAG, device.getAddress() + " : " + device.getName());
                    if(!devices.contains(device)) devices.add(device);
                    onEventListener.onDeviceFound(device);
                }
            }
        };

        private static BroadcastReceiver discoveryFinishedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(intent.getAction())){
                    Log.d(TAG, "discovery finished");
                    Log.d(TAG, "scan yielded " + devices.size() + " devices");
                    onEventListener.onDiscoveryFinished(devices);
                }
            }
        };

        private static BroadcastReceiver discoveryStartedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(intent.getAction())){
                    Log.d(TAG, "discovery started");
                    onEventListener.onDiscoveryStarted();
                }
            }
        };

        static void init(Context ctx){
            addReceiver(ctx, deviceFoundReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
            addReceiver(ctx, discoveryStartedReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED));
            addReceiver(ctx, discoveryFinishedReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
        }

        public static List<BluetoothDevice> getDevices() {
            return devices;
        }

        static void addReceiver(Context ctx, BroadcastReceiver br, IntentFilter inF){
            receivers.add(br);
            ctx.registerReceiver(br, inF);
        }

        static void unregisterReceivers(Context ctx){
            for(BroadcastReceiver br : receivers) {
                if(br != null) ctx.unregisterReceiver(br);
            }
        }

        public static boolean startDiscovery(OnEventListener oel){
            onEventListener = oel;
            devices = new ArrayList<>();
            return adapter.startDiscovery();
        }

        public static boolean cancelDiscovery(){
            return adapter.cancelDiscovery();
        }
    }

    public static void create(Context ctx){
        manager = (BluetoothManager) ctx.getSystemService(Context.BLUETOOTH_SERVICE);
        adapter = manager.getAdapter();
        ClassicScanner.init(ctx);
    }

    public static void destroy(Context ctx){
        ClassicScanner.unregisterReceivers(ctx);
        Connector.unregister(ctx);
    }

    public static void enable(){
        adapter.enable();
    }

    public static void disable(){
        adapter.disable();
    }

    public static boolean supported(){
        return adapter != null;
    }

    public static boolean isDiscovering(){
        return adapter.isDiscovering();
    }

    public static String getName(){
        return adapter.getName();
    }

    @SuppressLint("HardwareIds")
    public static String getAddress(){
        return adapter.getAddress();
    }

    public static void setDiscoverable(Context ctx, int duration){
        Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        i.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, duration);
        ctx.startActivity(i);
    }

    public static boolean isDeviceBonded(BluetoothDevice device){
        return adapter.getBondedDevices().contains(device);
    }

    public interface ConnectionInterface {
        void onConnected();
        void onDisconnected();
    }

    public static abstract class Connector {
        private static ConnectionInterface connectionInterface;
        private static BluetoothSocket socket = null;

        public static void register(Context ctx, ConnectionInterface ci){
            connectionInterface = ci;
            ctx.registerReceiver(connectionReceiver, new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED));
            ctx.registerReceiver(connectionReceiver, new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED));
        }

        static BroadcastReceiver connectionReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String a = intent.getAction();
                if(BluetoothDevice.ACTION_ACL_CONNECTED.equals(a)) connectionInterface.onConnected();
                else if(BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(a)) connectionInterface.onDisconnected();
            }
        };

        public static void connect(BluetoothDevice device, UUID uuid){
            try {
                socket = device.createInsecureRfcommSocketToServiceRecord(uuid);
                socket.connect();
            } catch (IOException e){
                e.printStackTrace();
            }
        }

        public static BluetoothSocket getSocket() {
            return socket;
        }

        static void unregister(Context ctx){
            ctx.unregisterReceiver(connectionReceiver);
        }
    }

    public static boolean isEnabled(){
        return adapter.isEnabled();
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
        void onClientConnected(BluetoothSocket socket);
    }

    public static abstract class Server extends AsyncTask<Void, Void, Void> implements ServerInterface {
        private static final String TAG = "BT.Server";
        private BluetoothServerSocket serverSocket;

        public Server(String name, UUID uuid){
            createServerSocket(name, uuid);
        }

        @Override
        public void createServerSocket(String name, UUID uuid) {
            try{
                Log.d(TAG, "creating server socket");
                serverSocket = adapter.listenUsingInsecureRfcommWithServiceRecord(name, uuid);
                onServerSocketCreated();
            } catch (IOException e){
                onServerSocketCreateException(e);
            }
        }

        @Override
        public void onServerSocketCreated() {
            Log.d(TAG, "server socket created");
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
        public void onClientConnected(BluetoothSocket socket) {
            Log.d(TAG, "client '" + socket.getRemoteDevice().getName() + "; " + socket.getRemoteDevice().getAddress() + "' connected");
        }

        @Override
        public BluetoothSocket acceptServerSocket() {
            try {
                Log.d(TAG, "accepting incoming connections");
                BluetoothSocket cs = serverSocket.accept();
                onClientConnected(cs);
                return cs;
            } catch (IOException e){
                onAcceptException(e);
                return null;
            }
        }

        @Override
        public void closeServerSocket() {
            try {
                Log.d(TAG, "closing socket");
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

    interface ClientInterface {
        void onReady();
        void onFinished();
    }

    public interface OnDataReceivedInterface {
        void onReceived(String data);
    }

    // todo fix
    // isn't receiving any data
    @SuppressLint("StaticFieldLeak")
    public static abstract class Client extends AsyncTask<Void, Void, Void> implements ClientInterface {
        public static class Reader extends AsyncTask<Void, Void, Void> {
            private static final String TAG = "BT.Client.Reader";
            private InputStream inputStream;
            private boolean doRun;
            private OnDataReceivedInterface onDataReceivedInterface;

            Reader(InputStream inputStream, OnDataReceivedInterface onDataReceivedInterface){
                this.inputStream = inputStream;
                this.onDataReceivedInterface = onDataReceivedInterface;
                doRun = inputStream != null;
                Log.d(TAG, (doRun ? "not " : "") + "going to run");
            }

            private void run(){
                // BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                byte[] buffer = new byte[1024];
                int bytes;
                Log.d(TAG, "receiving messages");
                while (doRun){
                    try {
                        bytes = inputStream.read(buffer);
                        Log.d(TAG, "received " + bytes + " bytes");
                        String data = new String(buffer, 0, bytes);
                        onDataReceivedInterface.onReceived(data);
                        Log.d(TAG, "received: " + data);
                    } catch (IOException e){
                        e.printStackTrace();
                    }
                }
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
                doRun = false;
            }

            @Override
            protected Void doInBackground(Void... voids) {
                run();
                return null;
            }

            boolean isRunning(){
                return doRun;
            }

            void stop(){
                doRun = false;
            }
        }

        public static class Writer extends AsyncTask<Void, Void, Void> {
            private OutputStream outputStream;
            private boolean doRun;
            private List<String> sendData = new ArrayList<>();

            Writer(OutputStream outputStream){
                this.outputStream = outputStream;
                doRun = outputStream != null;
            }

            private void run(){
                OutputStreamWriter osw = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
                while (doRun){
                    for(String s : sendData){
                        try {
                            osw.write(s);
                            osw.flush();
                            sendData.remove(s);
                        } catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
                doRun = false;
            }

            @Override
            protected Void doInBackground(Void... voids) {
                run();
                return null;
            }

            void addSendData(String data){
                sendData.add(data);
            }

            boolean isRunning(){
                return doRun;
            }

            void stop(){
                doRun = false;
            }
        }

        private static final String TAG = "BT.Client";

        private BluetoothSocket socket;

        private OnDataReceivedInterface onDataReceivedInterface;

        private Reader reader = null;
        private Writer writer = null;

        public Client(BluetoothSocket socket, OnDataReceivedInterface onDataReceivedInterface){
            this.socket = socket;
            this.onDataReceivedInterface = onDataReceivedInterface;
        }

        private boolean preCheck(){
            if(socket == null) {
                Log.e(TAG, "socket is null");
                return false;
            }
            if(socket.isConnected()){
                Log.d(TAG, "socket is connected");
            } else {
                Log.e(TAG, "socket is not connected");
                return false;
            }
            return true;
        }

        void run() {
            if(!preCheck()) return;
            try {
                reader = new Reader(socket.getInputStream(), onDataReceivedInterface);
                writer = new Writer(socket.getOutputStream());
                reader.execute(); writer.execute();
                onReady();
                while (reader.isRunning() && writer.isRunning()){
                    try {
                        Thread.sleep(420);
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            onFinished();
        }

        public void stop(){
            if(reader != null) reader.stop();
            if(writer != null) writer.stop();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            run();
            return null;
        }

        protected void setSendData(String data){
            writer.addSendData(data);
        }
    }
}
