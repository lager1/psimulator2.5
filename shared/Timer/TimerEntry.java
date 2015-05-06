package shared.Timer;


/**
 * Class representing timer callback entry
 *
* @author Peter Babics <babicpe1@fit.cvut.cz>
*/
public class TimerEntry {


    // Timer entry name or Callback name
    private String entryName;

    // Reference to callback class
    private ITimerCallable callback;

    // Interval in which callback shall be executed
    private Integer interval;

    // Boolean informing whenever timer should be repeated ( Interval Callback or Timed Callback )
    private Boolean isRepeated;

    // Boolean informing whenever timer is in processing queue
    private Boolean isActive;

    /**
     * Constructor of Timer entry
     *
     * @param entryName Name of Callback / Entry
     * @param callback Reference to callback class
     * @param interval Interval in which callback shall be executed
     * @param isRepeated Boolean informing whenever callback should be repeated ( Interval callback or Timer Callback )
     * @param isActive Boolean informing whenever timer is in processing queue
     */
    public TimerEntry(String entryName, ITimerCallable callback, Integer interval, Boolean isRepeated, Boolean isActive) {
        this.entryName = entryName;
        this.callback = callback;
        this.interval = interval;
        this.isRepeated = isRepeated;
        this.isActive = isActive;
    }

    /**
     * Gets timer entry name
     * @return Timer entry name
     */
    public String getEntryName() {
        return entryName;
    }

    /**
     * Gets reference to callback class
     * @return Callback class
     */
    public ITimerCallable getCallback() {
        return callback;
    }

    /**
     * Returns interval in which callback shall be executed
     * @return Interval in which callback shall be executed
     */
    public Integer getInterval() {
        return interval;
    }

    public Boolean isRepeated() {
        return isRepeated;
    }

    public Boolean isActive() {
        return isActive;
    }

    public void setTimerState(Boolean isActive) {
        this.isActive = isActive;
    }
}
