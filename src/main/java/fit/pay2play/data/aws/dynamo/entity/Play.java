package fit.pay2play.data.aws.dynamo.entity;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import xyz.cleangone.data.aws.dynamo.entity.base.BaseNamedEntity;
import xyz.cleangone.data.aws.dynamo.entity.base.EntityField;

import java.math.BigDecimal;

@DynamoDBTable(tableName = "Play")
public class Play extends BaseNamedEntity
{
    public static final EntityField VALUE_FIELD = new EntityField("play.value", "Value");
    public static final EntityField PLURAL_NAME_FIELD = new EntityField("play.pluralName", "Name (Plural)");

    private String userId;
    private BigDecimal value;
    protected String pluralName;

    public Play() {}
    public Play(String name, String userId)
    {
        super(name);
        setUserId(userId);
    }

    @DynamoDBIgnore public String getDisplayValue()
    {
        return value == null ? "" : value.toString();
    }

    public String get(EntityField field)
    {
        if (PLURAL_NAME_FIELD.equals(field)) { return getPluralName(); }
        else { return super.get(field); }
    }

    public void set(EntityField field, String value)
    {
        if (PLURAL_NAME_FIELD.equals(field)) { this.setPluralName(value); }
        else { super.set(field, value); }
    }

    public BigDecimal getBigDecimal(EntityField field)
    {
        if (VALUE_FIELD.equals(field)) return getValue();
        else return super.getBigDecimal(field);
    }

    public void setBigDecimal(EntityField field, BigDecimal value)
    {
        if (VALUE_FIELD.equals(field)) setValue(value);
        else super.setBigDecimal(field, value);
    }

    @DynamoDBAttribute(attributeName = "UserId")
    public String getUserId()
    {
        return userId;
    }
    public void setUserId(String userId)
    {
        this.userId = userId;
    }

    @DynamoDBAttribute(attributeName = "Value")
    public BigDecimal getValue()
    {
        return value;
    }
    public void setValue(BigDecimal value)
    {
        this.value = value;
    }

    @DynamoDBAttribute(attributeName = "PluralName")
    public String getPluralName()
    {
        return pluralName;
    }
    public void setPluralName(String pluralName)
    {
        this.pluralName = pluralName;
    }
}



