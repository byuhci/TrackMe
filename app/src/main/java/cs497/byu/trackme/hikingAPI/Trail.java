package cs497.byu.trackme.hikingAPI;

/**
 * Model class for Hiking Project API's trail object
 * Created by Nate on 3/10/2018.
 */

public class Trail {
    private String id;
    private String name;
    private String type;
    private String summary;
    private String difficulty;
    private double stars;
    private String starVotes;
    private String location;
    private String url;
    private String imgSqSmall;
    private String imgSmall;
    private String imgSmallMed;
    private String imgMedium;
    private double length;
    private double ascent;
    private double descent;
    private double high;
    private double low;
    private double numLat;
    private double numLong;
    private String longitude;
    private String latitude;
    private String conditionStatus;
    private String conditionDetails;
    private String conditionDate;
    private double distFromLocation;

    public Trail() {
    }

    public Trail(String id, String name, String type, String summary, String difficulty,
                 double stars, String starVotes, String location, String url, String imgSqSmall,
                 String imgSmall, String imgSmallMed, String imgMedium, double length,
                 double ascent, double descent, double high, double low, String longitude, String latitude,
                 String conditionStatus, String conditionDetails, String conditionDate) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.summary = summary;
        this.difficulty = difficulty;
        this.stars = stars;
        this.starVotes = starVotes;
        this.location = location;
        this.url = url;
        this.imgSqSmall = imgSqSmall;
        this.imgSmall = imgSmall;
        this.imgSmallMed = imgSmallMed;
        this.imgMedium = imgMedium;
        this.length = length;
        this.ascent = ascent;
        this.descent = descent;
        this.high = high;
        this.low = low;
        this.longitude = longitude;
        this.latitude = latitude;
        this.conditionStatus = conditionStatus;
        this.conditionDetails = conditionDetails;
        this.conditionDate = conditionDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public double getStars() {
        return stars;
    }

    public void setStars(double stars) {
        this.stars = stars;
    }

    public String getStarVotes() {
        return starVotes;
    }

    public void setStarVotes(String starVotes) {
        this.starVotes = starVotes;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getImgSqSmall() {
        return imgSqSmall;
    }

    public void setImgSqSmall(String imgSqSmall) {
        this.imgSqSmall = imgSqSmall;
    }

    public String getImgSmall() {
        return imgSmall;
    }

    public void setImgSmall(String imgSmall) {
        this.imgSmall = imgSmall;
    }

    public String getImgSmallMed() {
        return imgSmallMed;
    }

    public void setImgSmallMed(String imgSmallMed) {
        this.imgSmallMed = imgSmallMed;
    }

    public String getImgMedium() {
        return imgMedium;
    }

    public void setImgMedium(String imgMedium) {
        this.imgMedium = imgMedium;
    }

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public double getAscent() {
        return ascent;
    }

    public void setAscent(double ascent) {
        this.ascent = ascent;
    }

    public double getDescent() {
        return descent;
    }

    public void setDescent(double descent) {
        this.descent = descent;
    }

    public double getHigh() {
        return high;
    }

    public void setHigh(double high) {
        this.high = high;
    }

    public double getLow() {
        return low;
    }

    public void setLow(double low) {
        this.low = low;
    }

    public double getLongitude() {
        return numLong;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return numLat;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getConditionStatus() {
        return conditionStatus;
    }

    public void setConditionStatus(String conditionStatus) {
        this.conditionStatus = conditionStatus;
    }

    public String getConditionDetails() {
        return conditionDetails;
    }

    public void setConditionDetails(String conditionDetails) {
        this.conditionDetails = conditionDetails;
    }

    public String getConditionDate() {
        return conditionDate;
    }

    public void setConditionDate(String conditionDate) {
        this.conditionDate = conditionDate;
    }

    public void fixCoords(double lat, double lon) {
        numLat = Double.parseDouble(latitude);
        numLong = Double.parseDouble(longitude);

        distFromLocation = Math.sqrt(Math.pow(Math.abs(numLat - lat), 2) + Math.pow(Math.abs(numLong - lon), 2));
    }
}
