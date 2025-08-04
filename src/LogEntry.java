
public class LogEntry {
    private String activity;
    private long duration;

    // initializes activity and duration
    public LogEntry(String activity, long duration) {
        this.activity = activity;
        this.duration = duration;
    }
    // returns activity
    public String getActivity() {
        return activity;
    }
    // returns duration
    public long getDuration() {
        return duration;
    }
    // Converts format to HH:MM:SS
    @Override
    public String toString() {
        long hours = duration / 3600;
        long minutes = (duration % 3600) / 60;
        long seconds = duration % 60;

        return String.format("%s - %02d:%02d:%02d", activity, hours, minutes, seconds);
    }
}
