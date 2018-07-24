package fit.pay2play.data.manager;

import fit.pay2play.data.aws.dynamo.dao.ActionDao;
import fit.pay2play.data.aws.dynamo.dao.PayDao;
import fit.pay2play.data.aws.dynamo.dao.PlayDao;
import fit.pay2play.data.aws.dynamo.entity.Action;
import fit.pay2play.data.aws.dynamo.entity.DayAction;
import fit.pay2play.data.aws.dynamo.entity.Pay;
import fit.pay2play.data.aws.dynamo.entity.Play;
import xyz.cleangone.data.aws.dynamo.entity.lastTouched.EntityType;
import xyz.cleangone.data.cache.EntityCache;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.SECONDS;

public class Pay2PlayManager
{
    public static final EntityCache<Pay> PAY_CACHE_BY_USER = new EntityCache<>(EntityType.ACTION, 100);
    public static final EntityCache<Play> PLAY_CACHE_BY_USER = new EntityCache<>(EntityType.ACTION, 100);
    public static final EntityCache<Action> ACTION_CACHE_BY_USER = new EntityCache<>(EntityType.ACTION, 100);

    private final PayDao payDao = new PayDao();
    private final PlayDao playDao = new PlayDao();
    private final ActionDao actionDao = new ActionDao();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    // todo verify default sort is latest first
    public List<Action> getActions(String userId)
    {
        Date start = new Date();
        List<Action> actions = ACTION_CACHE_BY_USER.get(userId);
        if (actions == null)
        {
            actions = actionDao.getByUserId(userId);
            ACTION_CACHE_BY_USER.put(userId, actions, start);
        }

        return actions;
    }

    public List<Pay> getPays(String userId)
    {
        Date start = new Date();
        List<Pay> pays = PAY_CACHE_BY_USER.get(userId);
        if (pays == null)
        {
            pays = payDao.getByUserId(userId);
            PAY_CACHE_BY_USER.put(userId, pays, start);
        }

        return pays;
    }

    public List<Play> getPlays(String userId)
    {
        Date start = new Date();
        List<Play> plays = PLAY_CACHE_BY_USER.get(userId);
        if (plays == null)
        {
            plays = playDao.getByUserId(userId);
            PLAY_CACHE_BY_USER.put(userId, plays, start);
        }

        return plays;
    }

    public List<Action> getActions(String userId, Date date)
    {
        return getActions(userId).stream()
            .filter(a -> a.isSameDay(date))
            .collect(Collectors.toList());
    }

    public List<Action> getPayActions(String userId)
    {
        return getActions(userId, Action::isPay);
    }
    public List<Action> getPlayActions(String userId)
    {
        return getActions(userId, Action::isPlay);
    }
    private List<Action> getActions(String userId, Predicate<Action> predicate)
    {
        return getActions(userId).stream()
            .filter(predicate)
            .collect(Collectors.toList());
    }

    public void createAction(Pay pay)
    {
        create(new Action(pay));
    }
    public void createAction(Play play)
    {
        create(new Action(play));
    }

    private void create(Action action)
    {
        save(action);
        schedule(new ActionCombiner(action, actionDao));
    }

    public static BigDecimal sumTotalValue(List<Action> actions)
    {
        return actions.stream()
            .map(Action::getTotalValue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<DayAction> getDayActions(String userId)
    {
        Map<String, DayAction> dayActionByYearDay = new HashMap<>();

        for (Action action : getActions(userId))
        {
            DayAction data = new DayAction(action);
            if (dayActionByYearDay.containsKey(data.getYearAndDay()))
            {
                dayActionByYearDay.get(data.getYearAndDay()).add(data);
            }
            else
            {
                dayActionByYearDay.put(data.getYearAndDay(), data);
            }
        }

        ArrayList<DayAction> dayActions = new ArrayList<>(dayActionByYearDay.values());
        Collections.sort(dayActions, (d1, d2) -> d1.getYearAndDay().compareTo(d2.getYearAndDay()));
        return dayActions;
    }

    public void save(Pay pay)
    {
        payDao.save(pay);
    }
    public void save(Play play)
    {
        playDao.save(play);
    }
    public void save(Action action)
    {
        actionDao.save(action);
    }

    public void delete(Pay pay)
    {
        payDao.delete(pay);
        PAY_CACHE_BY_USER.clear(pay.getUserId());
    }

    public void delete(Play play)
    {
        playDao.delete(play);
        PLAY_CACHE_BY_USER.clear(play.getUserId());
    }

    public void delete(Action action)
    {
        actionDao.delete(action);
        ACTION_CACHE_BY_USER.clear(action.getUserId());
    }

     private void schedule(Runnable runnable)
    {
        scheduler.schedule(runnable, 1, SECONDS);
    }
}
