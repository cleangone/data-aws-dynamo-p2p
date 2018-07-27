package fit.pay2play.data.manager;

import fit.pay2play.data.aws.dynamo.dao.ActionDao;
import fit.pay2play.data.aws.dynamo.dao.PayDao;
import fit.pay2play.data.aws.dynamo.dao.PlayDao;
import fit.pay2play.data.aws.dynamo.entity.*;
import xyz.cleangone.data.aws.dynamo.entity.lastTouched.EntityType;
import xyz.cleangone.data.cache.EntityCache;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.SECONDS;

public class Pay2PlayManager
{
    private static final Integer MAX_ENTITIES = 100;
    private static final long CACHE_LOOKUP_BYPASS_10_MINUTES = 1000 * 60 * 10;

    private static final EntityCache<Pay> PAY_CACHE_BY_USER = new EntityCache<>(P2PEntityType.PAY, MAX_ENTITIES, CACHE_LOOKUP_BYPASS_10_MINUTES);
    private static final EntityCache<Play> PLAY_CACHE_BY_USER = new EntityCache<>(P2PEntityType.PLAY, MAX_ENTITIES, CACHE_LOOKUP_BYPASS_10_MINUTES);
    private static final EntityCache<Action> ACTION_CACHE_BY_USER = new EntityCache<>(EntityType.ACTION, MAX_ENTITIES, CACHE_LOOKUP_BYPASS_10_MINUTES);

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
            pays = new ArrayList<>(payDao.getByUserId(userId));
            pays.sort(Pay::compareTo);

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
            plays = new ArrayList<>(playDao.getByUserId(userId));
            plays.sort(Play::compareTo);

            PLAY_CACHE_BY_USER.put(userId, plays, start);
        }

        return plays;
    }

    // for now, determine actions each time
    public List<Pay> getPaysWithActions(String userId)
    {
         Set<String> usedPayIds = getPayActions(userId).stream()
             .map(Action::getPayId)
             .collect(Collectors.toSet());

        List<Pay> pays = getPays(userId);
        pays.forEach(p -> p.setHasActions(usedPayIds.contains(p.getId())));

        return pays;
    }

    // for now, determine actions each time
    public List<Play> getPlaysWithActions(String userId)
    {
        Set<String> usedPlayIds = getPlayActions(userId).stream()
            .map(Action::getPlayId)
            .collect(Collectors.toSet());

        List<Play> plays = getPlays(userId);
        plays.forEach(p -> p.setHasActions(usedPlayIds.contains(p.getId())));

        return plays;
    }

    public List<Action> getActions(String userId, Date date)
    {
        return getActions(userId).stream()
            .filter(a -> a.isSameDay(date))
            .collect(Collectors.toList());
    }

    //
    // TODO - have gone back & forth on whether pay/play info should be duplicated in action
    // TODO - may want to duplicate and update actions in background if pay/play updated
    //
    public List<Action> getPopulatedActions(String userId)
    {
        return getPopulatedActions(userId, getActions(userId));
    }
    public List<Action> getPopulatedActions(String userId, Date date)
    {
        return getPopulatedActions(userId, getActions(userId, date));
    }
    private List<Action> getPopulatedActions(String userId, List<Action> actions)
    {
        // pays and plays in one map - ids are all unique
        Map<String, Play> paysAndPlaysById = getPlays(userId).stream()
            .collect(Collectors.toMap(Play::getId, Function.identity()));
        for (Pay pay : getPays(userId)) { paysAndPlaysById.put(pay.getId(), pay); }

        for (Action action : actions)
        {
            action.populate(paysAndPlaysById.get(action.getPayPlayId()));
        }

        return actions;
    }

    public List<Pay> getEnabledPays(String userId)
    {
        return getPays(userId).stream()
            .filter(Pay::getEnabled)
            .collect(Collectors.toList());
    }

    public List<Play> getEnabledPlays(String userId)
    {
        return getPlays(userId).stream()
            .filter(Play::getEnabled)
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

    public void addAction(Pay pay)
    {
        addAction(new Action(pay));
    }
    public void addAction(Play play)
    {
        addAction(new Action(play));
    }

    // create new action or combine with existing
    private void addAction(Action action)
    {
        List<Action> matchingActions = getActions(action.getUserId(), new Date()).stream()
            .filter(a -> action.samePayPlay(a))
            .collect(Collectors.toList());

        if (matchingActions.isEmpty())
        {
            save(action);
        }
        else
        {
            // should only be one, but okay if multiple
            Action matchingAction = matchingActions.get(0);

            System.out.println("Combining new Action with Action " + matchingAction.getId());
            matchingAction.setAmount(matchingAction.getAmount() + action.getAmount());
            save(matchingAction);
        }

        //        schedule(new ActionCombiner(action, actionDao));
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

        for (Action action : getPopulatedActions(userId))
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
        dayActions.sort(Comparator.comparing(DayAction::getYearAndDay));

        return dayActions;
    }

    //
    // all saves and deletes clear the cache - bypass time set to 10 minutes, so cache is
    // effectively running without checking entityLastTouched
    //
    public void save(Pay pay)
    {
        payDao.save(pay);
        PAY_CACHE_BY_USER.clear(pay.getUserId());
    }

    public void save(Play play)
    {
        playDao.save(play);
        PLAY_CACHE_BY_USER.clear(play.getUserId());
    }

    public void save(Action action)
    {
        actionDao.save(action);
        ACTION_CACHE_BY_USER.clear(action.getUserId());
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
