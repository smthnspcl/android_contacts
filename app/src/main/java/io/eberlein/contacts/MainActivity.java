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
import io.eberlein.contacts.dialogs.ContactDialog;
import io.eberlein.contacts.dialogs.DeleteContactDialog;
import io.eberlein.contacts.objects.Contact;
import io.eberlein.contacts.objects.Settings;
import io.eberlein.contacts.objects.events.EventAddContact;
import io.eberlein.contacts.objects.events.EventContactSelected;
import io.eberlein.contacts.objects.events.EventDeleteContact;
import io.eberlein.contacts.objects.events.EventEncryptionDone;
import io.eberlein.contacts.ui.FragmentContacts;
import io.eberlein.contacts.ui.FragmentDecrypt;
import io.eberlein.contacts.ui.FragmentEncrypt;
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
    public void onEventAddContact(EventAddContact e){
        new ContactDialog(this, Contact.create(realm)).build();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventContactSelected(EventContactSelected e){
        new ContactDialog(this, e.getObject()).build();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventDeleteContact(EventDeleteContact e){
        new DeleteContactDialog(this, e.getObject()).build();
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
