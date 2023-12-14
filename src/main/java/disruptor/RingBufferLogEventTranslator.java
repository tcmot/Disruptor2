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

  private String name;

  public String getName() {
    return this.name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  @Override
  public void translateTo(User lr, long sequence) {
     lr.setUsername(getName());
  }
}
