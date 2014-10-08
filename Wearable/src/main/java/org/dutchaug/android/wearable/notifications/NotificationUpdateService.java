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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import org.dutchaug.android.wearable.notifications.common.Constants;

import static com.google.android.gms.wearable.PutDataRequest.WEAR_URI_SCHEME;

/**
 * A {@link com.google.android.gms.wearable.WearableListenerService} that will be invoked when a
 * DataItem is added or deleted. The creation of a new DataItem will be interpreted as a request to
 * create a new notification and the removal of that DataItem is interpreted as a request to
 * dismiss that notification.
 */
public class NotificationUpdateService extends WearableListenerService
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<DataApi.DeleteDataItemsResult> {

    private static final String TAG = "NotificationUpdate";
    private GoogleApiClient mGoogleApiClient;

    @Override
    public void onCreate() {
        super.onCreate();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (null != intent) {
            String action = intent.getAction();
            if (Constants.ACTION_DISMISS.equals(action)) {
                // We need to dismiss the wearable notification. We delete the data item that
                // created the notification and that is how we inform the phone
                int notificationId = intent.getIntExtra(Constants.KEY_NOTIFICATION_ID, -1);
                if (notificationId == Constants.BOTH_ID) {
                    dismissPhoneNotification(notificationId);
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * Dismisses the phone notification, via a {@link android.app.PendingIntent} that is triggered
     * when the user dismisses the local notification. Deleting the corresponding data item notifies
     * the {@link com.google.android.gms.wearable.WearableListenerService} on the phone that the
     * matching notification on the phone side should be removed.
     */
    private void dismissPhoneNotification(int id) {
        mGoogleApiClient.connect();
    }

    /**
     * INSERT CODE HERE FOR onDataChanged
     */

    /**
     * Builds a simple notification on the wearable.
     */

    /**
     * INSERT CODE HERE FOR buildWearableOnlyNotification
     */

    @Override
    public void onConnected(Bundle bundle) {
        final Uri dataItemUri =
                new Uri.Builder().scheme(WEAR_URI_SCHEME).path(Constants.BOTH_PATH).build();
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "Deleting Uri: " + dataItemUri.toString());
        }
        Wearable.DataApi.deleteDataItems(
                mGoogleApiClient, dataItemUri).setResultCallback(this);
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    @Override
    public void onResult(DataApi.DeleteDataItemsResult deleteDataItemsResult) {
        if (!deleteDataItemsResult.getStatus().isSuccess()) {
            Log.e(TAG, "dismissWearableNotification(): failed to delete DataItem");
        }
        mGoogleApiClient.disconnect();
    }
}