package fit.pay2play.data.aws.dynamo.dao;

import fit.pay2play.data.aws.dynamo.entity.Play;
import xyz.cleangone.data.aws.dynamo.dao.DynamoBaseDao;

import java.util.List;

public class PlayDao extends DynamoBaseDao<Play>
{
    public List<Play> getByUserId(String id)
    {
        return mapper.scan(Play.class, byUserId(id));
    }
}



