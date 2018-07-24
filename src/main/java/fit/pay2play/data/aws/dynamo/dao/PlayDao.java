package fit.pay2play.data.aws.dynamo.dao;

import fit.pay2play.data.aws.dynamo.entity.P2PEntityType;
import fit.pay2play.data.aws.dynamo.entity.Play;
import xyz.cleangone.data.aws.dynamo.dao.CachingDao;

import java.util.List;

public class PlayDao extends CachingDao<Play>
{
    public List<Play> getByUserId(String id)
    {
        return mapper.scan(Play.class, buildEqualsScanExpression("UserId", id));
    }

    public void save(Play play)
    {
        super.save(play);
        setEntityLastTouched(play.getUserId(), P2PEntityType.PLAY);
    }

    public void delete(Play play)
    {
        super.delete(play);
        setEntityLastTouched(play.getUserId(), P2PEntityType.PLAY);
    }
}



