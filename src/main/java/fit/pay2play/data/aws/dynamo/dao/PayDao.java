package fit.pay2play.data.aws.dynamo.dao;

import fit.pay2play.data.aws.dynamo.entity.P2PEntityType;
import fit.pay2play.data.aws.dynamo.entity.Pay;
import xyz.cleangone.data.aws.dynamo.dao.CachingDao;

import java.util.List;

public class PayDao extends CachingDao<Pay>
{
    public List<Pay> getByUserId(String id)
    {
        return mapper.scan(Pay.class, buildEqualsScanExpression("UserId", id));
    }

    public void save(Pay pay)
    {
        super.save(pay);
        setEntityLastTouched(pay.getUserId(), P2PEntityType.PAY);
    }

    public void delete(Pay pay)
    {
        super.delete(pay);
        setEntityLastTouched(pay.getUserId(), P2PEntityType.PAY);
    }
}



