package disruptor;

import com.lmax.disruptor.Sequence;
import com.lmax.disruptor.SequenceBarrier;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;

public interface ConsumerInfo {
  Sequence[] getSequences();

  SequenceBarrier getBarrier();

  boolean isEndOfChain();

  void start(ThreadFactory threadFactory);

  void halt();

  void markAsUsedInBarrier();

  void start(Executor executor);

  boolean isRunning();
}

