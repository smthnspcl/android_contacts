package io.eberlein.contacts.dialogs;

import android.content.Context;
import android.net.nsd.NsdServiceInfo;

import io.eberlein.contacts.R;

public class NsdServiceInfoDialog extends BaseDialog<NsdServiceInfo> {
    public NsdServiceInfoDialog(Context context, NsdServiceInfo object){
        super(context, object, 0);
    }

    @Override
    public void show() {
        // todo
    }
}
