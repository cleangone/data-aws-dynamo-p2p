package fit.pay2play.data.aws.dynamo.entity;

import java.util.Calendar;
import java.util.Date;

// holds/sums both pay and play for a specific day.  Action is either pay or play
public class DayAction
{
    private String yearAndDay;
    private Date date;
    private double pay = 0;
    private double play = 0;

    public DayAction(Action action)
    {
        date = action.getCreatedDate();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        yearAndDay = calendar.get(Calendar.YEAR) + "-" + calendar.get(Calendar.DAY_OF_YEAR);

        if (action.isActionType(ActionType.Pay)) { pay = action.getTotalValue().doubleValue(); }
        else if (action.isActionType(ActionType.Play)) { play = action.getTotalValue().doubleValue(); }
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


