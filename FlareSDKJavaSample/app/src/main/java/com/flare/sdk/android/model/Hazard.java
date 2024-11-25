package com.flare.sdk.android.model;

public class Hazard {
    private String partnerId;
    private String userId;
    private String hazardId;
    private double lat;
    private double lon;
    private String name;
    private String iconDrawableName;

    // Constructor
    public Hazard(String partnerId, String userId, String hazardId, double lat, double lon, String name, String iconDrawableName) {
        this.partnerId = partnerId;
        this.userId = userId;
        this.hazardId = hazardId;
        this.lat = lat;
        this.lon = lon;
        this.name = name;
        this.iconDrawableName = iconDrawableName;
    }

    // Getters and Setters
    public String getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(String partnerId) {
        this.partnerId = partnerId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getHazardId() {
        return hazardId;
    }

    public void setHazardId(String hazardId) {
        this.hazardId = hazardId;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIconDrawableName() {
        return iconDrawableName;
    }

    public void setIconDrawableName(String iconDrawableName) {
        this.iconDrawableName = iconDrawableName;
    }

    @Override
    public String toString() {
        return "Hazard{" +
                "partnerId='" + partnerId + '\'' +
                ", userId='" + userId + '\'' +
                ", hazardId='" + hazardId + '\'' +
                ", lat=" + lat +
                ", lon=" + lon +
                ", name='" + name + '\'' +
                ", iconDrawableName='" + iconDrawableName + '\'' +
                '}';
    }
}
