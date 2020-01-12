package io.eberlein.contacts.viewholders;

import android.net.nsd.NsdServiceInfo;
import android.view.View;

import org.greenrobot.eventbus.EventBus;

import butterknife.OnClick;
import io.eberlein.contacts.objects.events.EventSelectedSyncDevice;

public class VHNsdServiceInfo extends VH<NsdServiceInfo> {
    public VHNsdServiceInfo(View v){
        super(v);
    }

    @OnClick
    void onClick(){
        EventBus.getDefault().post(new EventSelectedSyncDevice(object));
    }

    @Override
    public void onSetObject() {
        left_middle.setText(object.getServiceName());
        right_up.setText(object.getHost().toString());
        right_bottom.setText(String.valueOf(object.getPort()));
        one.setVisibility(View.GONE);
        two.setVisibility(View.GONE);
    }
}
