package object concurrent {
  def threadName(): String = Thread.currentThread().getName
}
