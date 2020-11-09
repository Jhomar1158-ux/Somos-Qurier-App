package receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import providers.ResiBookingProvider;

public class AcceptReceiver extends BroadcastReceiver {

    private ResiBookingProvider mResiBookingProvider;
    @Override
    public void onReceive(Context context, Intent intent) {
        //Se ejecuto cuando aceptasmos la notificación
        String idResi = intent.getExtras().getString("idResi");
        mResiBookingProvider = new ResiBookingProvider();

        mResiBookingProvider.updateStatus(idResi,"accept");

        NotificationManager manager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        manager.cancel(2);
    }
}
