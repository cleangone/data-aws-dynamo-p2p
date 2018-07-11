package fit.pay2play.data.aws.dynamo.dao;

import fit.pay2play.data.aws.dynamo.entity.Pay;
import xyz.cleangone.data.aws.dynamo.dao.DynamoBaseDao;

import java.util.List;

public class PayDao extends DynamoBaseDao<Pay>
{
    public List<Pay> getByUserId(String id)
    {
        return mapper.scan(Pay.class, buildEqualsScanExpression("UserId", id));
    }
}



