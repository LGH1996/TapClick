package com.lgh.advertising.going.mybean;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.lgh.advertising.going.myclass.DataDao;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Entity(indices = @Index(value = "appPackage", unique = true))
public class AppDescribe {
	@PrimaryKey(autoGenerate = true)
	public Integer id;
	public String appName;
	public String appPackage;
	public boolean onOff;
	public int autoFinderRetrieveTime;
	public boolean autoFinderRetrieveAllTime;
	public int coordinateRetrieveTime;
	public boolean coordinateRetrieveAllTime;
	public int widgetRetrieveTime;
	public boolean widgetRetrieveAllTime;
	public boolean autoFinderOnOFF;
	public boolean coordinateOnOff;
	public boolean widgetOnOff;

	@Ignore
	public AutoFinder autoFinder;
	@Ignore
	public Map<String, Coordinate> coordinateMap;
	@Ignore
	public Map<String, Set<Widget>> widgetSetMap;

	public AppDescribe() {
		this.appName = "";
		this.appPackage = "";
		this.onOff = true;
		this.autoFinderRetrieveTime = 8000;
		this.autoFinderRetrieveAllTime = false;
		this.coordinateRetrieveTime = 15000;
		this.coordinateRetrieveAllTime = false;
		this.widgetRetrieveTime = 15000;
		this.widgetRetrieveAllTime = false;
		this.autoFinderOnOFF = true;
		this.coordinateOnOff = true;
		this.widgetOnOff = true;
		this.autoFinder = new AutoFinder();
		this.coordinateMap = new HashMap<>();
		this.widgetSetMap = new HashMap<>();
	}

	public void getOtherFieldsFromDatabase(DataDao dataDao) {
		getAutoFinderFromDatabase(dataDao);
		getCoordinateMapFromDatabase(dataDao);
		getWidgetSetMapFromDatabase(dataDao);
	}

	public void getAutoFinderFromDatabase(DataDao dataDao) {
		this.autoFinder = dataDao.getAutoFinderByPackage(this.appPackage);
	}

	public void getCoordinateMapFromDatabase(DataDao dataDao) {
		this.coordinateMap = new HashMap<>();
		for (Coordinate e : dataDao.getCoordinatesByPackage(this.appPackage)) {
			this.coordinateMap.put(e.appActivity, e);
		}
	}

	public void getWidgetSetMapFromDatabase(DataDao dataDao) {
		List<Widget> widgetList = dataDao.getWidgetsByPackage(this.appPackage);
		this.widgetSetMap = new HashMap<>();
		for (Widget w : widgetList) {
			Set<Widget> widgetSet = this.widgetSetMap.get(w.appActivity);
			if (widgetSet == null) {
				widgetSet = new HashSet<>();
				widgetSet.add(w);
				this.widgetSetMap.put(w.appActivity, widgetSet);
			} else {
				widgetSet.add(w);
			}
		}
	}
}
