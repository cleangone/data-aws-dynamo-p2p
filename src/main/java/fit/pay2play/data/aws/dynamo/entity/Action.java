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

import static java.util.Objects.requireNonNull;

@DynamoDBTable(tableName="Action")
public class Action extends BaseEntity
{
    public static final EntityField DESC_FIELD = new EntityField("action.description", "Description");
    public static final EntityField AMOUNT_FIELD = new EntityField("action.amount", "Amount");
    public static final EntityField TOTAL_VALUE_FIELD = new EntityField("action.totalValue", "Total Value");

    private static MathContext TWO_DIGITS = new MathContext(2, RoundingMode.HALF_UP);

    private String userId;
    private String payId;  // either pay or play will be used
    private String playId;
    private int amount;

    protected String name;  // transient
    protected String pluralName; // transient
    private BigDecimal value; // transient

    public Action()
    {
        super();
    }

    public Action(Pay pay)
    {
        super();
        setUserId(requireNonNull(pay).getUserId());
        setPayId(pay.getId());
        setAmount(1);
    }

    public Action(Play play)
    {
        super();
        setUserId(requireNonNull(play).getUserId());
        setPlayId(play.getId());
        setAmount(1);
    }

    public void populate(Play play)
    {
        setName(play == null ? "Unknown" : play.getName());
        setPluralName(play == null ? null : play.getPluralName());
        setValue(play == null ? new BigDecimal(0) : play.getValue());
    }

    @DynamoDBIgnore public String getPayPlayId()
    {
        return isPay() ? payId : playId;
    }
    @DynamoDBIgnore public String getPayPlayDisplay()
    {
        return isPay() ? "Pay" : "Play";
    }
    @DynamoDBIgnore public String getDescription()
    {
        return amount + " " + (amount == 1 || getPluralName() == null ? getName() : getPluralName());
    }
    @DynamoDBIgnore public boolean isPay()
    {
        return payId != null;
    }
    @DynamoDBIgnore public boolean isPlay()
    {
        return playId != null;
    }
    @DynamoDBIgnore public BigDecimal getTotalValue()
    {
        return getValue().multiply(new BigDecimal(getCreditAmount()), TWO_DIGITS);
    }
    @DynamoDBIgnore public int getCreditAmount()
    {
        return isPay() ? amount : amount * -1;
    }

    @DynamoDBIgnore public boolean samePayPlay(Action that)
    {
        return (
            getUserId().equals(that.getUserId()) &&
            equals(payId, that.getPayId()) &&
            equals(playId, that.getPlayId()));
    }

    @DynamoDBIgnore public boolean canCombineWith(Action that)
    {
        return (!getId().equals(that.getId()) &&
            samePayPlay(that) &&
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

    @DynamoDBAttribute(attributeName = "PayId")
    public String getPayId()
    {
        return payId;
    }
    public void setPayId(String payId)
    {
        this.payId = payId;
    }

    @DynamoDBAttribute(attributeName = "PlayId")
    public String getPlayId()
    {
        return playId;
    }
    public void setPlayId(String playId)
    {
        this.playId = playId;
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

    @DynamoDBIgnore
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }

    @DynamoDBIgnore
    public String getPluralName()
    {
        return pluralName;
    }
    public void setPluralName(String pluralName)
    {
        this.pluralName = pluralName;
    }

    @DynamoDBIgnore
    public BigDecimal getValue()
    {
        return value;
    }
    public void setValue(BigDecimal value)
    {
        this.value = value;
    }
}


