package fit.pay2play.data.manager;

import fit.pay2play.data.aws.dynamo.dao.ActionDao;
import fit.pay2play.data.aws.dynamo.entity.Action;

import java.util.List;
import java.util.stream.Collectors;

public class ActionCombiner implements Runnable
{
    private final Action action;
    private final ActionDao actionDao;

    // todo - manager can pass in list of current actions
    public ActionCombiner(Action action, ActionDao actionDao)
    {
        this.action = action;
        this.actionDao = actionDao;
    }

    public void run()
    {
        System.out.println("Checking to see if Action " + action.getId() + " can be combined with another action");

        // todo - make use of sorted list to exit early
        List<Action> actions = actionDao.getByUserId(action.getUserId());
        List<Action> prevMatchingActions = actions.stream()
            .filter(a -> action.canCombineWith(a))
            .collect(Collectors.toList());

        // should only be one, but okay if multiple
        if (!prevMatchingActions.isEmpty())
        {
            Action prevAction = prevMatchingActions.get(0);

            System.out.println("Combining Action " + action.getId() + " with Action " + prevAction.getId());
            prevAction.setAmount(prevAction.getAmount() + action.getAmount());
            actionDao.save(prevAction);

            actionDao.delete(action);
        }
    }

}
