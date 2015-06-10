package net.symplifier.core.application.threading;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by ranjan on 6/10/15.
 */
public class ThreadPool<S, A> {
  private volatile boolean exit = false;
  private final S source;
  private final Map<ThreadTarget<S,A>, A> targets = new LinkedHashMap<>();

  public ThreadPool(S source) {
    this.source = source;
  }

  public void start(int threads) {
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
          while(!exit && targets.isEmpty()) {
            try {
              targets.wait();
            } catch (InterruptedException ex) {
              // Unexpected error
              break;
            }
          }

          if (exit) {
            break;
          }

          Iterator<Map.Entry<ThreadTarget<S,A>, A>> it = targets.entrySet().iterator();
          if (it.hasNext()) {
            Map.Entry<ThreadTarget<S,A>, A> item = it.next();
            it.remove();

            try {
              item.getKey().onRun(source, item.getValue());
            } catch(RuntimeException ex) {
              // We cannot allow an exception on the thread to break our application
              ex.printStackTrace();
            }
          }
        }

      }
    }
  }
}
