package shared.Timer;


import logging.Logger;
import logging.LoggingCategory;

import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Singleton class enabling add timed callbacks
 *
 * @author Peter Babics <babicpe1@fit.cvut.cz>
 */
final public class Timer implements Runnable {

    /**
     * Internal class for priority queue
     */
    private class EntryPair implements Comparable {
        /**
         * @var time Time in miliseconds when timer shall call callback
         */
        private Long time;

        /**
         * @var entry Timer entry containing callback object reference
         */
        private TimerEntry entry;


        /**
         * Constructor of internal class for priority Queue
         *
         * @param time Time in miliseconds when timer shall call the callback
         * @param entry Timer entry containing callback object reference
         */
        public EntryPair(Long time, TimerEntry entry) {
            this.time = time;
            this.entry = entry;
        }

        /**
         * Gets time in miliseconds when timer shall call the callback
         *
         * @return time
         */
        public Long getTime() {
            return time;
        }

        /**
         * Gets timer entry containing callback object reference
         *
         * @return entry
         */
        public TimerEntry getEntry() {
            return entry;
        }

        @Override
        public int compareTo(Object o) {
            EntryPair p = (EntryPair) o;
            if (p == null)
                return 1;
            return this.time < p.time ? -1 : 1;
        }
    };

    // Timer instance reference
    private static Timer instance = null;

    // Timer worker thread reference
    private static Thread thread = null;

    // Hash map for storing timer entries
    private Map<String, TimerEntry> timerEntries = Collections.synchronizedMap(new HashMap<String, TimerEntry>());

    // Thread safe priority queue
    private PriorityBlockingQueue<EntryPair> expireOrder = new PriorityBlockingQueue<>();

    // Singleton initialisation
    static {
        System.out.println("Creating Timer Instance");
        instance = new Timer();
    }

    private Timer() {
    }

    private void addTimerEntry(String intervalName, int interval, ITimerCallable callback, Boolean isRepeated, Boolean active) {
        TimerEntry e = new TimerEntry(intervalName, callback, interval, isRepeated, active);
        timerEntries.put(intervalName, e);
        if (thread == null)
            return;
        if (active) {
            synchronized (expireOrder) {
                System.out.println("Adding to queue: " + intervalName + " Size: " + expireOrder.size());
                expireOrder.add(new EntryPair((new Date()).getTime() + interval * 1000, e));
                expireOrder.notify();
            }
        }
    }

    public void removeTimerCallback(String timerName) {
        for (EntryPair e : expireOrder)
            if (e.getEntry().getEntryName().compareTo(timerName) == 0)
                expireOrder.remove(e);
        timerEntries.remove(timerName);
    }

    public Boolean _hasTimer(String timerName) {
        return timerEntries.containsKey(timerName);
    }

    public Boolean _hasActiveTimer(String timerName) {
        TimerEntry e = timerEntries.get(timerName);
        if (e == null)
            return false;
        return e.isActive();
    }

    public void _activateTimer(String timerName) {
        TimerEntry e = timerEntries.get(timerName);
        if (e == null)
            return;
        if (e.isActive())
            return;
        e.setTimerState(true);
        if (thread == null)
            return;
        expireOrder.add(new EntryPair((new Date()).getTime() + e.getInterval(),  e));
    }

    public void _deactivateTimer(String timerName) {
        TimerEntry e = timerEntries.get(timerName);
        if (e == null)
            return;
        if (!e.isActive())
            return;
        e.setTimerState(false);
        if (thread == null)
            return;
        for (EntryPair _e : expireOrder)
            if (_e.getEntry().getEntryName() == timerName)
                expireOrder.remove(_e);
    }

    public Long _getTimerRemainingTime(String timerName) {
        TimerEntry e = timerEntries.get(timerName);
        if (e == null)
            return (long) -1;
        if (!e.isActive())
            return (long) -1;
        if (thread == null)
            return (long) -1;

        for (EntryPair _e : expireOrder)
            if (_e.getEntry().getEntryName() == timerName)
                return _e.getTime() - (new Date()).getTime();
        return (long) -1;
    }

    private void _resetTimer(String timerName, Integer newInterval) {
        TimerEntry e = timerEntries.get(timerName);
        if (e == null)
            return;


        if (e.isActive()) {
            e.setTimerState(false);
            for (EntryPair _e : expireOrder)
            {
                if (_e.getEntry().getEntryName().compareTo(timerName) == 0)
                {
                    System.out.println("Removed entry " + _e);
                    expireOrder.remove(_e);
                }
            }
        }

        TimerEntry newEntry = e;
        if (newInterval != null) {
            timerEntries.remove(timerName);
            newEntry = new TimerEntry(timerName, e.getCallback(), newInterval, e.isRepeated(), true);
            timerEntries.put(timerName, newEntry);
        }

        if (thread == null)
            return;

        synchronized (expireOrder) {

            EntryPair _e = new EntryPair((new Date()).getTime() + newEntry.getInterval() * 1000, newEntry);
            System.out.println("Removed entry " + e + " Planned new " + _e + "  @  " + _e.getTime()) ;
            expireOrder.add(_e);
            expireOrder.notify();
        }

    }

    private void startThread() {
        if (thread != null)
            return;
        thread = new Thread(instance);

        synchronized (instance.expireOrder) {
            for (TimerEntry timerEntry : instance.timerEntries.values())
            {
                if (!timerEntry.isActive())
                    continue;
                EntryPair _e = new EntryPair((new Date()).getTime() + timerEntry.getInterval() * 1000, timerEntry);
                instance.expireOrder.add(_e);
                instance.expireOrder.notify();
            }
        }

        thread.start();
    }
/*
    =========================================================================
                      Static Delegates
    =========================================================================
 */


    /**
     * Delegate for registering Interval callbacks
     * Using this method will automatically activate callback
     *
     * @param intervalName Name of callback
     * @param inteval Interval in milliseconds in which callback should be called
     * @param callback Reference to callback class
     */
    public static void registerIntervalCallback(String intervalName, int inteval, ITimerCallable callback) {
        instance.addTimerEntry(intervalName, inteval, callback, true, true);
    }

    /**
     * Delegate for registering Interval callbacks with option of active state
     *
     * @param intervalName Name of callback
     * @param interval Interval in milliseconds in which callback should be called
     * @param callback Reference to callback class
     * @param active Boolean informing whenever callback shall be activated right after adding
     */
    public static void registerIntervalCallback(String intervalName, int interval, ITimerCallable callback, Boolean active) {
        instance.addTimerEntry(intervalName, interval, callback, true, active);
    }

    /**
     * Delegate for registering Timed callbacks
     * Using this method will automatically activate callback
     *
     * @param intervalName Name of callback
     * @param inteval Interval in milliseconds in which callback should be called
     * @param callback Reference to callback class
     */
    public static void registerTimedCallback(String intervalName, int inteval, ITimerCallable callback) {
        instance.addTimerEntry(intervalName, inteval, callback, false, true);
    }

    /**
     * Delegate for registering Timed callbacks with option of active state
     *
     * @param intervalName Name of callback
     * @param inteval Interval in milliseconds in which callback should be called
     * @param callback Reference to callback class
     * @param active Boolean informing whenever callback shall be activated right after adding
     */
    public static void registerTimedCallback(String intervalName, int inteval, ITimerCallable callback, Boolean active) {
        instance.addTimerEntry(intervalName, inteval, callback, false, active);
    }

    /**
     * Delegate for removing callbacks
     *
     * @param timerName Name of callback
     */
    public static void removeCallback(String timerName) {
        instance.removeTimerCallback(timerName);
    }

    /**
     * Delegate for checking whenever callback is registered
     *
     * @param timerName Name of callback
     * @return True if callback exists in Timer entries list
     */
    public static Boolean hasTimer(String timerName) {
        return instance.hasTimer(timerName);
    }


    /**
     * Delegate for checking whenever callback is registered and active
     *
     * @param timerName Name of callback
     * @return True if callback exists in Timer entries list and also in processing queue
     */
    public static Boolean hasActiveTimer(String timerName) {
        return instance._hasActiveTimer(timerName);
    }

    /**
     * Delegate for deactivating callback
     *
     * @param timerName Name of callback
     */
    public static void deactivateTimer(String timerName) {
        instance._deactivateTimer(timerName);
    }

    /**
     * Delegate for activating callback
     *
     * @param timerName Name of callback
     */
    public static void activateTimer(String timerName) {
        instance._activateTimer(timerName);
    }

    /**
     * Delegate that returns remaining time until callback execution
     *
     * @param timerName Name of callback
     * @return Time in milliseconds until execution
     */
    public static Long getTimerRemainingTime(String timerName) { return instance._getTimerRemainingTime(timerName); }

    /**
     * Recalculates callback execution time
     * New execution time is calculated as Current Time + Callback interval
     *
     * @param timerName Name of callback
     */
    public static void resetTimer(String timerName) { instance._resetTimer(timerName, null); }

    /**
     * Recalculates callback execution time
     * New execution time is calculated as Current Time + New interval time
     *
     * @param timerName Name of callback
     * @param newInterval Callback new interval time
     */
    public static void resetTimer(String timerName, Integer newInterval) { instance._resetTimer(timerName, newInterval); }

    /**
     * Delegate for starting Timer worked thread
     * Called right after the Network model is loaded
     */
    public static void start() { instance.startThread(); }

/*
    =========================================================================
                      Thread Worker method
    =========================================================================
 */

    /**
     * Thread worker method
     */
    @Override
    public void run() {
        System.out.println("Starting Timer Thread");
        while (true) {
            EntryPair entryPair = expireOrder.peek();
            if (entryPair == null) {
                try {
                    synchronized (expireOrder) {
                        expireOrder.wait();
                        continue;
                    }
                } catch (InterruptedException e) {
                    continue;
                }
            }

            Long curTime = (new Date()).getTime();
            if (entryPair.getTime() > curTime) {
                try {
                    synchronized (expireOrder) {
                        expireOrder.wait((entryPair.getTime() - curTime));
                    }
                } catch (InterruptedException e) {
                    continue;
                }
            }

            while (true) {
                EntryPair _e = null;
                synchronized (expireOrder)
                {
                    _e = expireOrder.poll();
                    if (_e == null)
                        break;
                    if (_e.getTime() >= (new Date()).getTime())
                    {
                        expireOrder.add(new EntryPair((new Date()).getTime() + _e.getEntry().getInterval() * 1000, _e.getEntry()));
                        break;
                    }
                }
                    _e.getEntry().getCallback().timerExpired(_e.getEntry());
                 synchronized (expireOrder)
                 {
                     if (_e.getEntry().isRepeated())
                         expireOrder.add(new EntryPair((new Date()).getTime() + _e.getEntry().getInterval() * 1000, _e.getEntry()));
                     else
                         timerEntries.remove(_e.getEntry().getEntryName());
                 }
            }
        }
    }
}
