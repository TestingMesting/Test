package Task;

public class ReportDefinition {
    private final long TOP_PERFORMERS_THRESHOLD;
    private final boolean USER_EXPERIENCE_MULTIPLIER;
    private final long PERIOD_LIMIT;

    public ReportDefinition(long TOP_PERFORMERS_THRESHOLD, boolean USER_EXPERIENCE_MULTIPLIER, long PERIOD_LIMIT) {
        this.TOP_PERFORMERS_THRESHOLD = TOP_PERFORMERS_THRESHOLD;
        this.USER_EXPERIENCE_MULTIPLIER = USER_EXPERIENCE_MULTIPLIER;
        this.PERIOD_LIMIT = PERIOD_LIMIT;
    }

    public long getTOP_PERFORMERS_THRESHOLD() {
        return TOP_PERFORMERS_THRESHOLD;
    }

    public boolean isUSER_EXPERIENCE_MULTIPLIER() {
        return USER_EXPERIENCE_MULTIPLIER;
    }

    public long getPERIOD_LIMIT() {
        return PERIOD_LIMIT;
    }
}
