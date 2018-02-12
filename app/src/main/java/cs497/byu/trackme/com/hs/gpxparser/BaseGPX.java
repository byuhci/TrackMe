package cs497.byu.trackme.com.hs.gpxparser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TimeZone;

import cs497.byu.trackme.com.hs.gpxparser.extension.IExtensionParser;


class BaseGPX {

    final SimpleDateFormat xmlDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    final ArrayList<IExtensionParser> extensionParsers = new ArrayList<>();

    BaseGPX() {
        // TF, 20170515: iso6801 dates are always in GMT timezone
        xmlDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    /**
     * Adds a new extension parser to be used when parsing a gpx steam
     *
     * @param parser an instance of a {@link IExtensionParser} implementation
     */
    public void addExtensionParser(IExtensionParser parser) {
        this.extensionParsers.add(parser);
    }

    /**
     * Removes an extension parser previously added
     *
     * @param parser an instance of a {@link IExtensionParser} implementation
     */
    public void removeExtensionParser(IExtensionParser parser) {
        this.extensionParsers.remove(parser);
    }
}
