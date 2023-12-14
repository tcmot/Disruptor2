package disruptor;

import com.lmax.disruptor.RingBuffer;

/**
 * Callback interface to be implemented for processing units of work as they become available in the {@link RingBuffer}.
 *
 * @param <T> event implementation storing the data for sharing during exchange or parallel coordination of an event.
 * @see WorkerPool
 */
public interface WorkHandler<T>
{
  /**
   * Callback to indicate a unit of work needs to be processed.
   *
   * @param event published to the {@link RingBuffer}
   * @throws Exception if the {@link WorkHandler} would like the exception handled further up the chain.
   */
  void onEvent(T event) throws Exception;

}
