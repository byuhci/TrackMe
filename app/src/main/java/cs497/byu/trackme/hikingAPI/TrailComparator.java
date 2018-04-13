package cs497.byu.trackme.hikingAPI;

import java.util.Comparator;

public class TrailComparator implements Comparator<Trail> {

    double latitude;
    double longitude;

    public TrailComparator(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public int compare(Trail t1, Trail t2) {
        if (t1.getDistFromUser() <= 100 && t2.getDistFromUser() <= 100)
            return compareByVotes(t1, t2);
        else {
            if (t1.getDistFromUser() - t2.getDistFromUser() >= 100 || t2.getDistFromUser() - t1.getDistFromUser() >= 100) {
                if(Double.compare(t1.getDistFromUser(), t2.getDistFromUser()) == 0)
                    return compareByVotes(t1, t2);
                else return Double.compare(t1.getDistFromUser(), t2.getDistFromUser());
            } else {
                return compareByVotes(t1, t2);
            }
        }
    }

    private int compareByVotes(Trail t1, Trail t2) {
        if (t1.getStarVotes() > t2.getStarVotes())
            return -1;
        else if (t1.getStarVotes() < t2.getStarVotes())
            return 1;
        else {
            if (t1.getStars() > t2.getStars())
                return -1;
            else if (t1.getStars() < t2.getStars())
                return 1;
            else
                return Double.compare(t1.getLength(), t2.getLength());

        }
    }


}
