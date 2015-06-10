package net.symplifier.core.application.threading;

/**
 * Created by ranjan on 6/10/15.
 */
public interface ThreadTarget <S, A>{

  void onRun(S source, A attachment);
}
