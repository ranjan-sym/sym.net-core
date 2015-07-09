package net.symplifier.core.application.threading;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by ranjan on 6/10/15.
 */
public class ThreadPool<S, A> {

  private static final Logger LOGGER = LogManager.getLogger(ThreadPool.class);

  private volatile boolean exit = false;
  private final S source;
  private final Map<ThreadTarget<S,A>, A> targets = new LinkedHashMap<>();

  public ThreadPool(S source) {
    this.source = source;
  }

  public void start(int threads) {
    LOGGER.info("Starting poll with " + threads + " threads for " + source.getClass().toString());
    for(int i=0; i<threads; ++i) {
      new Thread(new WorkerThread()).start();
    }
  }

  public void stop() {
    exit = true;
    synchronized (targets) {
      targets.clear();
      targets.notifyAll();
    }
  }

  public void queue(ThreadTarget<S, A> target, A attachment) {
    synchronized (targets) {
      targets.put(target, attachment);
      targets.notify();
    }
  }

  class WorkerThread implements Runnable {

    @Override
    public void run() {
      while(!exit) {

        synchronized (targets) {
          while (!exit && targets.isEmpty()) {
            try {
              targets.wait();
            } catch (InterruptedException ex) {
              // Unexpected error
              break;
            }
          }
        }

        if (exit) {
          break;
        }

        Map.Entry<ThreadTarget<S,A>, A> item = null;
        synchronized (targets) {
          Iterator<Map.Entry<ThreadTarget<S, A>, A>> it = targets.entrySet().iterator();
          if (it.hasNext()) {
            item = it.next();
            it.remove();
          }
        }

        if (item != null) {
          try {
            item.getKey().onRun(source, item.getValue());
          } catch (RuntimeException ex) {
            // We cannot allow an exception on the thread to break our application
            ex.printStackTrace();
          }
        }
      }
    }
  }
}
