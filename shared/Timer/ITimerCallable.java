package shared.Timer;

/**
 * Interface used as base class for callbacks of Timer
 *
 * @author Peter Babics <babicpe1@fit.cvut.cz>
 */
public interface ITimerCallable {

    /**
     *  Public method used as callback for Timer
     */
    public void timerExpired(TimerEntry entry);

}
