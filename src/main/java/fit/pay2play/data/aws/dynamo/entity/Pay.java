package fit.pay2play.data.aws.dynamo.entity;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import xyz.cleangone.data.aws.dynamo.entity.base.BaseNamedEntity;
import xyz.cleangone.data.aws.dynamo.entity.base.EntityField;

@DynamoDBTable(tableName = "Pay")
public class Pay extends Play
{
    public static final EntityField REQUIRED_FIELD = new EntityField("pay.required", "Required");

    private boolean isRequired;

    public Pay() {}
    public Pay(String name, String userId)
    {
        super(name, userId);
    }

    public boolean getBoolean(EntityField field)
    {
        if (REQUIRED_FIELD.equals(field)) return isRequired();
        else return super.getBoolean(field);
    }

    public void setBoolean(EntityField field, boolean value)
    {
        if (REQUIRED_FIELD.equals(field)) setRequired(value);
        else super.setBoolean(field, value);
    }

    @DynamoDBAttribute(attributeName = "IsRequired")
    public boolean isRequired()
    {
        return isRequired;
    }
    public void setRequired(boolean required)
    {
        isRequired = required;
    }
}



