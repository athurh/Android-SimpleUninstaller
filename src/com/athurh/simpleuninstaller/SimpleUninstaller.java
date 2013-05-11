package com.athurh.simpleuninstaller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class SimpleUninstaller extends ListActivity {

    private final static String TAG = "SimpleUninstaller";
    private PackageManager mPkgMgr;
    private ListView mListView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPkgMgr = getPackageManager();
        mListView = getListView();
        mListView.setFastScrollEnabled(true);
        setListAdapter(new AppAdapter(this.getApplication(), R.layout.activity_main, getApps()));
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        App app = (App) l.getItemAtPosition(position);
        Uri packageURI = Uri.parse("package:" + app.appIntName);
        startActivityForResult(new Intent(Intent.ACTION_DELETE, packageURI), position);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        ((AppAdapter) mListView.getAdapter()).reloadView();
    }

    private static final class App {
        private String appIntName;
        private String appName;
    }

    private static final class AppHolder {
        private ImageView appIcon;
        private TextView appName;
        private String appIntName;
    }

    private class AppAdapter extends ArrayAdapter<App> {
        public ArrayList<App> items;

        private AppAdapter(Context context, int viewId, ArrayList<App> mApps) {
            super(context, viewId, mApps);
            items = mApps;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            App app = items.get(position);
            AppHolder holder;

            if (view == null) {
                LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = li.inflate(R.layout.activity_main, null);
                holder = new AppHolder();
                holder.appIcon = (ImageView) view.findViewById(R.id.appIcon);
                holder.appName = (TextView) view.findViewById(R.id.appName);
                view.setTag(holder);
            } else {
                holder = (AppHolder) view.getTag();
            }
            if (app != null) {
                holder.appIntName = app.appIntName;
                holder.appName.setText(app.appName);
                new GetIcon().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, holder);
            }
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
        List<ApplicationInfo> appsInstalled = mPkgMgr.getInstalledApplications(
                PackageManager.GET_UNINSTALLED_PACKAGES);

        for (ApplicationInfo appInfo : appsInstalled) {
            if (!isSystemPackage(appInfo)) {
                App app = new App();
                app.appIntName = appInfo.packageName;
                app.appName = appInfo.loadLabel(mPkgMgr).toString();
                appsList.add(app);
            }
        }
        Collections.sort(appsList, new AppNameComparator());
        return appsList;
    }

    private class GetIcon extends AsyncTask<AppHolder, Void, Bitmap> {
        private AppHolder appHolder;

        @Override
        protected Bitmap doInBackground(AppHolder... params) {
            appHolder = params[0];
            Drawable icon;

            try {
                icon = mPkgMgr.getApplicationInfo(appHolder.appIntName, 0).loadIcon(mPkgMgr);
            } catch (NameNotFoundException e) {
                icon = getResources().getDrawable(R.drawable.ic_launcher);
                Log.w(TAG, "icon not found " + e);
            }
            return ((BitmapDrawable) icon).getBitmap();
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            appHolder.appIcon.setImageBitmap(result);
        }
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
