package controllers

import play.api._
import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._
import play.api.Play.current
import scala.concurrent._
import scala.concurrent.duration._
import scala.util._
import scala.concurrent.Promise

object Application2 extends Controller {

  def factorial(n: Int) = Action.async {
    computeFactorial(n, 3.seconds).map { i => Ok(s"$n! = $i") }
  }

  def computeFactorial(n: BigInt, timeout: Duration): Future[BigInt] = {
    val startTime = System.nanoTime()
    val maxTime = timeout.toNanos

    def factorial(n: BigInt, result: BigInt = 1): BigInt = {
      if (System.nanoTime() - startTime > maxTime) {
        throw new RuntimeException("The maximum time for the computation was exceeded.")
      }
      Thread.sleep(100)
      if (n == 0) result else factorial(n - 1, n * result)
    }

    Future { factorial(n) }(Akka.system.dispatchers.lookup("contexts.computationContext"))
  }

}
