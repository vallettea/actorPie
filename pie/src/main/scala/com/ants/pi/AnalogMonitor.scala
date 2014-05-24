package com.ants.pi

import akka.actor.{RootActorPath, ActorLogging, Actor}
import akka.cluster.ClusterEvent.MemberUp
import com.pi4j.io.gpio.{RaspiPin, GpioFactory, Pin}

class AnalogMonitor extends Actor with ActorLogging {

	val gpio = GpioFactory.getInstance()
	val (clockpin, mosipin, misopin, cspin) = (1,5,4,6)
	val CSPIN = getDigitalOutput(getPin(cspin))
    val CLOCK = getDigitalOutput(getPin(clockpin))
    val MOSI = getDigitalOutput(getPin(mosipin))
    val MISO = getDigitalInput(getPin(misopin))

	def receive = {
		case MeasureAnalog(entry) ⇒ sender ! readAdc(entry)
		case Ping     ⇒ sender ! Pong
	}

	def getPin(no: Int): Pin = no match {
		case 0 ⇒ RaspiPin.GPIO_00
		case 1 ⇒ RaspiPin.GPIO_01
		case 2 ⇒ RaspiPin.GPIO_02
		case 3 ⇒ RaspiPin.GPIO_03
		case 4 ⇒ RaspiPin.GPIO_04
		case 5 ⇒ RaspiPin.GPIO_05
		case 6 ⇒ RaspiPin.GPIO_06
		case 7 ⇒ RaspiPin.GPIO_07
		case 8 ⇒ RaspiPin.GPIO_08
		case 9 ⇒ RaspiPin.GPIO_09
		case 10 ⇒ RaspiPin.GPIO_10
		case 11 ⇒ RaspiPin.GPIO_11
		case 12 ⇒ RaspiPin.GPIO_12
		case 13 ⇒ RaspiPin.GPIO_13
		case 14 ⇒ RaspiPin.GPIO_14
		case 15 ⇒ RaspiPin.GPIO_15
		case 16 ⇒ RaspiPin.GPIO_16
		case 17 ⇒ RaspiPin.GPIO_17
		case 18 ⇒ RaspiPin.GPIO_18
		case 19 ⇒ RaspiPin.GPIO_19
		case 20 ⇒ RaspiPin.GPIO_20
		case n ⇒ throw new IllegalArgumentException(s"Unknown pin $n")
	}

  	def getDigitalOutput(pin: Pin) = gpio.provisionDigitalOutputPin(pin)
  	def getDigitalInput(pin: Pin) = gpio.provisionDigitalInputPin(pin)

	def readAdc( adcnum: Int): Int = {
	    if( (adcnum > 7) || (adcnum < 0)){
	        -1
	    } else {

		    CSPIN.high
		    CLOCK.low
		    CSPIN.low

		    var commandout = adcnum
		    commandout |= 0x18  // OR to add Start bit + signle-ended bit
		                        // 0x18 = 24d = 00011000b
		    commandout <<=3     // shift 3 bit left

		    // send bits on SPI bus
		    for (i <- (0 until 5)) {
		            // do an AND to dertermine the bit of strongest weight 
		            // (0x80 = 128d = 10000000b)
		            if( (commandout & 0x80) > 0 ) { // do AND to determine the state of the bit
		                    MOSI.high
		            } else{
		                    MOSI.low
		            }
		            commandout <<= 1 // shift one bit left

		            // send mosipin with clock
		            CLOCK.high
		            CLOCK.low
		    }

		    // read bits sent by MCP3008
		    // read one empty bit, 10 bits of data and one null bit
		    var adcout = 0
		    for (i <- (0 until 12)) {
		            // clock so that  MCP3008 place one bit
		            CLOCK.high
		            CLOCK.low
		            // shift one bit left
		            adcout <<= 1
		            // store one bit depending on miso pin
		            if(MISO.isHigh){
		                    adcout |= 0x1 // activate the bit with OR
		                }
		    }

		    // put chip select to high (desactivate le MCP3008)
		    CSPIN.high

		    // The first bit (the one of smallest weight, the last read)
		    // is null . so we shift right to cancel it
		    adcout >>= 1

		    adcout
		}
	}
}