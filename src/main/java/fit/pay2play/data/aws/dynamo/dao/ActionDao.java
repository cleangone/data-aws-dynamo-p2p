package fit.pay2play.data.aws.dynamo.dao;

import fit.pay2play.data.aws.dynamo.entity.Action;
import xyz.cleangone.data.aws.dynamo.dao.CachingDao;
import xyz.cleangone.data.aws.dynamo.entity.lastTouched.EntityType;

import java.util.List;

public class ActionDao extends CachingDao<Action>
{
    public List<Action> getByUserId(String id)
    {
        return mapper.scan(Action.class, buildEqualsScanExpression("UserId", id));
    }

    public void save(Action action)
    {
        super.save(action);
        setEntityLastTouched(action.getUserId(), EntityType.ACTION);
    }

    public void delete(Action action)
    {
        super.delete(action);
        setEntityLastTouched(action.getUserId(), EntityType.ACTION);
    }
}



