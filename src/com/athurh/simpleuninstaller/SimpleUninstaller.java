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
		Uri packageURI = Uri.parse("package:" + app.getAppIntName());
		startActivityForResult(new Intent(Intent.ACTION_DELETE, packageURI), position);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		ListView listView = getListView();
		App app = (App) listView.getItemAtPosition(requestCode);
		try {
			getPackageManager().getApplicationInfo(app.getAppIntName(), ApplicationInfo.FLAG_INSTALLED);
		} catch (NameNotFoundException e) {
			((AppAdapter) listView.getAdapter()).reloadView();
		}
	}

	private class App {
		private Drawable appIcon;
		private String appIntName;
		private String appName;

		private Drawable getAppIcon() {
			return appIcon;
		}

		private void setAppIcon(Drawable appIconImage) {
			appIcon = appIconImage;
		}

		private String getAppIntName() {
			return appIntName;
		}

		private void setAppIntName(String appIntNameText) {
			appIntName = appIntNameText;
		}

		private String getAppName() {
			return appName;
		}

		private void setAppName(String appNameText) {
			appName = appNameText;
		}
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
			((ImageView) view.findViewById(R.id.appIcon)).setImageDrawable(app.getAppIcon());
			((TextView) view.findViewById(R.id.appName)).setText(app.getAppName());
			return view;
		}

		private void reloadView() {
			items.clear();
			items.addAll(getApps());
			notifyDataSetChanged();
		}
	}

	private ArrayList<App> getApps() {
		ApplicationInfo appInfo;
		ArrayList<App> appsList = new ArrayList<App>();
		PackageManager pm = getPackageManager();
		List<ApplicationInfo> appsInstalled = pm.getInstalledApplications(PackageManager.GET_META_DATA);
		for (int i = 0; i < appsInstalled.size(); i++) {
			appInfo = appsInstalled.get(i);
			if (!isSystemPackage(appInfo)) {
				App app = new App();
				app.setAppIcon(appInfo.loadIcon(pm));
				app.setAppName(appInfo.loadLabel(pm).toString());
				app.setAppIntName(appInfo.packageName);
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
			return left.getAppName().compareToIgnoreCase(right.getAppName());
		}
	}

}
