package fit.pay2play.data.aws.dynamo.entity;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConvertedEnum;
import xyz.cleangone.data.aws.dynamo.entity.base.BaseNamedEntity;
import xyz.cleangone.data.aws.dynamo.entity.base.EntityField;

import java.math.BigDecimal;
import java.util.Objects;

@DynamoDBTable(tableName = "ActionCategory")
public class ActionCategory extends BaseNamedEntity
{
    public static final EntityField VALUE_FIELD = new EntityField("play.value", "Value");
    public static final EntityField SHORT_NAME_FIELD = new EntityField("play.shortName", "Short Name");
    public static final EntityField PLURAL_NAME_FIELD = new EntityField("play.pluralName", "Name (Plural)");
    public static final EntityField TARGET_AMOUNT_FIELD = new EntityField("play.targetAmount", "Target Amount");
    public static final EntityField TARGET_MIN_AMOUNT_FIELD = new EntityField(TARGET_AMOUNT_FIELD, "Target Min Amount");
    public static final EntityField TARGET_MAX_AMOUNT_FIELD = new EntityField(TARGET_AMOUNT_FIELD, "Target Max Amount");
    public static final EntityField DISPLAY_ORDER_FIELD = new EntityField("play.displayOrder", "Display Order");

    private String userId;
    private ActionType actionType;
    private BigDecimal value;
    private String shortName;
    private String pluralName;
    private String displayOrder;
    private Integer targetAmount;

    public ActionCategory() {}
    public ActionCategory(String name, String userId, ActionType actionType)
    {
        super(Objects.requireNonNull(name));
        setUserId(Objects.requireNonNull(userId));
        setActionType(Objects.requireNonNull(actionType));
    }

    @DynamoDBIgnore public String getDisplayShortName()
    {
        return shortName == null ? getName() : shortName;
    }
    @DynamoDBIgnore public String getDisplayValue()
    {
        return value == null ? "" : value.toString();
    }
    @DynamoDBIgnore public boolean isActionType(ActionType actionType)
    {
        return this.actionType == actionType;
    }

    // return -1, 0 or 1 as that is gt, equal to, or lt this
    @DynamoDBIgnore public int compareTo(ActionCategory that)
    {
        if (displayOrder == null) { return that.getDisplayOrder() == null ? 0 : 1; }
        else if (that.getDisplayOrder() == null) { return -1; }
        else { return displayOrder.compareToIgnoreCase(that.getDisplayOrder()); }
    }

    public String get(EntityField field)
    {
        if (SHORT_NAME_FIELD.equals(field)) { return getShortName(); }
        else if (PLURAL_NAME_FIELD.equals(field)) { return getPluralName(); }
        else if (DISPLAY_ORDER_FIELD.equals(field)) { return getDisplayOrder(); }
        else { return super.get(field); }
    }

    public void set(EntityField field, String value)
    {
        if (SHORT_NAME_FIELD.equals(field)) { this.setShortName(value); }
        else if (PLURAL_NAME_FIELD.equals(field)) { this.setPluralName(value); }
        else if (DISPLAY_ORDER_FIELD.equals(field)) { this.setDisplayOrder(value); }
        else { super.set(field, value); }
    }

    public Integer getInteger(EntityField field)
    {
        if (TARGET_AMOUNT_FIELD.equals(field)) return getTargetAmount();
        else return super.getInteger(field);
    }

    public void setInteger(EntityField field, Integer value)
    {
        if (TARGET_AMOUNT_FIELD.equals(field)) setTargetAmount(value);
        else super.setInteger(field, value);
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

    @DynamoDBTypeConvertedEnum
    @DynamoDBAttribute(attributeName="ActionType")
    public ActionType getActionType()
    {
        return actionType;
    }
    public void setActionType(ActionType actionType)
    {
        this.actionType = actionType;
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

    @DynamoDBAttribute(attributeName = "ShortName")
    public String getShortName()
    {
        return shortName;
    }
    public void setShortName(String shortName)
    {
        this.shortName = shortName;
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

    @DynamoDBAttribute(attributeName = "DisplayOrder")
    public String getDisplayOrder()
    {
        return displayOrder;
    }
    public void setDisplayOrder(String displayOrder)
    {
        this.displayOrder = displayOrder;
    }

    @DynamoDBAttribute(attributeName = "TargetAmount")
    public Integer getTargetAmount()
    {
        return targetAmount;
    }
    public void setTargetAmount(Integer targetAmount)
    {
        this.targetAmount = targetAmount;
    }
}



