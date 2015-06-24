import java.util.NoSuchElementException

import reactivemongo.core.commands.LastError

import scala.util.Try

val x = Try (doSomething()).getOrElse(LastError(false, None, None, None, None, 0, false))



def doSomething(): LastError = {
  //LastError(false, None, None, None, None, 0, false)
  throw new NoSuchElementException("Dummy test")
}