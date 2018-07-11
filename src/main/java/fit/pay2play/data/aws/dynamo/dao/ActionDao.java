package fit.pay2play.data.aws.dynamo.dao;

import fit.pay2play.data.aws.dynamo.entity.Action;
import xyz.cleangone.data.aws.dynamo.dao.DynamoBaseDao;

import java.util.List;

public class ActionDao extends DynamoBaseDao<Action>
{
    public List<Action> getByUserId(String id)
    {
        return mapper.scan(Action.class, buildEqualsScanExpression("UserId", id));
    }
}



