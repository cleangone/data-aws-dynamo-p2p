package fit.pay2play.data.aws.dynamo.entity;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import xyz.cleangone.data.aws.dynamo.entity.base.BaseEntity;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import static java.util.Objects.requireNonNull;

@DynamoDBTable(tableName="Action")
public class P2pAction extends BaseEntity
{
    private static MathContext TWO_DIGITS = new MathContext(2, RoundingMode.HALF_UP);

    private String userId;
    private String payId;  // either pay or play will be used
    private String playId;
    private int amount;

    // todo - need daily/weekly/monthly for consolidation - start/endTime?

    // the following are held redundantly in case the entities are deleted
    private String name;  // todo - reuse baseNamedEntity?  but don't want enabled field
    private BigDecimal value;


    public P2pAction()
    {
        super();
    }

    public P2pAction(Pay pay, int amount)
    {
        super();
        init(requireNonNull(pay), amount);
        setPayId(pay.getId());
    }

    public P2pAction(Play play, int amount)
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
        return value.multiply(getPlusMinusAmount(), TWO_DIGITS);
    }
    @DynamoDBIgnore public BigDecimal getPlusMinusAmount()
    {
        return isPay() ? new BigDecimal(amount) : new BigDecimal(amount * -1);
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


