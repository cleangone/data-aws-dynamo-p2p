package fit.pay2play.data.manager;

import fit.pay2play.data.aws.dynamo.dao.ActionDao;
import fit.pay2play.data.aws.dynamo.dao.ActionCategoryDao;
import fit.pay2play.data.aws.dynamo.entity.*;
import xyz.cleangone.data.aws.dynamo.entity.lastTouched.EntityType;
import xyz.cleangone.data.cache.EntityCache;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.SECONDS;

public class Pay2PlayManager
{
    private static final Integer MAX_ENTITIES = 100;
    private static final long CACHE_LOOKUP_BYPASS_10_MINUTES = 1000 * 60 * 10;

    private static final EntityCache<ActionCategory> ACTION_CATEGORY_CACHE_BY_USER = new EntityCache<>(P2PEntityType.ACTION_CATEGORY, MAX_ENTITIES, CACHE_LOOKUP_BYPASS_10_MINUTES);
    private static final EntityCache<Action> ACTION_CACHE_BY_USER = new EntityCache<>(EntityType.ACTION, MAX_ENTITIES, CACHE_LOOKUP_BYPASS_10_MINUTES);

    private final ActionCategoryDao actionCategoryDao = new ActionCategoryDao();
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

    public List<ActionCategory> getActionCategories(String userId)
    {
        Date start = new Date();
        List<ActionCategory> actionTypes = ACTION_CATEGORY_CACHE_BY_USER.get(userId);
        if (actionTypes == null)
        {
            actionTypes = new ArrayList<>(actionCategoryDao.getByUserId(userId));
            actionTypes.sort(ActionCategory::compareTo);

            ACTION_CATEGORY_CACHE_BY_USER.put(userId, actionTypes, start);
        }

        return actionTypes;
    }

    public List<ActionCategory> getActionCategories(String userId, ActionType actionType)
    {
        return getActionCategories(userId).stream()
            .filter(a -> a.isActionType(actionType))
            .collect(Collectors.toList());
    }

    public Set<String> getActiveActionCategoryIds(String userId)
    {
        return getActions(userId).stream()
            .map(Action::getActionCategoryId)
            .collect(Collectors.toSet());
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

    // todo - need to add sep adjustment after anything with a min/max
    public List<Action> getActionsWithAdjustments(String userId, Date date)
    {
        List<Action> allActions = new ArrayList<>();

        List<Action> actions = getPopulatedActions(userId, getActions(userId, date));
        for (Action action : actions)
        {
            allActions.add(action);
            Action adjustment = action.getAdjustment();
            if (adjustment != null) { allActions.add(adjustment); }
        }

        return allActions;
    }


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
        Map<String, ActionCategory> actionTypesById = getActionCategories(userId).stream()
            .collect(Collectors.toMap(ActionCategory::getId, Function.identity()));

        for (Action action : actions)
        {
            if (action.getActionCategoryId() != null)
            {
                action.populate(actionTypesById.get(action.getActionCategoryId()));
            }
        }

        return actions;
    }

    public void createActionCategory(String name, String userId, ActionType actionType)
    {
        save(new ActionCategory(name, userId, actionType));
    }

    public List<ActionCategory> getEnabledActionCategories(String userId, ActionType actionType)
    {
        return getActionCategories(userId, actionType).stream()
            .filter(ActionCategory::getEnabled)
            .collect(Collectors.toList());
    }

    private List<Action> getActions(String userId, ActionType ActionType)
    {
        return getActions(userId).stream()
            .filter(a -> a.isActionType(ActionType))
            .collect(Collectors.toList());
    }

    // create new action or combine with existing
    public void addAction(ActionCategory actionCategory)
    {
        Action action = new Action(actionCategory);

        List<Action> matchingActions = getActions(action.getUserId(), new Date()).stream()
            .filter(a -> action.sameActionType(a))
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

        // schedule(new ActionCombiner(action, actionDao));
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
            DayAction dayAction = new DayAction(action);
            if (dayActionByYearDay.containsKey(dayAction.getYearAndDay()))
            {
                dayActionByYearDay.get(dayAction.getYearAndDay()).add(dayAction);
            }
            else
            {
                dayActionByYearDay.put(dayAction.getYearAndDay(), dayAction);
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
    public void save(ActionCategory actionCategory)
    {
        actionCategoryDao.save(actionCategory);
        ACTION_CATEGORY_CACHE_BY_USER.clear(actionCategory.getUserId());
    }

    public void save(Action action)
    {
        actionDao.save(action);
        ACTION_CACHE_BY_USER.clear(action.getUserId());
    }

    public void delete(ActionCategory actionCategory)
    {
        actionCategoryDao.delete(actionCategory);
        ACTION_CATEGORY_CACHE_BY_USER.clear(actionCategory.getUserId());
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
