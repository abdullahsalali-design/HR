package com.carlauncher.mobile;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import androidx.annotation.NonNull;
import androidx.car.app.CarContext;
import androidx.car.app.CarToast;
import androidx.car.app.Screen;
import androidx.car.app.model.Action;
import androidx.car.app.model.ItemList;
import androidx.car.app.model.ListTemplate;
import androidx.car.app.model.Row;
import androidx.car.app.model.Template;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class CarAppsScreen extends Screen {
    public CarAppsScreen(@NonNull CarContext carContext) {
        super(carContext);
    }

    @NonNull
    @Override
    public Template onGetTemplate() {
        PackageManager pm = getCarContext().getPackageManager();
        Intent query = new Intent(Intent.ACTION_MAIN, null);
        query.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> apps = new ArrayList<>(pm.queryIntentActivities(query, PackageManager.MATCH_ALL));
        Collections.sort(apps, Comparator.comparing(a -> {
            CharSequence label = a.loadLabel(pm);
            return (label == null ? a.activityInfo.packageName : label.toString()).toLowerCase(Locale.ROOT);
        }));

        ItemList.Builder list = new ItemList.Builder();
        int added = 0;
        for (ResolveInfo info : apps) {
            if (info.activityInfo == null) continue;
            if (getCarContext().getPackageName().equals(info.activityInfo.packageName)) continue;
            if (added >= 50) break;

            final String packageName = info.activityInfo.packageName;
            final String className = info.activityInfo.name;
            CharSequence labelSeq = info.loadLabel(pm);
            final String label = labelSeq == null ? packageName : labelSeq.toString();

            Row row = new Row.Builder()
                    .setTitle(label)
                    .addText("Open on phone")
                    .setOnClickListener(() -> launchOnPhone(packageName, className, label))
                    .build();
            list.addItem(row);
            added++;
        }

        if (added == 0) {
            list.addItem(new Row.Builder()
                    .setTitle("No launchable phone apps found")
                    .addText("Install apps on the connected phone")
                    .build());
        }

        return new ListTemplate.Builder()
                .setTitle("CarLauncher Auto")
                .setHeaderAction(Action.APP_ICON)
                .setSingleList(list.build())
                .build();
    }

    private void launchOnPhone(String packageName, String className, String label) {
        try {
            Intent launch = new Intent();
            launch.setClassName(packageName, className);
            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getCarContext().startActivity(launch);
            CarToast.makeText(getCarContext(), "Opening " + label + " on phone", CarToast.LENGTH_SHORT).show();
        } catch (Exception e) {
            try {
                Intent fallback = getCarContext().getPackageManager().getLaunchIntentForPackage(packageName);
                if (fallback == null) throw new IllegalStateException();
                fallback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getCarContext().startActivity(fallback);
                CarToast.makeText(getCarContext(), "Opening " + label + " on phone", CarToast.LENGTH_SHORT).show();
            } catch (Exception ignored) {
                CarToast.makeText(getCarContext(), "Android Auto blocked this app", CarToast.LENGTH_SHORT).show();
            }
        }
    }
}
