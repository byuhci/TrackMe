package cs497.byu.trackme.model;

import android.os.Bundle;

/**
 * Created by mitchclements on 11/8/17.
 */

public class ProfileData {
    private static final ProfileData ourInstance = new ProfileData();

    public static ProfileData getInstance() {
        return ourInstance;
    }

    private ProfileData() {
    }

    public enum USER {HIKER, OBSERVER}

    public USER userType;

    public USER getUserType() {
        return userType;
    }

    public void setUserType(USER userType) {
        this.userType = userType;
    }
}
