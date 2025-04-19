package com.wormfarm.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EventMapModel {
    private final List<EventMarker> markers = new ArrayList<>();

    public void addMarker(EventMarker marker) {
        markers.add(marker);
    }

    public List<EventMarker> getMarkers() {
        return Collections.unmodifiableList(markers);
    }
}