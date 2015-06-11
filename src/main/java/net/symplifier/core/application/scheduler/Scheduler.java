package net.symplifier.core.application.scheduler;

import net.symplifier.core.application.threading.ThreadPool;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * The main Scheduler implementation. The Scheduler can run any arbitrary 
 * ScheduledTask set for a definite Schedule based on the date and time. A set
 * of threads in a pool (ThreadPool) are used for running the ScheduledTask. If
 * the number of tasks to be run are greater than the number of threads allocated
 * in the pool, the tasks are queued and run as soon as a thread is available.
 * 
 * Scheduler itself has a main thread, which does all the controlling.
 * 
 * @author ranjan
 * @version 1.0
 * 
 *
 */
public class Scheduler implements Runnable {
	private final ArrayList<Schedule> schedules = new ArrayList<>();
	private final ThreadPool<Scheduler, Schedule> pool = new ThreadPool<>(this);
	private static Scheduler SELF = new Scheduler();
	private volatile boolean exit;
	private volatile boolean started;

	private Scheduler() {
		start();
	}
	
	/**
	 * Start the scheduler. All the threads from the pool are allocated and
	 * ScheduledTask are executed as per their definition in Schedule.
   *
   * There is no need to call this method explicitly to start the scheduler
   * as it is automatically started during the application initialization
   * through the static constructor
	 */
	public void start() {
    if (!started) {
      started = true;

      new Thread(this).start();
      pool.start(5);
    }
	}
	
	/**
	 * Stop the scheduler. At the end of the application.
	 */
	public void stop() {
		exit = true;
		pool.stop();			/* Stop the thread pool */
		
		schedules.notifyAll();	/* Notify the main thread for exit */
    started = false;
	}

	public static void addTimer(Timer timer) {
		addSchedule(timer);
	}
	/**
	 * Add a schedule for running. The Schedule need not be explicitly added to 
	 * the Scheduler but rather managed from Schedule. 
	 * 
	 * The Schedule are always arranged in ascending order by the time when
	 * they need to be run next. 
	 * 
	 * @param schedule The schedule to be included in the scheduler
	 */
	public static void addSchedule(Schedule schedule) {
		synchronized(SELF.schedules) {
			long nextRunTime = schedule.getNextRunTime(System.currentTimeMillis());

			if (nextRunTime == -1) {
				return;
			}

			int insertedAt = -1;
			int removeAt = -1;
			for(int i=0; i<SELF.schedules.size(); ++i) {
				Schedule chk = SELF.schedules.get(i);
				// don't check with self, mark for removal
				if (chk == schedule) {
					removeAt = i;
					continue;
				}
				
				if (insertedAt == -1) {
					if (nextRunTime < chk.getNextRunTime()) {
						SELF.schedules.add(i, schedule);
						insertedAt = i;
					}
				}
			}
			
			// if something has been marked for removal, shifting the schedule from one point to another
			// due to change in its configuration
			if (removeAt >= 0) {
				SELF.schedules.remove(removeAt);
			}
			
			// if the schedule has not been inserted anywhere insert it at the end
			if (insertedAt == -1) {
				SELF.schedules.add(schedule);
			}
			
			// If the schedule is inserted at the very beginning, we need to 
			// reevaluate our wait time in the main scheduler thread
			if (insertedAt == 0 || SELF.schedules.size() == 1) {
				SELF.schedules.notifyAll();
			}
		}			
		
	}
	
	/**
	 * Remove a Schedule from schedule. A Schedule need not be explicitly removed
	 * from the Scheduler but rather controller indirectly from Schedule addTask 
	 * and removeTask
	 * 
	 * @param schedule Schedule to be removed
	 */
	public static void removeSchedule(Schedule schedule) {
		synchronized(SELF.schedules) {
			Iterator<Schedule> it = SELF.schedules.iterator();
			boolean first = true;
			
			// Iterate through all schedules and remove the given schedule
			while(it.hasNext()) {
				Schedule sch = it.next();
				if (sch == schedule) {
					it.remove();
					// if the schedule is the first on the list, we need to
					// reevaluate our wait time in the main scheduler thread
					if (first) {
						SELF.schedules.notifyAll();
					}
					break;
				}
				first = false;
			}
		}
	}

	public static boolean isScheduled(Schedule schedule) {
		synchronized (SELF.schedules) {
			return SELF.schedules.contains(schedule);
		}
	}
	
	@Override
	public final void run() {
		long timestamp, scheduled, delay;
		scheduled = 0;
		ArrayList<Schedule> schedulesToRun = new ArrayList<Schedule>();
		while(!exit) {
			schedulesToRun.clear();

			timestamp = System.currentTimeMillis();
			delay = scheduled - timestamp;
			
			synchronized(schedules) {
				try {
					// If no schedules are defined wait indefinitely
					if (schedules.isEmpty()) {
//						System.out.println("Waiting for external event no schedule to work on");
						schedules.wait();
					} else {
						/* Find out how long do we need to wait before the Schedule in line needs to be run */						
						delay = schedules.get(0).getNextRunTime() - timestamp;
						if (delay > 0) {
							//System.out.println("Waiting for " + delay + " milliseconds for a schedule to start");
							schedules.wait(delay);
						} else {
//							System.out.println("No time to wait, delay calculated is " + delay + " milliseconds");
						}
					}
				} catch (InterruptedException e) {
					// This is not supposed to happen */
					System.err.println("An error occured while waiting for schedules");
				} 
				
				if(exit) break;	/* if asked for quitting, quit */
				
				Iterator<Schedule> it = schedules.iterator();
				/* Iterate through all the schedules and find out the ones that need to be run
				 * and also remove them from our schedules list for addition later after
				 * executing the tasks
				 */
				while(it.hasNext()) {
					Schedule sch = it.next();
					scheduled = sch.getNextRunTime();
					if (scheduled > System.currentTimeMillis()) {
						break;
					} else {
						schedulesToRun.add(sch);
						it.remove();
					}
				}
			}	
			
			/* Run all the scheduled tasks */
			for(Schedule schedule:schedulesToRun) {
				synchronized(schedule) {
					Iterator<ScheduledTask> tasks = schedule.getTasks();
					while(tasks.hasNext()) {
						this.pool.queue(tasks.next(), schedule);
					}
				}
				
				/* Activate the schedule after running the tasks */
				addSchedule(schedule);
			}
		}
	}

}
