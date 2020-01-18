package io.eberlein.contacts.activities;

import android.Manifest;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import com.blankj.utilcode.util.FragmentUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.eberlein.contacts.BT;
import io.eberlein.contacts.R;
import io.eberlein.contacts.dialogs.AddressDialog;
import io.eberlein.contacts.dialogs.ContactDialog;
import io.eberlein.contacts.dialogs.EmailAddressDialog;
import io.eberlein.contacts.dialogs.NoteDialog;
import io.eberlein.contacts.dialogs.PhoneNumberDialog;
import io.eberlein.contacts.objects.Settings;
import io.eberlein.contacts.objects.events.EventDeleteAddress;
import io.eberlein.contacts.objects.events.EventDeleteContact;
import io.eberlein.contacts.objects.events.EventDeleteEmailAddress;
import io.eberlein.contacts.objects.events.EventDeleteNote;
import io.eberlein.contacts.objects.events.EventDeletePhoneNumber;
import io.eberlein.contacts.objects.events.EventEncryptionDone;
import io.eberlein.contacts.objects.events.EventSelectedAddress;
import io.eberlein.contacts.objects.events.EventSelectedContact;
import io.eberlein.contacts.objects.events.EventSelectedEmailAddress;
import io.eberlein.contacts.objects.events.EventSelectedNote;
import io.eberlein.contacts.objects.events.EventSelectedPhoneNumber;
import io.eberlein.contacts.ui.FragmentContacts;
import io.eberlein.contacts.ui.FragmentDecrypt;
import io.eberlein.contacts.ui.FragmentEncrypt;
import io.eberlein.contacts.ui.FragmentSettings;
import io.eberlein.contacts.ui.FragmentSync;
import io.realm.Realm;
import static io.eberlein.contacts.Static.getRealm;


// todo add vcf/db export
// todo add network sync


public class MainActivity extends AppCompatActivity {
    private Realm realm;
    private boolean showOptionsMenu = false;
    private boolean showOptionsMenuSync = true;

    @BindView(R.id.toolbar) Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BT.init(this);
        Realm.init(this);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        requestPermissions(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
        }, 420);

        if(BT.supported()){
            requestPermissions(new String[]{
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN
                    }, 420);
        } else {
            showOptionsMenuSync = false;
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
        initDB("");
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BT.uninit(this);
        realm.close();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventEncryptionDone(EventEncryptionDone e){
        initDB(e.getObject());
        showOptionsMenu = true;
        invalidateOptionsMenu();
        FragmentUtils.replace(getSupportFragmentManager(), new FragmentContacts(realm), R.id.fragment_host);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventOpenDialogContact(EventSelectedContact e){
        new ContactDialog(this, e.getObject()).show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventOpenDialogAddress(EventSelectedAddress e){
        new AddressDialog(this, e.getObject()).show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventOpenDialogEmailAddress(EventSelectedEmailAddress e){
        new EmailAddressDialog(this, e.getObject()).show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventOpenDialogPhoneNumber(EventSelectedPhoneNumber e){
        new PhoneNumberDialog(this, e.getObject()).show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventOpenDialogNote(EventSelectedNote e){
        new NoteDialog(this, e.getObject()).show();
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

    private void initDB(String password){
        Settings settings = Settings.get();
        if(settings == null) settings = Settings.create();

        if(settings.isFirstRun()){
            showOptionsMenu = false;
            invalidateOptionsMenu();
            FragmentUtils.replace(getSupportFragmentManager(), new FragmentEncrypt(settings), R.id.fragment_host);
        } else {
            showOptionsMenu = true;
            invalidateOptionsMenu();

            realm = getRealm(settings, password);
            if(realm == null){
                Toast.makeText(this, "password incorrect or database damaged", Toast.LENGTH_LONG).show();
                FragmentUtils.replace(getSupportFragmentManager(), new FragmentDecrypt(), R.id.fragment_host);
            }

            FragmentUtils.replace(getSupportFragmentManager(), new FragmentContacts(realm), R.id.fragment_host);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        for(int i=0; i<menu.size(); i++) {
            if(menu.getItem(i).getTitle().toString().equals(getString(R.string.sync)) && !showOptionsMenuSync) {
                menu.getItem(i).setVisible(false);
            } else {
                menu.getItem(i).setVisible(showOptionsMenu);
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            FragmentUtils.replace(getSupportFragmentManager(), new FragmentSettings(realm), R.id.fragment_host, true);
        } else if(id == R.id.action_sync) {
            FragmentUtils.replace(getSupportFragmentManager(), new FragmentSync(realm), R.id.fragment_host, true);
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
