package fit.pay2play.data.aws.dynamo.entity;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import xyz.cleangone.data.aws.dynamo.entity.base.BaseEntity;
import xyz.cleangone.data.aws.dynamo.entity.base.EntityField;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import static java.util.Objects.requireNonNull;

@DynamoDBTable(tableName="Action")
public class Action extends BaseEntity
{
    public static final EntityField DESC_FIELD = new EntityField("pay.description", "Description");
    public static final EntityField TOTAL_VALUE_FIELD = new EntityField("pay.totalValue", "Total Value");

    private static MathContext TWO_DIGITS = new MathContext(2, RoundingMode.HALF_UP);

    private String userId;
    private String payId;  // either pay or play will be used
    private String playId;
    private int amount;

    // todo - need daily/weekly/monthly for consolidation - start/endTime?

    // the following are held redundantly in case the entities are deleted
    private String name;  // todo - reuse baseNamedEntity?  but don't want enabled field
    private BigDecimal value;


    public Action()
    {
        super();
    }

    public Action(Pay pay, int amount)
    {
        super();
        init(requireNonNull(pay), amount);
        setPayId(pay.getId());
    }

    public Action(Play play, int amount)
    {
        super();
        init(requireNonNull(play), amount);
        setPlayId(play.getId());
    }

    private void init(Play play, int amount)
    {
        setUserId(play.getUserId());
        setAmount(amount);

        setName(play.getName());
        setValue(play.getValue());
    }

    @DynamoDBIgnore public String getPayPlayDisplay()
    {
        return isPay() ? "Pay" : "Play";
    }
    @DynamoDBIgnore public String getDescription()
    {
        return amount + " " + name;
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
        return value.multiply(new BigDecimal(getCreditAmount()), TWO_DIGITS);
    }
    @DynamoDBIgnore public int getCreditAmount()
    {
        return isPay() ? amount : amount * -1;
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

    @DynamoDBAttribute(attributeName = "Name")
    public String getName()
    {
        return name;
    }
    public void setName(String name)
    {
        this.name = name;
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
}


