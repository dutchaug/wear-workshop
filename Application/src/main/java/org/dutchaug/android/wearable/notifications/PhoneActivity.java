/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dutchaug.android.wearable.notifications;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.view.View;

import org.dutchaug.android.wearable.notifications.R;
import org.dutchaug.android.wearable.notifications.common.Constants;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataItemAsset;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.text.DateFormat;
import java.util.Date;
import java.util.Map;

/**
 * A simple activity that presents three buttons that would trigger three different combinations of
 * notifications on the handset and the watch:
 * <ul>
 * <li>The first button builds a simple local-only notification on the handset.</li>
 * <li>The second one creates a wearable-only notification by putting a data item in the shared data
 * store and having a {@link com.google.android.gms.wearable.WearableListenerService} listen for
 * that on the wearable</li>
 * <li>The third one creates a local notification and a wearable notification by combining the above
 * two. It, however, demonstrates how one can set things up so that the dismissal of one
 * notification results in the dismissal of the other one.</li>
 * </ul>
 */
public class PhoneActivity extends Activity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "PhoneActivity";
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    /**
     * Builds a simple notification that displays both on the wearable and handset.
     */
    private void buildSimpleNotification(String title, String content, int notificationId) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.drawable.ic_notification)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_notification_big));

        // Build intent for notification content
        Intent viewIntent = new Intent(this, ActionActivity.class);
        viewIntent.putExtra(ActionActivity.EXTRA_NOTIFICATION_ID, notificationId);
        PendingIntent viewPendingIntent =
                PendingIntent.getActivity(this, 0, viewIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(viewPendingIntent);

        // Build intent for action notification content
        Intent actionIntent = new Intent(this, ActionActivity.class);
        actionIntent.setAction(ActionActivity.ACTION_DO_MAGIC);
        actionIntent.putExtra(ActionActivity.EXTRA_NOTIFICATION_ID, notificationId);
        PendingIntent actionPendingIntent =
                PendingIntent.getActivity(this, 0, actionIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.addAction(R.drawable.ic_action, getString(R.string.action_title), actionPendingIntent);

        // Create a WearableExtender to add functionality for wearables
        NotificationCompat.WearableExtender wearableExtender =
                new NotificationCompat.WearableExtender()
                        .setHintHideIcon(true)
                        .setBackground(BitmapFactory.decodeResource(getResources(), R.drawable.dutchaug));

        // Uncomment these lines to show a different text for the action
        //NotificationCompat.Action action =
        //        new NotificationCompat.Action.Builder(R.drawable.ic_action,
        //                getString(R.string.action_title), actionPendingIntent).build();
        //wearableExtender.addAction(action);

        builder.extend(wearableExtender);

        // Get an instance of the NotificationManager service
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // Build the notification and issues it with notification manager
        notificationManager.notify(notificationId, builder.build());
    }

    /**
     * Builds a local-only notification for the handset. This is achieved by using
     * <code>setLocalOnly(true)</code>. If <code>withDismissal</code> is set to <code>true</code>, a
     * {@link android.app.PendingIntent} will be added to handle the dismissal of notification to
     * be able to remove the mirrored notification on the wearable.
     */
    private void buildLocalOnlyNotification(String title, String content, int notificationId,
                                            boolean withDismissal) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.drawable.ic_notification)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_notification_big))
                .setLocalOnly(true);

        // Build intent for notification content
        Intent viewIntent = new Intent(this, ActionActivity.class);
        viewIntent.putExtra(ActionActivity.EXTRA_NOTIFICATION_ID, notificationId);
        PendingIntent viewPendingIntent =
                PendingIntent.getActivity(this, 0, viewIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(viewPendingIntent);

        // Build intent for action notification content
        Intent actionIntent = new Intent(this, ActionActivity.class);
        actionIntent.setAction(ActionActivity.ACTION_DO_MAGIC);
        actionIntent.putExtra(ActionActivity.EXTRA_NOTIFICATION_ID, notificationId);
        PendingIntent actionPendingIntent =
                PendingIntent.getActivity(this, 0, actionIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.addAction(R.drawable.ic_action, getString(R.string.action_title), actionPendingIntent);

        if (withDismissal) {
            // Send an intent when the notification is deleted
            Intent dismissIntent = new Intent(Constants.ACTION_DISMISS);
            dismissIntent.putExtra(Constants.KEY_NOTIFICATION_ID, Constants.BOTH_ID);
            PendingIntent pendingIntent = PendingIntent
                    .getService(this, 0, dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setDeleteIntent(pendingIntent);
        }

        // Get an instance of the NotificationManager service
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // Build the notification and issues it with notification manager
        notificationManager.notify(notificationId, builder.build());
    }

    /**
     * Builds a DataItem that on the wearable will be interpreted as a request to show a
     * notification. The result will be a notification that only shows up on the wearable.
     */
    private void buildWearableOnlyNotification(String title, String content, String path) {
        if (mGoogleApiClient.isConnected()) {
            PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(path);
            putDataMapRequest.getDataMap().putString(Constants.KEY_CONTENT, content);
            putDataMapRequest.getDataMap().putString(Constants.KEY_TITLE, title);
            PutDataRequest request = putDataMapRequest.asPutDataRequest();
            Wearable.DataApi.putDataItem(mGoogleApiClient, request)
                    .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                        @Override
                        public void onResult(DataApi.DataItemResult dataItemResult) {
                            if (!dataItemResult.getStatus().isSuccess()) {
                                Log.e(TAG, "buildWatchOnlyNotification(): Failed to set the data, "
                                        + "status: " + dataItemResult.getStatus().getStatusCode());
                            }
                        }
                    });
        } else {
            Log.e(TAG, "buildWearableOnlyNotification(): no Google API Client connection");
        }
    }

    /**
     * Builds a local notification and sets a DataItem that will be interpreted by the wearable as
     * a request to build a notification on the wearable as as well. The two notifications show
     * different messages.
     * Dismissing either of the notifications will result in dismissal of the other; this is
     * achieved by creating a {@link android.app.PendingIntent} that results in removal of
     * the DataItem that created the watch notification. The deletion of the DataItem is observed on
     * both sides, using WearableListenerService callbacks, and is interpreted on each side as a
     * request to dismiss the corresponding notification.
     */
    private void buildMirroredNotifications(String phoneTitle, String watchTitle, String content) {
        if (mGoogleApiClient.isConnected()) {
            // Wearable notification
            buildWearableOnlyNotification(watchTitle, content, Constants.BOTH_PATH);

            // Local notification, with a pending intent for dismissal
            buildLocalOnlyNotification(phoneTitle, content, Constants.BOTH_ID, true);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(Bundle bundle) {
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "Failed to connect to Google API Client");
    }

    /**
     * Returns a string built from the current time
     */
    private String now() {
        DateFormat dateFormat = android.text.format.DateFormat.getTimeFormat(this);
        return dateFormat.format(new Date());
    }

    /**
     * Handles button clicks in the UI.
     */
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.simple:
                buildSimpleNotification(getString(R.string.simple), now(),
                        Constants.SIMPLE_ID);
                break;
            case R.id.phone_only:
                buildLocalOnlyNotification(getString(R.string.phone_only), now(),
                        Constants.PHONE_ONLY_ID, false);
                break;
            case R.id.wear_only:
                buildWearableOnlyNotification(getString(R.string.wear_only), now(),
                        Constants.WATCH_ONLY_PATH);
                break;
            case R.id.different_notifications:
                buildMirroredNotifications(getString(R.string.phone_both), getString(R.string.watch_both), now());
                break;
        }
    }
}
