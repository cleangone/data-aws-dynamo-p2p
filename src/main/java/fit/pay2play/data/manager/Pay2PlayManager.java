package fit.pay2play.data.manager;

import fit.pay2play.data.aws.dynamo.dao.ActionDao;
import fit.pay2play.data.aws.dynamo.dao.PayDao;
import fit.pay2play.data.aws.dynamo.dao.PlayDao;
import fit.pay2play.data.aws.dynamo.entity.Action;
import fit.pay2play.data.aws.dynamo.entity.Pay;
import fit.pay2play.data.aws.dynamo.entity.Play;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public class Pay2PlayManager
{
    // todo - need to extract org from cache, move it to base
//    public static final EntityCache<P2pAction> ACTION_CACHE_BY_USER = new EntityCache<>(EntityType.Action, 100);

    private final PayDao payDao = new PayDao();
    private final PlayDao playDao = new PlayDao();
    private final ActionDao actionDao = new ActionDao();

    public List<Action> getActions(String userId)
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

    public List<Pay> getPays(String userId)
    {
        return payDao.getByUserId(userId);
    }
    public List<Play> getPlays(String userId)
    {
        return playDao.getByUserId(userId);
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

    public void createPayAction(Pay pay, int amount)
    {
        save(new Action(pay, amount));
    }
    public void createPlayAction(Play play, int amount)
    {
        save(new Action(play, amount));
    }

    public static BigDecimal sumTotalValue(List<Action> actions)
    {
        return actions.stream()
            .map(Action::getTotalValue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
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
    }
    public void delete(Play play)
    {
        playDao.delete(play);
    }
}
