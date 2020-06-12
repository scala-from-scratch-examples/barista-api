package concurrent

import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.ThreadFactory

class NamedThreadFactory(namePrefix: String) extends ThreadFactory {
    private val threadCount = new AtomicLong(0L)
    def newThread(runnable: Runnable): Thread = {
      val count = threadCount.incrementAndGet()
      new Thread(runnable, namePrefix + "-" + count)
    }
 }
