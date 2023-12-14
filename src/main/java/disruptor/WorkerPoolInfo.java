package disruptor;


import com.lmax.disruptor.Sequence;
import com.lmax.disruptor.SequenceBarrier;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;

public class WorkerPoolInfo<T> implements ConsumerInfo
{
  private final WorkerPool<T> workerPool;
  private final SequenceBarrier sequenceBarrier;
  private boolean endOfChain = true;

  WorkerPoolInfo(final WorkerPool<T> workerPool, final SequenceBarrier sequenceBarrier)
  {
    this.workerPool = workerPool;
    this.sequenceBarrier = sequenceBarrier;
  }

  @Override
  public Sequence[] getSequences()
  {
    return workerPool.getWorkerSequences();
  }

  @Override
  public SequenceBarrier getBarrier()
  {
    return sequenceBarrier;
  }

  @Override
  public boolean isEndOfChain()
  {
    return endOfChain;
  }

  @Override
  public void start(ThreadFactory threadFactory) {
/*    final Thread thread = threadFactory.newThread(eventprocessor);
    if (null == thread) {
      throw new RuntimeException("Failed to create thread to run: " + eventprocessor);
    }

    thread.start();*/
  }

  @Override
  public void start(Executor executor)
  {
    workerPool.start(executor);
  }

  @Override
  public void halt()
  {
    workerPool.halt();
  }

  @Override
  public void markAsUsedInBarrier()
  {
    endOfChain = false;
  }

  @Override
  public boolean isRunning()
  {
    return workerPool.isRunning();
  }
}

