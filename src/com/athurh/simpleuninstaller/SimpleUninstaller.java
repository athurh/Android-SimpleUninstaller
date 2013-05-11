package com.athurh.simpleuninstaller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;

public class SimpleUninstaller extends ListActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getListView().setFastScrollEnabled(true);
        setListAdapter(new AppAdapter(this, R.layout.activity_main, getApps()));
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        App app = (App) l.getItemAtPosition(position);
        Uri packageURI = Uri.parse("package:" + app.appIntName);
        startActivityForResult(new Intent(Intent.ACTION_DELETE, packageURI), position);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        ListView listView = getListView();
        App app = (App) listView.getItemAtPosition(requestCode);
        try {
            getPackageManager().getApplicationInfo(app.appIntName, ApplicationInfo.FLAG_INSTALLED);
        } catch (NameNotFoundException e) {
            ((AppAdapter) listView.getAdapter()).reloadView();
        }
    }

    private class App {
        private Drawable appIcon;
        private String appIntName;
        private String appName;
    }

    private class AppAdapter extends ArrayAdapter<App> {
        public ArrayList<App> items;

        private AppAdapter(Context context, int viewId, ArrayList<App> mApps) {
            super(context, viewId, mApps);
            items = mApps;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = vi.inflate(R.layout.activity_main, null);
            }
            App app = items.get(position);
            ((ImageView) view.findViewById(R.id.appIcon)).setImageDrawable(app.appIcon);
            ((TextView) view.findViewById(R.id.appName)).setText(app.appName);
            return view;
        }

        private void reloadView() {
            items.clear();
            items.addAll(getApps());
            notifyDataSetChanged();
        }
    }

    private ArrayList<App> getApps() {
        ArrayList<App> appsList = new ArrayList<App>();
        PackageManager pm = getPackageManager();
        List<ApplicationInfo> appsInstalled = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        for (ApplicationInfo appInfo : appsInstalled) {
            if (!isSystemPackage(appInfo)) {
                App app = new App();
                app.appIcon = appInfo.loadIcon(pm);
                app.appName = appInfo.loadLabel(pm).toString();
                app.appIntName = appInfo.packageName;
                appsList.add(app);
            }
        }
        Collections.sort(appsList, new AppNameComparator());
        return appsList;
    }

    private boolean isSystemPackage(ApplicationInfo pkg) {
        return ((pkg.flags & ApplicationInfo.FLAG_SYSTEM) != 0) ? true : false;
    }

    private class AppNameComparator implements Comparator<App> {
        public int compare(App left, App right) {
            return left.appName.compareToIgnoreCase(right.appName);
        }
    }

}
