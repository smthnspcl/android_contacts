package io.eberlein.contacts;

import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.blankj.utilcode.util.FragmentUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import butterknife.ButterKnife;
import io.eberlein.contacts.dialogs.AddressDialog;
import io.eberlein.contacts.dialogs.ContactDialog;
import io.eberlein.contacts.dialogs.EmailAddressDialog;
import io.eberlein.contacts.dialogs.NoteDialog;
import io.eberlein.contacts.dialogs.PhoneNumberDialog;
import io.eberlein.contacts.objects.Address;
import io.eberlein.contacts.objects.Contact;
import io.eberlein.contacts.objects.EmailAddress;
import io.eberlein.contacts.objects.Note;
import io.eberlein.contacts.objects.PhoneNumber;
import io.eberlein.contacts.objects.Settings;
import io.eberlein.contacts.objects.events.EventDeleteObject;
import io.eberlein.contacts.objects.events.EventEncryptionDone;
import io.eberlein.contacts.objects.events.EventOpenDialog;
import io.eberlein.contacts.ui.FragmentContacts;
import io.eberlein.contacts.ui.FragmentDecrypt;
import io.eberlein.contacts.ui.FragmentEncrypt;
import io.eberlein.contacts.ui.FragmentSettings;
import io.eberlein.contacts.ui.FragmentSync;
import io.realm.Realm;
import io.realm.RealmConfiguration;


// todo add vcf/db export
// todo add network sync


public class MainActivity extends FragmentActivity {
    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Realm.init(this);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
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
        realm.close();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventEncryptionDone(EventEncryptionDone e){
        initDB(e.getObject());
        FragmentUtils.replace(getSupportFragmentManager(), new FragmentContacts(realm), R.id.fragment_host);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventOpenDialogContact(EventOpenDialog<Contact> e){
        new ContactDialog(this, e.getObject()).show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventOpenDialogAddress(EventOpenDialog<Address> e){
        new AddressDialog(this, e.getObject()).show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventOpenDialogEmailAddress(EventOpenDialog<EmailAddress> e){
        new EmailAddressDialog(this, e.getObject()).show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventOpenDialogPhoneNumber(EventOpenDialog<PhoneNumber> e){
        new PhoneNumberDialog(this, e.getObject()).show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventOpenDialogNote(EventOpenDialog<Note> e){
        new NoteDialog(this, e.getObject()).show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventDeleteContact(EventDeleteObject<Contact> e){
        e.getObject().delete();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventDeleteAddress(EventDeleteObject<Address> e){
        e.getObject().delete();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventDeletePhoneNumber(EventDeleteObject<PhoneNumber> e){
        e.getObject().delete();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventDeleteEmailAddress(EventDeleteObject<EmailAddress> e){
        e.getObject().delete();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventDeleteNote(EventDeleteObject<Note> e){
        e.getObject().delete();
    }

    private void initDB(String password){
        Settings settings = Settings.get();
        if(settings == null) settings = Settings.create();

        if(settings.isFirstRun()){
            FragmentUtils.replace(getSupportFragmentManager(), new FragmentEncrypt(settings), R.id.fragment_host);
        } else {
            RealmConfiguration.Builder rcb = new RealmConfiguration.Builder();

            if(settings.isEncrypted()){
                if(password != null && !password.isEmpty()){
                    try {
                        rcb.encryptionKey(settings.getEncryptionKey(password).getBytes());
                    } catch (GeneralSecurityException | UnsupportedEncodingException e){
                        e.printStackTrace();
                        Toast.makeText(this, "password incorrect or database damaged", Toast.LENGTH_LONG).show();
                        FragmentUtils.replace(getSupportFragmentManager(), new FragmentDecrypt(), R.id.fragment_host);
                    }
                }
            }

            realm = Realm.getInstance(rcb.build());

            FragmentUtils.replace(getSupportFragmentManager(), new FragmentContacts(realm), R.id.fragment_host);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            FragmentUtils.replace(getSupportFragmentManager(), new FragmentSettings(realm), R.id.fragment_host);
        } else if(id == R.id.action_sync) {
            FragmentUtils.replace(getSupportFragmentManager(), new FragmentSync(realm), R.id.fragment_host);
        }

        return super.onOptionsItemSelected(item);
    }
}
