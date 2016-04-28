package nl.rddesmit

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.{ExecutionContext, Future}

/**
 * Created by rudies on 28-4-2016.
 */
object Retry{

  /**
    * Java API: Akka 'ask' method with N retries.
    */
  def retryJava(to: ActorRef, message: Object, retries: Int, timout: Timeout, executor: ExecutionContext): Future[Any] = {
    implicit val _t = timout
    implicit val _e = executor

    retry(() => to ? message, retries)
  }


  /**
    * Try to re-execute a action N times before it fails.
    */
  def retry(action: () => Future[Any], retries: Int)(implicit timout: Timeout, executor: ExecutionContext): Future[Any] = {
    action() recoverWith { case _ => if (retries >= 0) retry(action, retries -1) else throw new Exception }
  }
}
