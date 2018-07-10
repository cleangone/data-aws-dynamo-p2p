package fit.pay2play.data.aws.dynamo.dao;

import fit.pay2play.data.aws.dynamo.entity.P2pAction;
import xyz.cleangone.data.aws.dynamo.dao.DynamoBaseDao;

import java.util.List;

public class P2pActionDao extends DynamoBaseDao<P2pAction>
{
    public List<P2pAction> getByUserId(String id)
    {
        return mapper.scan(P2pAction.class, byUserId(id));
    }
}



