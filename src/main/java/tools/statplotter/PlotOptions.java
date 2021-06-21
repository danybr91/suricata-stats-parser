package tools.statplotter;

public class PlotOptions {
    private boolean enableLogScale;
    private int logScale;
    private String dateformat;

    public void setDefaults(){
        enableLogScale = false;
        logScale = 10;
        dateformat = "dd/MM/yyyy HH:mm:ss";
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
