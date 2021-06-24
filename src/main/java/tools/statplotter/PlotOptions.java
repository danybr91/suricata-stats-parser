package tools.statplotter;

import java.util.TimeZone;

public class PlotOptions {
    private boolean enableLogScale;
    private int logScale;
    private String dateformat;
    private TimeZone timeZone;

    public void setDefaults(){
        enableLogScale = false;
        logScale = 10;
        dateformat = "HH:mm:ss z";
        timeZone = TimeZone.getTimeZone("UTC");
    }

    public PlotOptions(){
        setDefaults();
    }

    public boolean isLogScaleEnabled() {
        return enableLogScale;
    }

    public void setLogAxis(boolean enableLogScale) {
        this.enableLogScale = enableLogScale;
    }

    public String getDateformat() {
        return dateformat;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    public void setDateformat(String dateformat) {
        this.dateformat = dateformat;
    }

    public int getLogScale() {
        return logScale;
    }

    public void setLogScale(int logScale) {
        this.logScale = logScale;
    }
}
