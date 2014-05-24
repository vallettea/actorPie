package com.ants.pi

import akka.actor._
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import akka.cluster.ClusterEvent.{MemberUp, ClusterDomainEvent}
import akka.cluster.Cluster
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.concurrent.duration._

object Server extends App {


  val conf = ConfigFactory.load

  val system = ActorSystem("PieServer", conf)


  val ticker = system.actorOf(Props[Ticker], name = "ticker")

  Cluster(system).subscribe(ticker, classOf[ClusterDomainEvent])

}

class Ticker extends Actor with ActorLogging {

  implicit val ctx :ExecutionContext = context.dispatcher
  implicit val timeout: Timeout = Timeout(5 seconds)

  var ticker :Cancellable = _


  def receive = {

    case Pong ⇒
      log.info("Got Pong from {}", sender)
      context watch sender

      

      ticker = context.system.scheduler.schedule(0 milliseconds, 1000 milliseconds)({
            val future = sender ? MeasureAnalog(0)
            val result = Await.result(future, timeout.duration).asInstanceOf[Int]
            println(result)
          })

    case Terminated(x) ⇒
      log.info("Terminating {}", x)
      ticker.cancel


    case MemberUp(member) if member.roles.contains("pi")⇒
      log.info("Raspberry Pi is up: {}", member.address)
      val adcMonitor = context.actorSelection(RootActorPath(member.address) / "user" / "ADCmonitor")
      adcMonitor ! Ping

  }
}




