package nl.rddesmit

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl._
import akka.util.Timeout

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.{Random, Success}

object Main{
  implicit val system = ActorSystem("akka")
  implicit val materializer = ActorMaterializer()
  implicit val executor = system.dispatcher

  def main(args: Array[String]): Unit= {
    val code = Flow[String]
      .mapAsync(1)(url => retry(3){Future { randomError("code") }})

    val token = Flow[String]
      .mapAsync(1)(code => retry(3){Future { randomError("token")}})
      .completionTimeout(Duration(500, TimeUnit.MILLISECONDS))

    val source = Source.single("url")
    val sink = Sink.foreach(println)

    source.via(code).via(token).to(sink).run()

    //system.terminate()
    //Await.result(system.whenTerminated, Duration.Inf)
  }

  def retry[T](times: Int)(future: => Future[T]): Future[T]= {
    future.recoverWith({
      case error =>
        if(times > 0) retry(times-1)(future)
        else future
    })
  }

  def randomError[T](result: T): T={
    if(Random.nextBoolean()) result
    else throw new RuntimeException
  }
}