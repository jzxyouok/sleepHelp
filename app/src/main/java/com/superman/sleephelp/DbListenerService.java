package com.superman.sleephelp;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class DbListenerService extends Service {
    public DbListenerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
