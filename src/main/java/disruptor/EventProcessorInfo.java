package disruptor;

import com.lmax.disruptor.EventProcessor;
import com.lmax.disruptor.Sequence;
import com.lmax.disruptor.SequenceBarrier;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;

public class EventProcessorInfo implements ConsumerInfo {

  private final EventProcessor eventprocessor;
  private final SequenceBarrier barrier;
  private boolean endOfChain = true;

  EventProcessorInfo(final EventProcessor eventprocessor, final SequenceBarrier barrier) {
    this.eventprocessor = eventprocessor;
    this.barrier = barrier;
  }

  public EventProcessor getEventProcessor() {
    return eventprocessor;
  }

  @Override
  public Sequence[] getSequences() {
    return new Sequence[]{eventprocessor.getSequence()};
  }

  @Override
  public SequenceBarrier getBarrier() {
    return barrier;
  }

  @Override
  public boolean isEndOfChain() {
    return endOfChain;
  }

  @Override
  public void start(final ThreadFactory threadFactory) {
    final Thread thread = threadFactory.newThread(eventprocessor);
    if (null == thread) {
      throw new RuntimeException("Failed to create thread to run: " + eventprocessor);
    }

    thread.start();
  }

  @Override
  public void halt() {
    eventprocessor.halt();
  }

  /**
   *
   */
  @Override
  public void markAsUsedInBarrier() {
    endOfChain = false;
  }

  @Override
  public void start(Executor executor) {
    //workerPool.start(executor);
  }

  @Override
  public boolean isRunning() {
    return eventprocessor.isRunning();
  }
}

