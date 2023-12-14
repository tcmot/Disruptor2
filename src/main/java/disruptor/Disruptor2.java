package disruptor;

import com.lmax.disruptor.BatchEventProcessor;
import com.lmax.disruptor.BatchEventProcessorBuilder;
import com.lmax.disruptor.BatchRewindStrategy;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.RewindableEventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.Sequence;
import com.lmax.disruptor.SequenceBarrier;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ExceptionHandlerWrapper;
import com.lmax.disruptor.dsl.ProducerType;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

public class Disruptor2<T> extends Disruptor<T> {

  private final Executor executor;
  private final ConsumerRepository2<T> consumerRepository = new ConsumerRepository2<>();
  private final AtomicBoolean started = new AtomicBoolean(false);
  private ExceptionHandler<? super T> exceptionHandler = new ExceptionHandlerWrapper<>();

  public Disruptor2(EventFactory eventFactory, int ringBufferSize, ThreadFactory threadFactory) {
    super(eventFactory, ringBufferSize, threadFactory);
    executor = new BasicExecutor(threadFactory);
  }

  public Disruptor2(EventFactory eventFactory, int ringBufferSize, ThreadFactory threadFactory, ProducerType producerType, WaitStrategy waitStrategy) {
    super(eventFactory, ringBufferSize, threadFactory, producerType, waitStrategy);
    executor = new BasicExecutor(threadFactory);
  }

  /**
   * <p>Starts the event processors and returns the fully configured ring buffer.</p>
   *
   * <p>The ring buffer is set up to prevent overwriting any entry that is yet to
   * be processed by the slowest event processor.</p>
   *
   * <p>This method must only be called once after all event processors have been added.</p>
   *
   * @return the configured ring buffer.
   */
  public RingBuffer<T> start() {
    checkOnlyStartedOnce();
    for (final ConsumerInfo consumerInfo : consumerRepository) {
      consumerInfo.start(executor);
    }

    return getRingBuffer();
  }
  private void checkOnlyStartedOnce()
  {
    if (!started.compareAndSet(false, true))
    {
      throw new IllegalStateException("Disruptor.start() must only be called once.");
    }
  }

  /**
   * Set up a {@link WorkerPool} to distribute an event to one of a pool of work handler threads. Each event will only be processed by one of the work handlers. The Disruptor will automatically start this processors when {@link #start()} is called.
   *
   * @param workHandlers the work handlers that will process events.
   * @return a {@link EventHandlerGroup} that can be used to chain dependencies.
   */
  @SafeVarargs
  @SuppressWarnings("varargs")
  public final EventHandlerGroup<T> handleEventsWithWorkerPool(final WorkHandler<T>... workHandlers) {
    return createWorkerPool(new Sequence[0], workHandlers);
  }

  EventHandlerGroup<T> createWorkerPool(final Sequence[] barrierSequences, final WorkHandler<? super T>[] workHandlers) {
    final SequenceBarrier sequenceBarrier = getRingBuffer().newBarrier(barrierSequences);
    final WorkerPool<T> workerPool = new WorkerPool<>(getRingBuffer(), sequenceBarrier, exceptionHandler, workHandlers);

    consumerRepository.add(workerPool, sequenceBarrier);

    final Sequence[] workerSequences = workerPool.getWorkerSequences();

    updateGatingSequencesForNextInChain(barrierSequences, workerSequences);

    return new EventHandlerGroup<>(this, consumerRepository, workerSequences);
  }


  EventHandlerGroup<T> createEventProcessors(final Sequence[] barrierSequences, final EventHandler<? super T>[] eventHandlers) {
    checkNotStarted();

    final Sequence[] processorSequences = new Sequence[eventHandlers.length];
    final SequenceBarrier barrier = getRingBuffer().newBarrier(barrierSequences);

    for (int i = 0, eventHandlersLength = eventHandlers.length; i < eventHandlersLength; i++) {
      final EventHandler<? super T> eventHandler = eventHandlers[i];

      final BatchEventProcessor<T> batchEventProcessor = new BatchEventProcessorBuilder().build(getRingBuffer(), barrier, eventHandler);

      if (exceptionHandler != null) {
        batchEventProcessor.setExceptionHandler(exceptionHandler);
      }

      consumerRepository.add(batchEventProcessor, eventHandler, barrier);
      processorSequences[i] = batchEventProcessor.getSequence();
    }

    updateGatingSequencesForNextInChain(barrierSequences, processorSequences);

    return new EventHandlerGroup<>(this, consumerRepository, processorSequences);
  }

  EventHandlerGroup<T> createEventProcessors(final Sequence[] barrierSequences, final BatchRewindStrategy batchRewindStrategy, final RewindableEventHandler<? super T>[] eventHandlers) {
    checkNotStarted();

    final Sequence[] processorSequences = new Sequence[eventHandlers.length];
    final SequenceBarrier barrier = getRingBuffer().newBarrier(barrierSequences);

    for (int i = 0, eventHandlersLength = eventHandlers.length; i < eventHandlersLength; i++) {
      final RewindableEventHandler<? super T> eventHandler = eventHandlers[i];

      final BatchEventProcessor<T> batchEventProcessor = new BatchEventProcessorBuilder().build(getRingBuffer(), barrier, eventHandler, batchRewindStrategy);

      if (exceptionHandler != null) {
        batchEventProcessor.setExceptionHandler(exceptionHandler);
      }

      consumerRepository.add(batchEventProcessor, eventHandler, barrier);
      processorSequences[i] = batchEventProcessor.getSequence();
    }

    updateGatingSequencesForNextInChain(barrierSequences, processorSequences);

    return new EventHandlerGroup<>(this, consumerRepository, processorSequences);
  }

  private void checkNotStarted() {
    if (started.get()) {
      throw new IllegalStateException("All event handlers must be added before calling starts.");
    }
  }

  private void updateGatingSequencesForNextInChain(final Sequence[] barrierSequences, final Sequence[] processorSequences) {
    if (processorSequences.length > 0) {
      getRingBuffer().addGatingSequences(processorSequences);
      for (final Sequence barrierSequence : barrierSequences) {
        getRingBuffer().removeGatingSequence(barrierSequence);
      }
      consumerRepository.unMarkEventProcessorsAsEndOfChain(barrierSequences);
    }
  }
}
