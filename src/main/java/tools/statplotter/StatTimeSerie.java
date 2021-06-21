package tools.statplotter;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public class StatTimeSerie implements Comparable<StatTimeSerie> {
    private String name;
    private Map<Time, Long> data;

    private Long maxValue;
    private Long minValue;

    public StatTimeSerie(String name){
        this.name = name;
        data = new HashMap<>();
        maxValue = Long.MIN_VALUE;
        minValue = Long.MAX_VALUE;
    }

    public String getName() {
        return name;
    }

    public void addValue(Time time, Long value){
        data.put(time, value);
        if (maxValue < value){
            maxValue = value;
        }
        if (minValue > value){
            minValue = value;
        }
    }

    public Collection<Time> getTimes(){
        return data.keySet();
    }

    public Collection<Long> getValues() {
        return data.values();
    }

    public Long getValue(Time time){
        return data.get(time);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StatTimeSerie statTimeSerie = (StatTimeSerie) o;
        return Objects.equals(name, statTimeSerie.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    public Long getMaxValue() {
        return maxValue;
    }

    public Long getMinValue() {
        return minValue;
    }

    @Override
    public int compareTo(@NotNull StatTimeSerie o) {
        return name.compareTo(o.getName());
    }
}
