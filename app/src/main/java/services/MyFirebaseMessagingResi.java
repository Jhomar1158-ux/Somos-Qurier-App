package services;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.optic.curri.R;

import channel.NotificationHelper;
import receivers.AcceptReceiver;
import receivers.CancelReceiver;

import java.util.Map;

public class MyFirebaseMessagingResi extends FirebaseMessagingService {

    private static final int NOTIFICATION_CODE = 100;

    //PODREMOS USAR UN TOKEN DE USUARIO DE DISPOSITIVO A DISPOSITIVO
    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
    }

    //aquí recibimos las notificaciones push
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        RemoteMessage.Notification notification = remoteMessage.getNotification();
        Map<String, String> data = remoteMessage.getData();
        String title = data.get("title");
        String body = data.get("body");

        if (title != null){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                if (title.contains("Solicitud de servicio")){
                    String idResi = data.get("idResi");
                    showNotificationApiOreoActions(title,body,idResi);
                }
                else {
                    showNotificationApiOreo(title,body);
                }
            }
            else {
                if (title.contains("Solicitud de servicio")) {
                    String idResi = data.get("idResi");
                    showNotificationActions(title,body,idResi);
                }
                else {
                    showNotification(title, body);

                }
                }
            }


    }
//ACEPTAR
private void showNotification(String title, String body) {
    PendingIntent intent = PendingIntent.getActivity(getBaseContext(), 0, new Intent(), PendingIntent.FLAG_ONE_SHOT);
    Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    NotificationHelper notificationHelper = new NotificationHelper(getBaseContext());
    NotificationCompat.Builder builder = notificationHelper.getNotificationOldAPI(title, body, intent, sound);
    notificationHelper.getManager().notify(1, builder.build());
}

    private void showNotificationActions(String title, String body, String idResi) {

        // ACEPTAR
        Intent acceptIntent = new Intent(this, AcceptReceiver.class);
        acceptIntent.putExtra("idResi", idResi);
        PendingIntent acceptPendingIntent = PendingIntent.getBroadcast(this, NOTIFICATION_CODE, acceptIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Action acceptAction = new NotificationCompat.Action.Builder(
                R.mipmap.ic_launcher,
                "Aceptar",
                acceptPendingIntent
        ).build();

        // CANCELAR

        Intent cancelIntent = new Intent(this, CancelReceiver.class);
        cancelIntent.putExtra("idResi", idResi);
        PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(this, NOTIFICATION_CODE, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Action cancelAction = new NotificationCompat.Action.Builder(
                R.mipmap.ic_launcher,
                "Cancelar",
                cancelPendingIntent
        ).build();

        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationHelper notificationHelper = new NotificationHelper(getBaseContext());
        NotificationCompat.Builder builder = notificationHelper.getNotificationOldAPIActions(title, body, sound, acceptAction, cancelAction);
        notificationHelper.getManager().notify(2, builder.build());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showNotificationApiOreo(String title, String body) {
        PendingIntent intent = PendingIntent.getActivity(getBaseContext(), 0, new Intent(), PendingIntent.FLAG_ONE_SHOT);
        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationHelper notificationHelper = new NotificationHelper(getBaseContext());
        Notification.Builder builder = notificationHelper.getNotification(title, body, intent, sound);
        notificationHelper.getManager().notify(1, builder.build());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showNotificationApiOreoActions(String title, String body, String idResi) {

        Intent acceptIntent = new Intent(this, AcceptReceiver.class);
        acceptIntent.putExtra("idResi", idResi);
        PendingIntent acceptPendingIntent = PendingIntent.getBroadcast(this, NOTIFICATION_CODE, acceptIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Action acceptAction= new Notification.Action.Builder(
                R.mipmap.ic_launcher,
                "Aceptar",
                acceptPendingIntent
        ).build();

        Intent cancelIntent = new Intent(this, CancelReceiver.class);
        cancelIntent.putExtra("idResi", idResi);
        PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(this, NOTIFICATION_CODE, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Action cancelAction= new Notification.Action.Builder(
                R.mipmap.ic_launcher,
                "Cancelar",
                cancelPendingIntent
        ).build();

        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationHelper notificationHelper = new NotificationHelper(getBaseContext());
        Notification.Builder builder = notificationHelper.getNotificationActions(title, body, sound, acceptAction, cancelAction);
        notificationHelper.getManager().notify(2, builder.build());
    }
}