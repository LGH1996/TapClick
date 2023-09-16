package com.lgh.advertising.going.mybean;

import java.util.ArrayList;
import java.util.List;

public class Regulation {
    public AppDescribe appDescribe;
    public AutoFinder autoFinder;
    public List<Coordinate> coordinateList;
    public List<Widget> widgetList;

    public Regulation() {
        coordinateList = new ArrayList<>();
        widgetList = new ArrayList<>();
    }
}
