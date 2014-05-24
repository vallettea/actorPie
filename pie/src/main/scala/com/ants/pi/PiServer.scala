package com.ants.pi

import akka.actor.{Actor, ActorLogging, Props, ActorSystem}
import com.typesafe.config.ConfigFactory
import akka.cluster.ClusterEvent._
import akka.cluster.ClusterEvent.CurrentClusterState
import akka.cluster.ClusterEvent.MemberRemoved
import akka.cluster.ClusterEvent.MemberUp
import akka.cluster.ClusterEvent.UnreachableMember
import akka.cluster.Cluster


object PiServer extends App {

  val conf = ConfigFactory.load

  val system = ActorSystem("PieServer", conf)

  val actor = system.actorOf(Props(new AnalogMonitor ), name = "ADCmonitor")

  Cluster(system).subscribe(actor, classOf[ClusterDomainEvent])

}
