package com.ants.pi

import akka.actor._
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import akka.cluster.ClusterEvent.{MemberUp, ClusterDomainEvent}
import akka.cluster.Cluster

object Server extends App {


  val conf = ConfigFactory.load

  val system = ActorSystem("PieServer", conf)


  val ticker = system.actorOf(Props[Ticker], name = "ticker")

  Cluster(system).subscribe(ticker, classOf[ClusterDomainEvent])

}

class Ticker extends Actor with ActorLogging {

  implicit val ctx :ExecutionContext = context.dispatcher

  var ticker :Cancellable = _


  def receive = {

    case Pong ⇒
      log.info("Got Pong from {}", sender)
      context watch sender

      // ticker = context.system.scheduler.schedule(0 milliseconds,
      //     500 milliseconds,
      //     sender,
      //     Led.TOGGLE)

    case Terminated(x) ⇒
      log.info("Terminating {}", x)
      ticker.cancel


    case MemberUp(member) if member.roles.contains("pi")⇒
      log.info("Raspberry Pi is up: {}", member.address)
      val adcMonitor = context.actorSelection(RootActorPath(member.address) / "user" / "ADCmonitor")
      adcMonitor ! Ping

  }
}




