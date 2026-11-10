package com.ravi.atelier;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final int BG = Color.rgb(5, 6, 8);
    private static final int PANEL = Color.rgb(19, 21, 26);
    private static final int WHITE = Color.rgb(244, 246, 250);
    private static final int MUTED = Color.rgb(151, 158, 171);
    private LinearLayout root;
    private TextView featuredName;
    private TextView networkPill;
    private final List<AppEntry> apps = new ArrayList<>();
    private View lastFocused;
    private final String[] preferredPackages = {
        "com.netflix.ninja",
        "com.amazon.amazonvideo.livingroom",
        "com.apple.atve.androidtv.appletv",
        "com.google.android.youtube.tv",
        "in.startv.hotstar",
        "tv.airtel.xstream.tvapp"
    };

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        buildHome();
    }

    @Override protected void onResume() {
        super.onResume();
        refreshNetwork();
    }

    private int dp(float v) { return (int)(v * getResources().getDisplayMetrics().density + 0.5f); }

    private GradientDrawable rounded(int color, int stroke, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radius));
        if (stroke != 0) d.setStroke(dp(1), stroke);
        return d;
    }

    private TextView text(String value, float size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setGravity(Gravity.CENTER_VERTICAL);
        t.setFontFeatureSettings("kern");
        t.setTypeface(Typeface.create("sans-serif", bold ? Typeface.BOLD : Typeface.NORMAL));
        return t;
    }

    private LinearLayout column() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        return l;
    }

    private LinearLayout row() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.HORIZONTAL);
        l.setGravity(Gravity.CENTER_VERTICAL);
        return l;
    }

    private void buildHome() {
        root = column();
        root.setBackgroundColor(BG);
        root.setPadding(dp(42), dp(28), dp(42), dp(24));

        LinearLayout header = row();
        LinearLayout left = column();
        TextView greeting = text("GOOD EVENING", 12, MUTED, true);
        greeting.setLetterSpacing(0.16f);
        TextClock clock = new TextClock(this);
        clock.setFormat24Hour("HH:mm");
        clock.setFormat12Hour("h:mm");
        clock.setTextColor(WHITE);
        clock.setTextSize(48);
        clock.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        TextClock date = new TextClock(this);
        date.setFormat12Hour("EEEE, d MMMM");
        date.setFormat24Hour("EEEE, d MMMM");
        date.setTextColor(MUTED);
        date.setTextSize(15);
        left.addView(greeting);
        left.addView(clock);
        left.addView(date);
        header.addView(left, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        LinearLayout actions = row();
        TextView inputs = pill("INPUTS");
        inputs.setTag("top_inputs");
        inputs.setOnClickListener(v -> showInputs());
        networkPill = pill("WI-FI");
        networkPill.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS)));
        TextView settings = pill("SETTINGS");
        settings.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_SETTINGS)));
        actions.addView(inputs);
        addGap(actions, 10);
        actions.addView(networkPill);
        addGap(actions, 10);
        actions.addView(settings);
        header.addView(actions);
        root.addView(header);

        addGap(root, 22);
        featuredName = text("Choose something to watch", 25, WHITE, false);
        featuredName.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        root.addView(featuredName);
        addGap(root, 14);

        root.addView(sectionLabel("YOUR STREAMING APPS"));
        addGap(root, 10);
        root.addView(buildAppRow());
        addGap(root, 28);

        LinearLayout hdmiHeader = row();
        hdmiHeader.addView(sectionLabel("INPUTS"));
        TextView inputHint = text("HDMI · LIVE TV · AV", 11, MUTED, true);
        inputHint.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        hdmiHeader.addView(inputHint, new LinearLayout.LayoutParams(0, dp(24), 1));
        root.addView(hdmiHeader);
        addGap(root, 10);
        root.addView(buildInputRow());
        addGap(root, 18);

        TextView footer = text("ATELIER  /  TELEVISION", 10, Color.rgb(91, 97, 109), true);
        footer.setLetterSpacing(0.18f);
        root.addView(footer);

        setContentView(root);
        View first = root.findViewWithTag("first_app");
        if (first != null) first.requestFocus();
    }

    private TextView sectionLabel(String s) {
        TextView t = text(s, 12, MUTED, true);
        t.setLetterSpacing(0.14f);
        return t;
    }

    private TextView pill(String label) {
        TextView t = text(label, 11, WHITE, true);
        t.setGravity(Gravity.CENTER);
        t.setPadding(dp(17), 0, dp(17), 0);
        t.setBackground(rounded(Color.rgb(27, 30, 37), Color.rgb(55, 60, 70), 22));
        t.setFocusable(true);
        t.setFocusableInTouchMode(true);
        t.setMinHeight(dp(38));
        t.setOnFocusChangeListener((v, has) -> v.setBackground(rounded(
            has ? Color.rgb(58, 64, 76) : Color.rgb(27, 30, 37),
            has ? Color.rgb(222, 230, 244) : Color.rgb(55, 60, 70), 22)));
        return t;
    }

    private void addGap(LinearLayout parent, int height) {
        View gap = new View(this);
        parent.addView(gap, new LinearLayout.LayoutParams(1, dp(height)));
    }

    private View buildAppRow() {
        HorizontalScrollView scroller = new HorizontalScrollView(this);
        scroller.setHorizontalScrollBarEnabled(false);
        LinearLayout strip = row();
        apps.clear();
        PackageManager pm = getPackageManager();

        for (String pkg : preferredPackages) {
            try {
                ApplicationInfo ai = pm.getApplicationInfo(pkg, 0);
                CharSequence label = pm.getApplicationLabel(ai);
                apps.add(new AppEntry(pkg, label.toString(), pm.getApplicationIcon(ai)));
            } catch (PackageManager.NameNotFoundException ignored) { }
        }

        if (apps.isEmpty()) {
            TextView empty = text("No supported streaming apps were found.", 16, MUTED, false);
            strip.addView(empty);
        }

        for (int i = 0; i < apps.size(); i++) {
            AppEntry app = apps.get(i);
            View tile = appTile(app, i);
            if (i == 0) tile.setTag("first_app");
            strip.addView(tile);
            if (i < apps.size() - 1) {
                View gap = new View(this);
                strip.addView(gap, new LinearLayout.LayoutParams(dp(18), 1));
            }
        }
        scroller.addView(strip);
        return scroller;
    }

    private View appTile(AppEntry app, int index) {
        LinearLayout tile = column();
        tile.setGravity(Gravity.CENTER);
        tile.setFocusable(true);
        tile.setFocusableInTouchMode(true);
        tile.setPadding(dp(8), dp(10), dp(8), dp(8));
        tile.setBackground(rounded(PANEL, 0, 17));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(166), dp(122));
        tile.setLayoutParams(lp);

        ImageView icon = new ImageView(this);
        icon.setImageDrawable(app.icon);
        icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        tile.addView(icon, new LinearLayout.LayoutParams(dp(54), dp(54)));

        TextView name = text(app.label, 12, WHITE, true);
        name.setGravity(Gravity.CENTER);
        name.setMaxLines(1);
        LinearLayout.LayoutParams nameLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(30));
        nameLp.topMargin = dp(7);
        tile.addView(name, nameLp);

        tile.setOnFocusChangeListener((v, has) -> {
            v.animate().scaleX(has ? 1.055f : 1f).scaleY(has ? 1.055f : 1f)
                .setDuration(140).start();
            v.setBackground(rounded(has ? Color.rgb(44, 49, 60) : PANEL,
                has ? Color.rgb(215, 225, 242) : 0, 17));
            if (has) {
                featuredName.setText(app.label);
                lastFocused = v;
            }
        });
        tile.setOnClickListener(v -> launchPackage(app.packageName));
        tile.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                View top = root.findViewWithTag("top_inputs");
                if (top != null) { top.requestFocus(); return true; }
            }
            return false;
        });
        return tile;
    }

    private View buildInputRow() {
        HorizontalScrollView scroller = new HorizontalScrollView(this);
        scroller.setHorizontalScrollBarEnabled(false);
        LinearLayout strip = row();
        addInputCard(strip, "HDMI 1", "Connected device", "hdmi1");
        addInputCard(strip, "HDMI 2", "Connected device", "hdmi2");
        addInputCard(strip, "HDMI 3", "Connected device", "hdmi3");
        addInputCard(strip, "LIVE TV", "Antenna / cable", "livetv");
        addInputCard(strip, "AV", "Composite input", "av");
        scroller.addView(strip);
        return scroller;
    }

    private void addInputCard(LinearLayout strip, String title, String subtitle, String id) {
        LinearLayout card = column();
        card.setFocusable(true);
        card.setFocusableInTouchMode(true);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(18), 0, dp(18), 0);
        card.setBackground(rounded(PANEL, 0, 14));
        TextView a = text(title, 15, WHITE, true);
        TextView b = text(subtitle, 11, MUTED, false);
        card.addView(a);
        card.addView(b);
        strip.addView(card, new LinearLayout.LayoutParams(dp(166), dp(70)));
        View gap = new View(this);
        strip.addView(gap, new LinearLayout.LayoutParams(dp(12), 1));
        card.setOnFocusChangeListener((v, has) -> {
            v.setBackground(rounded(has ? Color.rgb(44,49,60) : PANEL,
                has ? Color.rgb(215,225,242) : 0, 14));
        });
        card.setOnClickListener(v -> openInput(id));
    }

    private void launchPackage(String pkg) {
        try {
            Intent launch = getPackageManager().getLeanbackLaunchIntentForPackage(pkg);
            if (launch == null) launch = getPackageManager().getLaunchIntentForPackage(pkg);
            if (launch == null) {
                Toast.makeText(this, "App launch activity not available", Toast.LENGTH_SHORT).show();
                return;
            }
            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(launch);
        } catch (Exception e) {
            Toast.makeText(this, "Could not open app: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showInputs() {
        String[] items = {"HDMI 1", "HDMI 2", "HDMI 3", "Live TV", "AV"};
        String[] ids = {"hdmi1", "hdmi2", "hdmi3", "livetv", "av"};
        new AlertDialog.Builder(this)
            .setTitle("Choose input")
            .setItems(items, (dialog, which) -> openInput(ids[which]))
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void openInput(String id) {
        // Xiaomi/MediaTek input switching is vendor-specific. Live TV is a reliable
        // fallback; HDMI/AV are exposed as selectable entries but require the TV's
        // private input-switch contract to guarantee direct switching.
        if ("livetv".equals(id)) {
            Intent i = getPackageManager().getLaunchIntentForPackage("com.mediatek.wwtv.tvcenter");
            if (i != null) startActivity(i);
            else Toast.makeText(this, "Live TV app not found", Toast.LENGTH_SHORT).show();
        } else {
            Intent i = new Intent("android.settings.TV_INPUT_SETTINGS");
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try { startActivity(i); }
            catch (Exception e) {
                Toast.makeText(this, "Input switching is controlled by Xiaomi TV firmware. Use the TV remote's Input button.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void refreshNetwork() {
        if (networkPill == null) return;
        try {
            WifiManager wm = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            String ssid = wm != null ? wm.getConnectionInfo().getSSID() : null;
            if (ssid != null && !ssid.equals("<unknown ssid>")) {
                ssid = ssid.replace("\"", "");
                networkPill.setText(ssid.length() > 17 ? ssid.substring(0, 15) + "…" : ssid);
            } else networkPill.setText("WI-FI");
        } catch (Exception e) { networkPill.setText("WI-FI"); }
    }

    private static class AppEntry {
        final String packageName, label;
        final android.graphics.drawable.Drawable icon;
        AppEntry(String p, String l, android.graphics.drawable.Drawable i) {
            packageName = p; label = l; icon = i;
        }
    }
}
