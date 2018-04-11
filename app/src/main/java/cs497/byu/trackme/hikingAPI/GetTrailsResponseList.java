package cs497.byu.trackme.hikingAPI;

import java.util.List;

public class GetTrailsResponseList {
    private List<Trail> trails;

    public GetTrailsResponseList(List<Trail> trails) {
        this.trails = trails;
    }

    public List<Trail> getTrails() {
        return trails;
    }

    public void setTrails(List<Trail> trails) {
        this.trails = trails;
    }

    public GetTrailsResponseList fixCoords(double lat, double lon) {
        for (Trail trail : trails) {
            trail.fixCoords(lat, lon);
        }

        return this;
    }
}