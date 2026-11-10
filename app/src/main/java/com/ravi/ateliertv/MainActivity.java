package com.ravi.ateliertv;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** Lightweight, remote-first Android TV home screen. */
public class MainActivity extends Activity {
    private final int BG = Color.rgb(8, 12, 22);
    private final int WHITE = Color.rgb(245, 247, 252);
    private final int MUTED = Color.rgb(159, 171, 193);
    private LinearLayout root;
    private LinearLayout appRow;
    private TextView clock;
    private final List<View> focusables = new ArrayList<>();

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        buildScreen();
    }

    private int dp(float v) { return (int)(v * getResources().getDisplayMetrics().density + 0.5f); }

    private GradientDrawable rounded(int color, int stroke, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color); d.setCornerRadius(dp(radius));
        if (stroke != 0) d.setStroke(dp(2), stroke);
        return d;
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value); t.setTextSize(size); t.setTextColor(color);
        t.setTypeface(bold ? Typeface.create("sans-serif", Typeface.BOLD) : Typeface.create("sans-serif", Typeface.NORMAL));
        t.setGravity(Gravity.CENTER_VERTICAL); t.setIncludeFontPadding(false);
        return t;
    }

    private void buildScreen() {
        ScrollView page = new ScrollView(this);
        page.setFillViewport(true); page.setBackgroundColor(BG);
        root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(52), dp(28), dp(52), dp(36));
        page.addView(root, new ScrollView.LayoutParams(-1, -2));
        setContentView(page);

        LinearLayout top = new LinearLayout(this); top.setGravity(Gravity.CENTER_VERTICAL);
        TextView brand = text("ATELIER", 17, WHITE, true); brand.setLetterSpacing(0.22f);
        top.addView(brand, new LinearLayout.LayoutParams(0, dp(34), 1));
        clock = text(dateLine(), 14, MUTED, false); top.addView(clock);
        root.addView(top, new LinearLayout.LayoutParams(-1, dp(38)));

        LinearLayout hero = new LinearLayout(this); hero.setOrientation(LinearLayout.VERTICAL);
        hero.setGravity(Gravity.CENTER_VERTICAL); hero.setPadding(dp(34), dp(26), dp(34), dp(26));
        GradientDrawable heroBg = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{Color.rgb(31,54,82), Color.rgb(19,31,52), Color.rgb(14,19,34)});
        heroBg.setCornerRadius(dp(22)); hero.setBackground(heroBg);
        TextView eyebrow = text("YOUR TV, WITHOUT THE CLUTTER", 12, Color.rgb(174,204,235), true);
        eyebrow.setLetterSpacing(0.16f); hero.addView(eyebrow);
        TextView headline = text("Everything you watch.\nOne beautiful place.", 31, WHITE, true);
        headline.setLineSpacing(dp(4), 1f);
        LinearLayout.LayoutParams hp = new LinearLayout.LayoutParams(-1, -2); hp.topMargin = dp(14); hero.addView(headline, hp);
        TextView sub = text("Choose an app below to start watching.", 15, Color.rgb(197,207,222), false);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(-1, dp(28)); sp.topMargin = dp(12); hero.addView(sub, sp);
        LinearLayout.LayoutParams heroP = new LinearLayout.LayoutParams(-1, dp(220)); heroP.topMargin = dp(28);
        root.addView(hero, heroP);

        LinearLayout section = new LinearLayout(this); section.setGravity(Gravity.CENTER_VERTICAL);
        TextView title = text("Your apps", 21, WHITE, true); section.addView(title, new LinearLayout.LayoutParams(0, dp(38), 1));
        TextView hint = text("Use the remote to navigate", 12, MUTED, false); section.addView(hint);
        LinearLayout.LayoutParams secP = new LinearLayout.LayoutParams(-1, dp(42)); secP.topMargin = dp(25); root.addView(section, secP);

        HorizontalScrollView hsv = new HorizontalScrollView(this); hsv.setHorizontalScrollBarEnabled(false); hsv.setClipToPadding(false);
        appRow = new LinearLayout(this); appRow.setOrientation(LinearLayout.HORIZONTAL); hsv.addView(appRow);
        root.addView(hsv, new LinearLayout.LayoutParams(-1, dp(132)));
        populateApps();

        LinearLayout footer = new LinearLayout(this); footer.setGravity(Gravity.CENTER_VERTICAL);
        TextView settings = text("⚙   TV Settings", 13, MUTED, false);
        settings.setFocusable(true); settings.setPadding(dp(12), dp(8), dp(16), dp(8));
        settings.setBackground(rounded(Color.rgb(20,27,41), 0, 12));
        settings.setOnClickListener(v -> openSettings());
        footer.addView(settings);
        TextView refresh = text("↻   Refresh apps", 13, MUTED, false);
        refresh.setFocusable(true); refresh.setPadding(dp(16), dp(8), dp(12), dp(8));
        refresh.setOnClickListener(v -> { appRow.removeAllViews(); focusables.clear(); populateApps(); Toast.makeText(this, "App list refreshed", Toast.LENGTH_SHORT).show(); });
        footer.addView(refresh);
        LinearLayout.LayoutParams fp = new LinearLayout.LayoutParams(-1, dp(48)); fp.topMargin = dp(18); root.addView(footer, fp);
        settings.setNextFocusRightId(View.NO_ID);
    }

    private String dateLine() {
        return new SimpleDateFormat("EEE, d MMM   •   h:mm a", Locale.getDefault()).format(new Date());
    }

    private void populateApps() {
        PackageManager pm = getPackageManager();
        Intent leanback = new Intent(Intent.ACTION_MAIN); leanback.addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);
        List<ResolveInfo> found = pm.queryIntentActivities(leanback, 0);
        if (found == null || found.isEmpty()) {
            Intent launch = new Intent(Intent.ACTION_MAIN); launch.addCategory(Intent.CATEGORY_LAUNCHER);
            found = pm.queryIntentActivities(launch, 0);
        }
        Collections.sort(found, new Comparator<ResolveInfo>() {
            @Override public int compare(ResolveInfo a, ResolveInfo b) {
                return a.loadLabel(pm).toString().compareToIgnoreCase(b.loadLabel(pm).toString());
            }
        });
        // Keep the TV's main viewing apps prominent; show other launchable apps after them.
        String[] preferred = {"com.netflix.ninja", "com.amazon.amazonvideo.livingroom", "com.google.android.youtube.tv", "in.startv.hotstar", "com.apple.atve.androidtv.appletv", "org.videolan.vlc"};
        List<ResolveInfo> ordered = new ArrayList<>();
        for (String pkg : preferred) for (ResolveInfo r : found) if (pkg.equals(r.activityInfo.packageName) && !containsPkg(ordered, pkg)) ordered.add(r);
        for (ResolveInfo r : found) if (!containsPkg(ordered, r.activityInfo.packageName)) ordered.add(r);

        for (ResolveInfo r : ordered) {
            final String pkg = r.activityInfo.packageName;
            final String label = r.loadLabel(pm).toString();
            LinearLayout tile = new LinearLayout(this); tile.setOrientation(LinearLayout.VERTICAL); tile.setGravity(Gravity.CENTER);
            tile.setFocusable(true); tile.setClickable(true); tile.setPadding(dp(10), dp(10), dp(10), dp(8));
            tile.setBackground(rounded(Color.rgb(24,32,48), 0, 16));
            TextView icon = text("", 30, WHITE, true); icon.setGravity(Gravity.CENTER);
            try { icon.setCompoundDrawables(null, null, null, null); icon.setBackground(rounded(Color.rgb(38,51,72), 0, 14)); icon.setText(shortMark(label)); }
            catch (Exception ignored) { icon.setText("TV"); }
            tile.addView(icon, new LinearLayout.LayoutParams(dp(64), dp(58)));
            TextView name = text(label, 12, WHITE, true); name.setGravity(Gravity.CENTER); name.setMaxLines(1); name.setEllipsize(android.text.TextUtils.TruncateAt.END);
            LinearLayout.LayoutParams np = new LinearLayout.LayoutParams(dp(112), dp(28)); np.topMargin = dp(7); tile.addView(name, np);
            LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(dp(126), dp(118)); tp.rightMargin = dp(14); appRow.addView(tile, tp);
            tile.setOnClickListener(v -> launchPackage(pkg));
            tile.setOnFocusChangeListener((v, has) -> v.setBackground(rounded(has ? Color.rgb(48,70,99) : Color.rgb(24,32,48), has ? Color.rgb(175,211,247) : 0, 16)));
        }
        if (appRow.getChildCount() > 0) appRow.getChildAt(0).requestFocus();
    }

    private boolean containsPkg(List<ResolveInfo> list, String pkg) {
        for (ResolveInfo r : list) if (pkg.equals(r.activityInfo.packageName)) return true;
        return false;
    }

    private String shortMark(String label) {
        String s = label.trim(); if (s.length() == 0) return "TV";
        String[] parts = s.split("\\s+");
        if (parts.length > 1) return ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase(Locale.getDefault());
        return s.substring(0, Math.min(2, s.length())).toUpperCase(Locale.getDefault());
    }

    private void launchPackage(String pkg) {
        try { Intent i = getPackageManager().getLaunchIntentForPackage(pkg); if (i != null) startActivity(i); else Toast.makeText(this, "This app has no launch activity", Toast.LENGTH_SHORT).show(); }
        catch (Exception e) { Toast.makeText(this, "Unable to open app", Toast.LENGTH_SHORT).show(); }
    }

    private void openSettings() {
        try { startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS)); }
        catch (Exception e) { Toast.makeText(this, "Settings unavailable", Toast.LENGTH_SHORT).show(); }
    }

    @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) { openSettings(); return true; }
        return super.onKeyDown(keyCode, event);
    }
}
