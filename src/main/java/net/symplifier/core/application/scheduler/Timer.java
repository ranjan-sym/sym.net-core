package net.symplifier.core.application.scheduler;

/**
 * Timer to schedule an event after a fixed interval
 *
 * Created by ranjan on 12/9/14.
 */
public class Timer extends Schedule {
  private long firstDelay;
  private long repeatDelay;

  public Timer() {
    this.nextRunTime = -1;
    firstDelay = -1;
    repeatDelay = -1;
  }

  public Timer(ScheduledTask task) {
    this();
    addTask(task);
  }

  public void cancel() {
    Scheduler.removeSchedule(this);
  }

  public void start(long delay) {
    start(delay, -1);
  }

  public void start(long firstDelay, long repeatDelay) {
    Scheduler.removeSchedule(this);
    this.firstDelay = firstDelay;
    this.repeatDelay = repeatDelay;
    this.nextRunTime = 0;
    Scheduler.addSchedule(this);
  }

  public long getNextRunTime(long timestamp) {
    if (nextRunTime == 0) {
      nextRunTime = timestamp + firstDelay;
    } else {
      if (repeatDelay == -1) {
        nextRunTime = -1;
      } else {
        nextRunTime = timestamp + repeatDelay;
      }
    }
    return nextRunTime;
  }
}
