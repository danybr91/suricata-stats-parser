package tools.statplotter;

import java.util.Date;
import java.util.Objects;

public class Time {
    private Date date;
    private long uptime;

    public Time(long uptime){
        this.uptime = uptime;
    }

    public Time(Date date, long uptime){
        this.date = date;
        this.uptime = uptime;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public long getUptime() {
        return uptime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Time time = (Time) o;
        return uptime == time.uptime;
    }

    @Override
    public int hashCode() {
        return Objects.hash(uptime);
    }
}
