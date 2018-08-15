package fit.pay2play.data.aws.dynamo.entity;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import xyz.cleangone.data.aws.dynamo.entity.base.BaseEntity;
import xyz.cleangone.data.aws.dynamo.entity.base.EntityField;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Date;

import static fit.pay2play.data.aws.dynamo.entity.ActionType.isPay;
import static java.util.Objects.requireNonNull;

@DynamoDBTable(tableName="Action")
public class Action extends BaseEntity
{
    public static final EntityField DESC_FIELD = new EntityField("action.description", "Description");
    public static final EntityField AMOUNT_FIELD = new EntityField("action.amount", "Amount");
    public static final EntityField TOTAL_VALUE_FIELD = new EntityField("action.totalValue", "Total Value");

    private static MathContext TWO_DIGITS = new MathContext(2, RoundingMode.HALF_UP);

    private String userId;
    private String actionCategoryId;
    private int amount;

    private ActionCategory actionCategory; // transient
    private ActionType actionType; // transient - overrides ActionCategory.actionType for adjustments
//    private boolean isAdjustment; // transient  // todo - ugly

    public Action()
    {
        super();
    }

    public Action(ActionCategory actionCategory)
    {
        super();
        setUserId(requireNonNull(actionCategory).getUserId());
        setActionCategoryId(actionCategory.getId());
        setAmount(1);
        setActionCategory(actionCategory);
    }

    @DynamoDBIgnore
    public void setActionCategory(ActionCategory actionCategory)
    {
        if (actionCategory == null) { return; }
        if (!actionCategoryId.equals(actionCategory.getId())) { throw new RuntimeException("Cannot populate Action with non-matching ActionType"); }

        this.actionCategory = actionCategory;
        setActionType(actionCategory.getActionType());
    }

    @DynamoDBIgnore public String getDescription()
    {
        // todo - semi-ugly
        if (ActionType.isAdjustment(actionType))
        {
            return getName() + " - Target Min Amount: " + actionCategory.getTargetAmount();
        }

        return amount + " " + (amount == 1 || getPluralName() == null ? getName() : getPluralName());
    }

    @DynamoDBIgnore public Action getAdjustment()
    {
        if (!actionCategory.isActionType(ActionType.Pay) ||
            !hasTargetAmount() ||
            amount >= actionCategory.getTargetAmount())
        {
           return null;
        }

        Action adjustment = new Action(actionCategory);
        adjustment.setCreatedDate(getCreatedDate());
        adjustment.setUpdatedDate(getUpdatedDate());
        adjustment.setAmount(amount - actionCategory.getTargetAmount());
        adjustment.setActionType(ActionType.PayAdjust);

        return adjustment;
    }

    @DynamoDBIgnore public BigDecimal getTotalValue()
    {
        return getValue().multiply(new BigDecimal(getCreditAmount()), TWO_DIGITS);
    }

    @DynamoDBIgnore public int getCreditAmount()
    {
        return isPay(actionType) ? amount : amount * -1;
    }
    @DynamoDBIgnore public boolean hasTargetAmount()
    {
        return actionCategory != null && actionCategory.getTargetAmount() != null && actionCategory.getTargetAmount() != 0;
    }
    @DynamoDBIgnore public boolean sameActionType(Action that)
    {
        return userId.equals(that.getUserId()) && actionCategoryId.equals(that.getActionCategoryId());
    }
    @DynamoDBIgnore public boolean isActionType(ActionType actionType)
    {
        return this.actionType == actionType;
    }
    @DynamoDBIgnore public String getActionTypeDisplay()
    {
        return "" + actionType;
    }

    @DynamoDBIgnore public boolean canCombineWith(Action that)
    {
        return (!getId().equals(that.getId()) &&
            sameActionType(that) &&
            isSameDay(that));
    }

    // todo - StringUtils didn't work - not in pom?
    private static boolean equals(String s1, String s2)
    {
        if (s1 == s2) { return true; }
        else if (s1 == null || s2 == null) { return false; }
        else { return s1.equals(s2); }
    }

    @DynamoDBIgnore public boolean isSameDay(Action that)
    {
        return isSameDay(that.getCreatedDate());
    }
    @DynamoDBIgnore public boolean isSameDay(Date date)
    {
        Calendar thisCreated = Calendar.getInstance();
        Calendar dateCreated = Calendar.getInstance();
        thisCreated.setTime(getCreatedDate());
        dateCreated.setTime(date);

        return (thisCreated.get(Calendar.YEAR) == dateCreated.get(Calendar.YEAR) &&
            thisCreated.get(Calendar.DAY_OF_YEAR) == dateCreated.get(Calendar.DAY_OF_YEAR));
    }

    @DynamoDBIgnore public String getName() {
        return actionCategory == null ? null : actionCategory.getName();
    }
    @DynamoDBIgnore public String getPluralName()
    {
        return actionCategory == null ? null : actionCategory.getPluralName();
    }
    @DynamoDBIgnore public BigDecimal getValue()
    {
        return actionCategory == null ? new BigDecimal(0)  : actionCategory.getValue();
    }

    @DynamoDBIgnore
    public ActionType getActionType()
    {
        return actionType;
    }
    public void setActionType(ActionType actionType)
    {
        this.actionType = actionType;
    }

//    @DynamoDBIgnore
//    public boolean isAdjustment()
//    {
//        return isAdjustment;
//    }
//    public void setAdjustment(boolean adjustment)
//    {
//        isAdjustment = adjustment;
//    }

    public int getInt(EntityField field)
    {
        if (AMOUNT_FIELD.equals(field)) return getAmount();
        else return super.getInt(field);
    }

    public void setInt(EntityField field, int value)
    {
        if (AMOUNT_FIELD.equals(field)) setAmount(value);
        else super.setInt(field, value);
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

    @DynamoDBAttribute(attributeName = "ActionCategoryId")
    public String getActionCategoryId()
    {
        return actionCategoryId;
    }
    public void setActionCategoryId(String actionCategoryId)
    {
        this.actionCategoryId = actionCategoryId;
    }

    @DynamoDBAttribute(attributeName = "Amount")
    public int getAmount()
    {
        return amount;
    }
    public void setAmount(int amount)
    {
        this.amount = amount;
    }
}


