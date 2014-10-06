package com.example.android.wearable.synchronizednotifications;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationManagerCompat;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

public class ActionActivity extends Activity {

    public static final String EXTRA_NOTIFICATION_ID = "notification_id";
    public static final String ACTION_DO_MAGIC = "do_magic";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_action);

        final View ivBugdroid = findViewById(R.id.iv_bugdroid);

        Intent intent = getIntent();
        if (intent != null) {
            if (ACTION_DO_MAGIC.equals(intent.getAction())) {
                Toast.makeText(this, getString(R.string.magic_message), Toast.LENGTH_SHORT).show();
                Animation anim = AnimationUtils.loadAnimation(this, R.anim.bugdroid);
                if (true) {
                    anim.setRepeatCount(Animation.INFINITE);
                    anim.setRepeatMode(Animation.REVERSE);
                } else
                    anim.setAnimationListener(new Animation.AnimationListener() {

                        @Override
                        public void onAnimationEnd(Animation anim) {
                            anim.reset();
                            ivBugdroid.startAnimation(anim);

                        }

                        @Override
                        public void onAnimationRepeat(Animation anim) {
                        }

                        @Override
                        public void onAnimationStart(Animation anim) {
                        }

                    });
                ivBugdroid.startAnimation(anim);
            }
            int notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0);
            NotificationManagerCompat notificationManager =
                    NotificationManagerCompat.from(this);
            notificationManager.cancel(notificationId);
        }
    }
}
