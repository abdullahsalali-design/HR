package com.carlauncher.mobile;

import android.app.Activity;
import android.app.role.RoleManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    private final List<AppEntry> allApps = new ArrayList<>();
    private final List<AppEntry> shownApps = new ArrayList<>();
    private AppAdapter adapter;
    private TextView countText;
    private EditText searchBox;

    private int dp(float value) { return Math.round(value * getResources().getDisplayMetrics().density); }

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.rgb(16, 17, 20));
        getWindow().setNavigationBarColor(Color.rgb(16, 17, 20));
        setContentView(buildUi());
        loadApps();
    }

    @Override protected void onResume() {
        super.onResume();
        if (adapter != null) loadApps();
    }

    private View buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(14), dp(18), dp(12));
        root.setBackgroundColor(Color.rgb(16, 17, 20));

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout titleArea = new LinearLayout(this);
        titleArea.setOrientation(LinearLayout.VERTICAL);
        titleArea.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView title = new TextView(this);
        title.setText("CarLauncher");
        title.setTextColor(Color.WHITE);
        title.setTextSize(27);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        titleArea.addView(title);

        countText = new TextView(this);
        countText.setTextColor(Color.rgb(184, 187, 195));
        countText.setTextSize(13);
        titleArea.addView(countText);
        header.addView(titleArea);

        TextView settingsButton = actionButton("Home settings");
        settingsButton.setOnClickListener(v -> openHomeSettings());
        header.addView(settingsButton);
        root.addView(header);

        searchBox = new EditText(this);
        searchBox.setHint("Search apps");
        searchBox.setHintTextColor(Color.rgb(145, 148, 157));
        searchBox.setTextColor(Color.WHITE);
        searchBox.setSingleLine(true);
        searchBox.setTextSize(17);
        searchBox.setPadding(dp(14), 0, dp(14), 0);
        searchBox.setBackground(makeRoundedBackground(Color.rgb(27, 29, 34), dp(14)));
        LinearLayout.LayoutParams searchParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(52));
        searchParams.topMargin = dp(16);
        searchParams.bottomMargin = dp(14);
        root.addView(searchBox, searchParams);

        GridView grid = new GridView(this);
        grid.setNumColumns(GridView.AUTO_FIT);
        grid.setColumnWidth(dp(118));
        grid.setHorizontalSpacing(dp(10));
        grid.setVerticalSpacing(dp(10));
        grid.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
        grid.setClipToPadding(false);
        grid.setPadding(0, 0, 0, dp(18));
        grid.setVerticalScrollBarEnabled(false);
        adapter = new AppAdapter(this);
        grid.setAdapter(adapter);
        grid.setOnItemClickListener((parent, view, position, id) -> launchApp(shownApps.get(position)));
        root.addView(grid, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        searchBox.addTextChangedListener(new SimpleTextWatcher() {
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterApps(s == null ? "" : s.toString());
            }
        });
        return root;
    }

    private TextView actionButton(String label) {
        TextView button = new TextView(this);
        button.setText(label);
        button.setTextColor(Color.rgb(16, 17, 20));
        button.setTextSize(14);
        button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        button.setGravity(Gravity.CENTER);
        button.setPadding(dp(14), dp(10), dp(14), dp(10));
        button.setBackground(makeRoundedBackground(Color.rgb(140, 198, 63), dp(12)));
        return button;
    }

    private Drawable makeRoundedBackground(int color, int radius) {
        android.graphics.drawable.GradientDrawable d = new android.graphics.drawable.GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(radius);
        return d;
    }

    private void loadApps() {
        PackageManager pm = getPackageManager();
        Intent query = new Intent(Intent.ACTION_MAIN, null);
        query.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> resolved = pm.queryIntentActivities(query, PackageManager.MATCH_ALL);
        allApps.clear();
        for (ResolveInfo info : resolved) {
            if (info.activityInfo == null) continue;
            String packageName = info.activityInfo.packageName;
            String className = info.activityInfo.name;
            if (getPackageName().equals(packageName)) continue;
            CharSequence labelSeq = info.loadLabel(pm);
            String label = labelSeq == null ? packageName : labelSeq.toString();
            Drawable icon;
            try { icon = info.loadIcon(pm); } catch (Exception e) { icon = getApplicationInfo().loadIcon(pm); }
            allApps.add(new AppEntry(label, packageName, className, icon));
        }
        Collections.sort(allApps, Comparator.comparing(a -> a.label.toLowerCase(Locale.ROOT)));
        filterApps(searchBox == null ? "" : searchBox.getText().toString());
    }

    private void filterApps(String query) {
        String needle = query.trim().toLowerCase(Locale.ROOT);
        shownApps.clear();
        for (AppEntry app : allApps) {
            if (needle.isEmpty() || app.label.toLowerCase(Locale.ROOT).contains(needle) || app.packageName.toLowerCase(Locale.ROOT).contains(needle)) shownApps.add(app);
        }
        if (countText != null) countText.setText(shownApps.size() + " apps");
        if (adapter != null) adapter.notifyDataSetChanged();
    }

    private void launchApp(AppEntry app) {
        try {
            Intent launch = new Intent();
            launch.setClassName(app.packageName, app.className);
            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(launch);
        } catch (Exception first) {
            try {
                Intent fallback = getPackageManager().getLaunchIntentForPackage(app.packageName);
                if (fallback == null) throw new ActivityNotFoundException();
                fallback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(fallback);
            } catch (Exception ignored) { Toast.makeText(this, "Unable to open " + app.label, Toast.LENGTH_SHORT).show(); }
        }
    }

    private void openHomeSettings() {
        try {
            if (android.os.Build.VERSION.SDK_INT >= 29) {
                RoleManager roleManager = (RoleManager) getSystemService(Context.ROLE_SERVICE);
                if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_HOME)) {
                    if (!roleManager.isRoleHeld(RoleManager.ROLE_HOME)) {
                        startActivityForResult(roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME), 2001);
                        return;
                    }
                    Toast.makeText(this, "CarLauncher is already your Home app", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            startActivity(new Intent(Settings.ACTION_HOME_SETTINGS));
        } catch (Exception e) { startActivity(new Intent(Settings.ACTION_SETTINGS)); }
    }

    private static class AppEntry {
        final String label, packageName, className;
        final Drawable icon;
        AppEntry(String label, String packageName, String className, Drawable icon) {
            this.label = label; this.packageName = packageName; this.className = className; this.icon = icon;
        }
    }

    private class AppAdapter extends BaseAdapter {
        private final Context context;
        AppAdapter(Context context) { this.context = context; }
        @Override public int getCount() { return shownApps.size(); }
        @Override public Object getItem(int position) { return shownApps.get(position); }
        @Override public long getItemId(int position) { return position; }
        @Override public View getView(int position, View convertView, ViewGroup parent) {
            AppEntry app = shownApps.get(position);
            LinearLayout tile;
            ImageView icon;
            TextView label;
            if (convertView instanceof LinearLayout) {
                tile = (LinearLayout) convertView;
                icon = (ImageView) tile.getChildAt(0);
                label = (TextView) tile.getChildAt(1);
            } else {
                tile = new LinearLayout(context);
                tile.setOrientation(LinearLayout.VERTICAL);
                tile.setGravity(Gravity.CENTER);
                tile.setPadding(dp(10), dp(13), dp(10), dp(11));
                tile.setBackground(makeRoundedBackground(Color.rgb(27, 29, 34), dp(16)));
                tile.setMinimumHeight(dp(116));
                icon = new ImageView(context);
                LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(54), dp(54));
                iconParams.bottomMargin = dp(8);
                tile.addView(icon, iconParams);
                label = new TextView(context);
                label.setTextColor(Color.WHITE);
                label.setTextSize(13);
                label.setGravity(Gravity.CENTER);
                label.setMaxLines(2);
                label.setEllipsize(android.text.TextUtils.TruncateAt.END);
                tile.addView(label, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
            icon.setImageDrawable(app.icon);
            label.setText(app.label);
            return tile;
        }
    }

    private abstract static class SimpleTextWatcher implements android.text.TextWatcher {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void afterTextChanged(android.text.Editable s) {}
    }
}
