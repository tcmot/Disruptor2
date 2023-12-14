package disruptor;

import com.lmax.disruptor.EventTranslator;
import java.util.UUID;

/**
 * .
 *
 * <p>.
 *
 * @author admin
 */
public class RingBufferLogEventTranslator implements EventTranslator<User> {


  @Override
  public void translateTo(User lr, long sequence) {
     lr.setUsername(UUID.randomUUID().toString());
  }
}
