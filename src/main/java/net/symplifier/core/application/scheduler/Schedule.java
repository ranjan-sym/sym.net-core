package net.symplifier.core.application.scheduler;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

/**
 * The Schedule class that defines the cron like scheduling mechanism
 * 
 * @author ranjan
 *
 */
public class Schedule {
	private ArrayList<ScheduledTask> tasks = new ArrayList<>();
	
	protected long nextRunTime;		/* The timestamp at which this schedule is supposed to run next */
	
	private int[] milliSeconds = new int[] {0};
	private int[] seconds;
	private int[] minutes;
	private int[] hours;
	private int[] weekdays;
	private int[] days;
	private int[] months;
	private int[] years;
	
	public void setMilliSeconds(int ... values) {
		milliSeconds = values;
	}
	/**
	 * Set the seconds value at which you want to activate this schedule. You 
	 * can have the schedule activated at multiple seconds 
	 * 
	 * @param values The second values (0 - 59)
	 */
	public void setSeconds(int ... values) {
		seconds = values;
	}
	
	/**
	 * Set the minute value at which you want to activate this schedule. If not
	 * mentioned, the schedule will run on every minute as long as other conditions
	 * are met
	 * 
	 * @param values The minute values (0 - 59)
	 */
	public void setMinutes(int ... values) {
		minutes = values;
	}
	
	/**
	 * Set the hour values at which you want to activate this schedule. If not
	 * mentioned, the schedule will run on every hour as long as other conditions
	 * are met
	 * @param values The hour values (0 - 23)
	 */
	public void setHours(int ... values) {
		hours = values;
	}
	
	/**
	 * Set the day of week value at which you want to activate this schdule. If
	 * not metioned, the schedule will run every day of week as long as other
	 * conditions are met. Use this value in combination with DayOfMonths to 
	 * generate unique combinations for activating schedule, like first sunday 
	 * of every month or first and third Monday, etc
	 * @param values The day of week values (1 - 7)
	 */
	public void setDaysOfWeek(int ... values) {
		weekdays = values;
	}
	
	/**
	 * Set the day of month at which you want to activate this schedule. If not
	 * mentioned, the scheduled will run every day as long as other conditions 
	 * are met. Use this value in combination with DaysOfWeek to generate unique 
	 * combinations for activating schedule like last saturday of every month
	 * 
	 * @param values Days of month values (1 - 31)
	 */
	public void setDaysOfMonth(int ... values) {
		days = values;
	}
	
	/**
	 * Set the month at which you want to activate this schedule. If not mentioned,
	 * the schedule will run every month as long as other conditions are met
	 * @param values The months values (1 - 12)
	 */
	public void setMonths(int ... values) {
		months = values;
	}
	
	/**
	 * Set the year at which you want to activate this schedule. If not mentioned,
	 * the schedule will run every year as long as other conditions are met
	 * 
	 * @param values The Year values
	 */
	public void setYears(int ... values) {
		years = values;
	}
	
	/**
	 * After changing the date and time fields, invoke this method to update
	 * the changes to this schedule in the scheduler. If not updated, the
	 * schedule will run once as per previous arrangement.
	 */
	public void updateScheduler() {
		nextRunTime = 0;
		Scheduler.removeSchedule(this);
		Scheduler.addSchedule(this);
	}

	public boolean isScheduled() {
		return Scheduler.isScheduled(this);
	}
	
	/* Helper function to find next value in the array in reference to the refValue. If the value
	 * rolls over, the new value returned is the first value with rollover addition */ 
	private int findNextValue(int[] array, int refValue, int rollover) {
		if (array == null || array.length == 0) {
			return refValue;
		}
		
		for(int x:array) {
			if (x>=refValue) {
				return x;
			}
		}
		
		return array[0] + rollover;
	}
	
	private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	
	/**
	 * Retrieve the next run time in milliseconds (Unix timestamp value). If
	 * the next run time has not been calculated, it is calculated and then 
	 * returned. This function can even return a past value.
	 * 
	 * @return the next unix epoch at which this schedule is supposed to run
	 */
	public long getNextRunTime() {
		if(nextRunTime == 0) {
			nextRunTime = calcNextRunTime(System.currentTimeMillis());
		}
		return nextRunTime;
	}
	
	/**
	 * Retrieve the next run time in milliseconds (Unit timestamp value) that
	 * is after the given timestamp
	 * 
	 * @param timestamp The threshold timestamp
	 * @return The next unix epoch after timestamp at which this schedule is supposed to run
	 */
	public long getNextRunTime(long timestamp) {
		if(nextRunTime == 0 || nextRunTime <= timestamp) {
			nextRunTime = calcNextRunTime(timestamp);
		} 

		return nextRunTime;
	}
	
	/* Calculates the next run time after the given timestamp */
	private long calcNextRunTime(long timestamp) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date(timestamp + 1));
		String orgDayValue = DATE_FORMAT.format(cal.getTime());
		//System.out.println(orgDayValue);
		
		fixTime(cal);
		fixDate(cal, 0);
		String newDayValue = DATE_FORMAT.format(cal.getTime());
		//System.out.println(newDayValue);
		
		if (!newDayValue.equals(orgDayValue)) {
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			fixTime(cal);
		}
		
		return cal.getTime().getTime();
		
	}
	
	/* Fix the time part of the next run time */
	private boolean fixTime(Calendar cal) {
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int minute = cal.get(Calendar.MINUTE);
		int second = cal.get(Calendar.SECOND);
		int milli = cal.get(Calendar.MILLISECOND);
		
		milli = findNextValue(milliSeconds, milli, 1000);
		if (milli >= 1000) {
			milli = milli - 1000;
			second += 1;
		}
		cal.set(Calendar.MILLISECOND, milli);
		
		second = findNextValue(seconds, second, 60);
		if (second >= 60) {
			second = second - 60;
			minute += 1;
		}
		cal.set(Calendar.SECOND, second);
		
		minute = findNextValue(minutes, minute, 60);
		if (minute >= 60) {
			minute = minute - 60;
			hour += 1;
		}
		cal.set(Calendar.MINUTE, minute);

		hour = findNextValue(hours, hour, 24);
		if (hour >= 24) {
			hour = hour - 24;
			cal.set(Calendar.HOUR_OF_DAY, hour);
			cal.add(Calendar.DAY_OF_MONTH, 1);
			return true;
		} else {
			cal.set(Calendar.HOUR_OF_DAY, hour);
			return false;
		}
	}
	
	/* fix the date part of the next run time, this is a bit complicated
	 * due do the combination allowed between the days of the week and date
	 */
	private boolean fixDate(Calendar date, int tryCount) {
		// An exhaust condition
		if (tryCount > 30) {
			return false;
		}
		int dom = date.get(Calendar.DAY_OF_MONTH);
		int month = date.get(Calendar.MONTH);
		int year = date.get(Calendar.YEAR);
		
		dom = findNextValue(days, dom, 100);
		if (dom >= 100) {
			dom = dom - 100;
			month += 1;
		}
		date.set(Calendar.DAY_OF_MONTH, dom);
		
		month = findNextValue(months, month, 12);
		if (month >= 13) {
			month = month - 12;
			year += 1;
		}
		date.set(Calendar.MONDAY, month);
		
		year = findNextValue(years, year, 10000);
		if (year >= 10000) {
			return false;
		}
		
		if (weekdays == null || weekdays.length == 0) {
			return true;
		} else {
			for(int dow:weekdays) {
				if (dow == date.get(Calendar.DAY_OF_WEEK)) {
					return true;
				}
			}
			
			// Recursively search for a date that would match to day of week
			date.add(Calendar.DAY_OF_MONTH, 1);
			return fixDate(date, tryCount+1);
		}
	}
	
	Iterator<ScheduledTask> getTasks() {
		return tasks.iterator();
	}
	
	/**
	 * Add a task to be run on this schedule. If there are no tasks on a schedule,
	 * it is not added on the Scheduler.
	 * @param task The task that needs to run on schedule
	 */
	public void addTask(ScheduledTask task) {
		synchronized(this) {
			tasks.add(task);
			if (tasks.size() == 1) {
				Scheduler.addSchedule(this);
			}
		}
	}
	
	/**
	 * Remove a task from this schedule. If there are no more tasks on this
	 * schedule, it is removed from the Scheduler
	 * 
	 * @param task The task that need not be run on schedule
	 */
	public void removeTask(ScheduledTask task) {
		synchronized(this) {
			tasks.remove(task);
			if (tasks.size() == 0) {
				Scheduler.removeSchedule(this);
			}
		}
	}
}
