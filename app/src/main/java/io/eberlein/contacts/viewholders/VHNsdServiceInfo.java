package io.eberlein.contacts.viewholders;

import android.net.nsd.NsdServiceInfo;
import android.view.View;

public class VHNsdServiceInfo extends VH<NsdServiceInfo> {
    public VHNsdServiceInfo(View v){
        super(v);
    }

    @Override
    void onSetObject() {
        left_middle.setText(object.getServiceName());
        right_up.setText(object.getHost().toString());
        right_bottom.setText(String.valueOf(object.getPort()));
        one.setVisibility(View.GONE);
        two.setVisibility(View.GONE);
    }
}
