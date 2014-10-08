package org.dutchaug.android.wearable.notifications;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.NotificationManagerCompat;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import org.dutchaug.android.wearable.notifications.R;

public class ActionActivity extends Activity {

    public static final String EXTRA_NOTIFICATION_ID = "notification_id";
    public static final String ACTION_DO_MAGIC = "do_magic";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_action);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        final View ivBugdroid = findViewById(R.id.iv_bugdroid);

        Intent intent = getIntent();
        if (intent != null) {
            if (ACTION_DO_MAGIC.equals(intent.getAction())) {
                Toast.makeText(this, getString(R.string.magic_message), Toast.LENGTH_SHORT).show();
                Animation anim = AnimationUtils.loadAnimation(this, R.anim.bugdroid);
                ivBugdroid.startAnimation(anim);
            }
            int notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0);
            NotificationManagerCompat notificationManager =
                    NotificationManagerCompat.from(this);
            notificationManager.cancel(notificationId);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
