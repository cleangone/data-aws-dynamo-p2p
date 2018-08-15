package fit.pay2play.data.aws.dynamo.dao;

import fit.pay2play.data.aws.dynamo.entity.ActionCategory;
import fit.pay2play.data.aws.dynamo.entity.P2PEntityType;
import xyz.cleangone.data.aws.dynamo.dao.CachingDao;

import java.util.List;

public class ActionCategoryDao extends CachingDao<ActionCategory>
{
    public List<ActionCategory> getByUserId(String id)
    {
        return mapper.scan(ActionCategory.class, buildEqualsScanExpression("UserId", id));
    }

    public void save(ActionCategory actionCategory)
    {
        super.save(actionCategory);
        setEntityLastTouched(actionCategory.getUserId(), P2PEntityType.ACTION_CATEGORY);
    }

    public void delete(ActionCategory actionCategory)
    {
        super.delete(actionCategory);
        setEntityLastTouched(actionCategory.getUserId(), P2PEntityType.ACTION_CATEGORY);
    }
}



