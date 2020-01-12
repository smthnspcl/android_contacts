package io.eberlein.contacts.viewholders;

import android.net.nsd.NsdServiceInfo;
import android.view.View;

import org.greenrobot.eventbus.EventBus;

import butterknife.OnClick;
import butterknife.OnLongClick;
import io.eberlein.contacts.R;
import io.eberlein.contacts.objects.events.EventSelectedSyncDevice;

public class VHNsdServiceInfo extends VH<NsdServiceInfo> {
    public VHNsdServiceInfo(View v){
        super(v);
    }

    @OnClick
    void onClick(){
        EventBus.getDefault().post(new EventSelectedSyncDevice(object));
    }

    @OnLongClick
    void onLongClick(){

    }

    @Override
    public void onSetObject() {
        left_middle.setText(object.getServiceName());
        right_up.setText(object.getHost().toString());
        right_bottom.setText(String.valueOf(object.getPort()));
    }
}
