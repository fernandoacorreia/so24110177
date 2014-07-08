package controllers

import play.api._
import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._
import play.api.Play.current
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.util._

object Application1 extends Controller {

  def factorial(n: Int) = Action.async {
    computeFactorial(n, 3.seconds).map { result =>
      result match {
        case Success(i) => Ok(s"$n! = $i")
        case Failure(ex) => InternalServerError(ex.getMessage)
      }
    }
  }

  def computeFactorial(n: BigInt, timeout: Duration): Future[Try[BigInt]] = {

    val startTime = System.nanoTime()
    val maxTime = timeout.toNanos

    def factorial(n: BigInt, result: BigInt = 1): BigInt = {
      // Calculate elapsed time.
      val elapsed = System.nanoTime() - startTime
      Logger.debug(s"Computing factorial($n) with $elapsed nanoseconds elapsed.")

      // Abort computation if timeout was exceeded.
      if (elapsed > maxTime) {
        Logger.debug(s"Timeout exceeded.")
        throw new ComputationTimeoutException("The maximum time for the computation was exceeded.")
      }

      // Introduce an artificial delay so that less iterations are required to produce the error.
      Thread.sleep(100)

      // Compute step.
      if (n == 0) result else factorial(n - 1, n * result)
    }

    Future {
      try {
        Success(factorial(n))
      } catch {
        case ex: Exception => Failure(ex)
      }
    }(Contexts.computationContext)
  }

}

class ComputationTimeoutException(msg: String) extends RuntimeException(msg)

object Contexts {
  implicit val computationContext: ExecutionContext = Akka.system.dispatchers.lookup("contexts.computationContext")
}
