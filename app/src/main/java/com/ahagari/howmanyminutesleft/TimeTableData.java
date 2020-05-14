package com.ahagari.howmanyminutesleft;

import java.io.Serializable;

public class TimeTableData implements Serializable {
    public static final long serialVersionUID = 2L;
    public int hour;
    public String destination;
    public long jikoku;
    public int minute;
    public String type;

    public TimeTableData() {

    }
    public TimeTableData(int i, int j, String s, String s1)
    {
        hour = i;
        minute = j;
        type = s;
        destination = s1;
        jikoku = i * 3600 + j * 60;
        if(i < 3)
            jikoku = jikoku + 0x15180L;
    }

    public int getHour()
    {
        return hour;
    }

    public String getDestination()
    {
        return destination;
    }

    public long getJikoku()
    {
        return jikoku;
    }

    public int getMinute()
    {
        return minute;
    }

    public String getType()
    {
        return type;
    }

    public void setHour(int i)
    {
        hour = i;
    }

    public void setDestination(String s)
    {
        destination = s;
    }

    public void setJikoku(long l)
    {
        jikoku = l;
    }

    public void setMinute(int i)
    {
        minute = i;
    }

    public void setType(String s)
    {
        type = s;
    }


}
