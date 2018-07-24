package fit.pay2play.data.aws.dynamo.entity;

import xyz.cleangone.data.aws.dynamo.entity.lastTouched.EntityType;

public class P2PEntityType extends EntityType
{
    public static final EntityType PAY = new EntityType("Pay");
    public static final EntityType PLAY = new EntityType("Play");

    public P2PEntityType(String name)
    {
        super(name);
    }
}

