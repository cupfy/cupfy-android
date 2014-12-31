package br.com.estudiotrilha.apphooks;

import android.app.Activity;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import br.com.estudiotrilha.apphooks.database.Namespace;

public class GcmIntentService extends IntentService
{
    public static final String TAG = "GcmIntentService";

    NotificationCompat.Builder builder;

    public GcmIntentService()
    {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);

        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty())
        {
            // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM
             * will be extended in the future with new message types, just ignore
             * any message types you're not interested in, or that you don't
             * recognize.
             */
            if(GoogleCloudMessaging.
                    MESSAGE_TYPE_SEND_ERROR.equals(messageType))
            {
                //sendNotification("Send error: " + extras.toString());
            }
            else if(GoogleCloudMessaging.
                    MESSAGE_TYPE_DELETED.equals(messageType))
            {
                // sendNotification("Deleted messages on server: " +
                //         extras.toString());
                // If it's a regular GCM message, do some work.
            }
            else if(GoogleCloudMessaging.
                    MESSAGE_TYPE_MESSAGE.equals(messageType))
            {
                sendNewMessageNotification(extras);
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        //GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNewMessageNotification(Bundle extra)
    {
        String namespace = extra.getString("namespace");
        String title = extra.getString("title");
        String message = extra.getString("message");

        if(!Namespace.isNamespaceActivated(namespace, getApplicationContext())) {
            return;
        }

        int refID = namespace.hashCode();

        Notification n;

        int iconRefID = R.drawable.logo_push;

        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.bigText(namespace + "\n" + message);

        n = new NotificationCompat.Builder(GcmIntentService.this)
                .setContentTitle(title)
                .setContentText(namespace)
                .setStyle(bigTextStyle)
                .setSmallIcon(iconRefID)
                .setAutoCancel(true)
                .build();

        NotificationManager notificationManager =
                (NotificationManager) GcmIntentService.this.getSystemService(Activity.NOTIFICATION_SERVICE);

        notificationManager.notify(refID, n);
    }

}