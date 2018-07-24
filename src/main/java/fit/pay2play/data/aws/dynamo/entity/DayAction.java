package fit.pay2play.data.aws.dynamo.entity;

import java.util.Calendar;
import java.util.Date;

public class DayAction
{
    private String yearAndDay;
    private Date date;
    private double pay = 0;
    private double play = 0;

    private Calendar calendar = Calendar.getInstance();

    public DayAction(Action action)
    {
        date = action.getCreatedDate();

        calendar.setTime(date);
        yearAndDay = calendar.get(Calendar.YEAR) + "-" + calendar.get(Calendar.DAY_OF_YEAR);

        if (action.isPay()) { pay = action.getTotalValue().doubleValue(); }
        else { play = action.getTotalValue().doubleValue(); }
    }

    public void add(DayAction that)
    {
        pay += that.getPay();
        play += that.getPlay();
    }

    public Date getDate()
    {
        return date;
    }
    public long getTime()
    {
        return date.getTime();
    }
    public String getYearAndDay()
    {
        return yearAndDay;
    }
    public double getPay()
    {
        return pay;
    }
    public double getPlay()
    {
        return play;
    }
}


