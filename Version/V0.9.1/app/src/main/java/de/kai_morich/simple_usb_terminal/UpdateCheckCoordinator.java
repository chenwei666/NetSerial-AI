package de.kai_morich.simple_usb_terminal;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.chenwei666.netserial.update.GitHubUpdateChecker;
import com.chenwei666.netserial.update.ReleaseInfo;
import com.chenwei666.netserial.update.UpdateCheckPreferences;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public final class UpdateCheckCoordinator {
    private UpdateCheckCoordinator() { }

    public static void checkOnStartup(Activity activity) {
        UpdateCheckPreferences preferences = new UpdateCheckPreferences(activity);
        long now = System.currentTimeMillis();
        if (!preferences.isDue(now)) return;
        check(activity, true);
    }

    public static void checkManually(Activity activity) { check(activity, false); }

    private static void check(Activity activity, boolean silent) {
        Handler main = new Handler(Looper.getMainLooper());
        if (!silent) Toast.makeText(activity, R.string.update_checking, Toast.LENGTH_SHORT).show();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        new GitHubUpdateChecker(executor).check(new GitHubUpdateChecker.Callback() {
            @Override public void onSuccess(ReleaseInfo release) {
                executor.shutdown();
                main.post(() -> {
                    if (activity.isFinishing()) return;
                    new UpdateCheckPreferences(activity).markChecked(System.currentTimeMillis());
                    if (release.isNewerThan(BuildConfig.VERSION_NAME)) {
                        new AlertDialog.Builder(activity)
                                .setTitle(R.string.update_available_title)
                                .setMessage(activity.getString(R.string.update_available_message,
                                        release.getTagName(), BuildConfig.VERSION_NAME))
                                .setNegativeButton(android.R.string.cancel, null)
                                .setPositiveButton(R.string.update_open_release, (dialog, which) ->
                                        activity.startActivity(new Intent(Intent.ACTION_VIEW,
                                                Uri.parse(release.getReleaseUrl()))))
                                .show();
                    } else if (!silent) {
                        new AlertDialog.Builder(activity).setTitle(R.string.update_current_title)
                                .setMessage(activity.getString(R.string.update_current_message,
                                        BuildConfig.VERSION_NAME, release.getTagName()))
                                .setPositiveButton(android.R.string.ok, null).show();
                    }
                });
            }

            @Override public void onFailure(String safeReason) {
                executor.shutdown();
                if (silent) return;
                main.post(() -> {
                    if (!activity.isFinishing()) Toast.makeText(activity,
                            R.string.update_check_failed, Toast.LENGTH_LONG).show();
                });
            }
        });
    }
}
