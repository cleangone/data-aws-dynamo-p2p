package fit.pay2play.data.aws.dynamo.entity;

public enum ActionType
{
    Pay,
    Play,
    PayAdjust;

    public static boolean isPay(ActionType actionType)
    {
        return actionType == Pay || actionType == PayAdjust;
    }
    public static boolean isPlay(ActionType actionType)
    {
        return actionType == Play;
    }
    public static boolean isAdjustment(ActionType actionType)
    {
        return actionType == PayAdjust;
    }
}
