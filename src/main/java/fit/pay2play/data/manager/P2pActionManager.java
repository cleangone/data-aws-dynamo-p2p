package fit.pay2play.data.manager;

import fit.pay2play.data.aws.dynamo.dao.P2pActionDao;
import fit.pay2play.data.aws.dynamo.entity.P2pAction;
import fit.pay2play.data.aws.dynamo.entity.Pay;
import fit.pay2play.data.aws.dynamo.entity.Play;
import xyz.cleangone.data.aws.dynamo.entity.base.EntityType;
import xyz.cleangone.data.cache.EntityCache;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public class P2pActionManager
{
    // todo - need to extract org from cache
    public static final EntityCache<P2pAction> ACTION_CACHE_BY_USER = new EntityCache<>(EntityType.Action, 100);

    private final P2pActionDao actionDao = new P2pActionDao();

    public List<P2pAction> getActions(String userId)
    {
        return actionDao.getByUserId(userId);

//        Date start = new Date();
//        List<P2pAction> actions = ACTION_CACHE_BY_USER.get(targetEvent, org.getId());
//        if (actions == null)
//        {
//            actions = actionDao.getByUserId(userId);
//            ACTION_CACHE_BY_USER.put(targetEvent, actions, org.getId(), start);
//        }
//
//        return actions;
    }

    public List<P2pAction> getPay(String userId)
    {
        return getActions(userId, P2pAction::isPay);
    }
    public List<P2pAction> getPlay(String userId)
    {
        return getActions(userId, P2pAction::isPlay);
    }
    private List<P2pAction> getActions(String userId, Predicate<P2pAction> predicate)
    {
        return getActions(userId).stream()
            .filter(predicate)
            .collect(Collectors.toList());
    }

    public void createPay(Pay pay, int amount)
    {
        save(new P2pAction(pay, amount));
    }
    public void createPlay(Play play, int amount)
    {
        save(new P2pAction(play, amount));
    }

    public static BigDecimal sumTotalValue(List<P2pAction> actions)
    {
        return actions.stream()
            .map(P2pAction::getTotalValue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void save(P2pAction action)
    {
        actionDao.save(action);
    }
}
