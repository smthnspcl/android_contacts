package io.eberlein.contacts.activities;

import android.Manifest;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.blankj.utilcode.constant.PermissionConstants;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.FragmentUtils;
import com.blankj.utilcode.util.PermissionUtils;
import com.github.tamir7.contacts.Contacts;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.eberlein.abt.BT;
import io.eberlein.contacts.R;
import io.eberlein.contacts.Static;
import io.eberlein.contacts.dialogs.DialogAddress;
import io.eberlein.contacts.dialogs.DialogChooseContacts;
import io.eberlein.contacts.dialogs.DialogChooseImporter;
import io.eberlein.contacts.dialogs.DialogChooseNumber;
import io.eberlein.contacts.dialogs.DialogContact;
import io.eberlein.contacts.dialogs.DialogEmailAddress;
import io.eberlein.contacts.dialogs.DialogNote;
import io.eberlein.contacts.dialogs.DialogPhoneNumber;
import io.eberlein.contacts.objects.Contact;
import io.eberlein.contacts.objects.Settings;
import io.eberlein.contacts.objects.events.EventCall;
import io.eberlein.contacts.objects.events.EventDeleteAddress;
import io.eberlein.contacts.objects.events.EventDeleteContact;
import io.eberlein.contacts.objects.events.EventDeleteEmailAddress;
import io.eberlein.contacts.objects.events.EventDeleteNote;
import io.eberlein.contacts.objects.events.EventDeletePhoneNumber;
import io.eberlein.contacts.objects.events.EventEncryptionDone;
import io.eberlein.contacts.objects.events.EventImporterSelected;
import io.eberlein.contacts.objects.events.EventSelectedAddress;
import io.eberlein.contacts.objects.events.EventSelectedContact;
import io.eberlein.contacts.objects.events.EventSelectedEmailAddress;
import io.eberlein.contacts.objects.events.EventSelectedNote;
import io.eberlein.contacts.objects.events.EventSelectedPhoneNumber;
import io.eberlein.contacts.objects.events.EventSms;
import io.eberlein.contacts.objects.events.EventSyncSelectedContacts;
import io.eberlein.contacts.objects.events.EventUserWantsImport;
import io.eberlein.contacts.ui.FragmentContacts;
import io.eberlein.contacts.ui.FragmentDecrypt;
import io.eberlein.contacts.ui.FragmentEncrypt;
import io.eberlein.contacts.ui.FragmentSync;
import io.realm.Realm;
import static io.eberlein.contacts.Static.getRealm;


// todo add csv/vcf import/export


public class MainActivity extends AppCompatActivity {
    private Realm realm;
    private boolean showOptionsMenu = false;

    @BindView(R.id.toolbar) Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Realm.init(this);
        BT.create(this);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        PermissionUtils.permission(PermissionConstants.LOCATION).callback(new PermissionUtils.SimpleCallback() {
            @Override
            public void onGranted() {
                Static.PERMISSION_LOCATION = true;
            }

            @Override
            public void onDenied() {
                Static.PERMISSION_LOCATION = false;
            }
        }).request();

        PermissionUtils.permission(PermissionConstants.CONTACTS).callback(new PermissionUtils.SimpleCallback() {
            @Override
            public void onGranted() {
                Static.PERMISSION_CONTACTS = true;
            }

            @Override
            public void onDenied() {
                Static.PERMISSION_CONTACTS = false;
            }
        }).request();

        // todo check if bluetooth permission was granted / bluetooth is available and disable functions accordingly

        if(BT.supported()){
            requestPermissions(new String[]{
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN
                    }, 420);
        } else {
            showOptionsMenu = false;
            new AlertDialog.Builder(this)
                    .setTitle(R.string.no_bluetooth_support)
                    .setMessage(R.string.unable_sync_contacts)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        initDB(null);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BT.destroy(this);
        if(realm != null) realm.close();
        AppUtils.exitApp();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventEncryptionDone(EventEncryptionDone e){
        initDB(e.getObject());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventOpenDialogContact(EventSelectedContact e){
        new DialogContact(this, e.getObject()).show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventOpenDialogAddress(EventSelectedAddress e){
        new DialogAddress(this, e.getObject()).show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventOpenDialogEmailAddress(EventSelectedEmailAddress e){
        new DialogEmailAddress(this, e.getObject()).show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventOpenDialogPhoneNumber(EventSelectedPhoneNumber e){
        new DialogPhoneNumber(this, e.getObject()).show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventOpenDialogNote(EventSelectedNote e){
        new DialogNote(this, e.getObject()).show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventDeleteContact(EventDeleteContact e){
        e.getObject().delete();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventDeleteAddress(EventDeleteAddress e){
        e.getObject().delete();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventDeletePhoneNumber(EventDeletePhoneNumber e){
        e.getObject().delete();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventDeleteEmailAddress(EventDeleteEmailAddress e){
        e.getObject().delete();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventDeleteNote(EventDeleteNote e){
        e.getObject().delete();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventCall(EventCall e){
        new DialogChooseNumber(this, e.getObject(), true).show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventSms(EventSms e){
        new DialogChooseNumber(this, e.getObject(), false).show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventUserWantsImport(EventUserWantsImport e){
        new DialogChooseImporter(this).show();
    }

    private void notImplementedYet(){
        Toast.makeText(this, "not implemented yet", Toast.LENGTH_SHORT).show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventImporterSelected(EventImporterSelected e){
        int it = e.getObject();
        Log.d("onEventImporterSelected", "yup");
        if(it == Static.IMPORTER_CONTACTS){
            Toast.makeText(this, "this does not quiet work yet", Toast.LENGTH_SHORT).show();
            /*
            if(Static.PERMISSION_CONTACTS) {
                Contacts.initialize(this);
                Contact.convert(realm, Contacts.getQuery().find());
            } else {
                Toast.makeText(this, R.string.permission_not_granted, Toast.LENGTH_SHORT).show();
            }
             */
        } else if(it == Static.IMPORTER_VCF){
            notImplementedYet();
        } else if(it == Static.IMPORTER_CSV){
            notImplementedYet();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventSyncSelectedContacts(EventSyncSelectedContacts e){
        realm.beginTransaction();
        for(Contact c : e.getObject()){
            realm.copyToRealmOrUpdate(c);
        }
        realm.commitTransaction();
    }

    private void initDB(String password){
        Settings settings = Settings.get();
        if(settings == null) settings = Settings.create();

        if(settings.isFirstRun()){
            showOptionsMenu = false;
            invalidateOptionsMenu();
            FragmentUtils.replace(getSupportFragmentManager(), new FragmentEncrypt(settings), R.id.fragment_host);
        } else {
            realm = getRealm(settings, password);
            if(realm != null){
                showOptionsMenu = true;
                invalidateOptionsMenu();
                FragmentUtils.replace(getSupportFragmentManager(), new FragmentContacts(realm), R.id.fragment_host);
            } else {
                showOptionsMenu = false;
                invalidateOptionsMenu();
                FragmentUtils.replace(getSupportFragmentManager(), new FragmentDecrypt(), R.id.fragment_host);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        for (int i = 0; i < menu.size(); i++) menu.getItem(i).setVisible(showOptionsMenu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_sync) {
            FragmentUtils.replace(getSupportFragmentManager(), new FragmentSync(this, realm), R.id.fragment_host, true);
        } else if(id == R.id.action_import) {
            new DialogChooseImporter(this).show();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(FragmentUtils.getFragmentsInStack(getSupportFragmentManager()).size() > 0){
            FragmentUtils.pop(getSupportFragmentManager());
        } else {
            super.onBackPressed();
        }
    }
}
