/*
 * Copyright (C) 2009-2017 Lightbend Inc. <https://www.lightbend.com>
 */
package play.api.libs.concurrent

import akka.actor.{Actor, ActorRef}
import akka.pattern.ask
import com.google.inject.name.Named
import com.google.inject.{AbstractModule, Inject}
import org.specs2.mutable.Specification
import play.api.inject.guice.GuiceInjectorBuilder
import play.api.libs.concurrent.AkkaGuiceSupportSpec.TestActor

import scala.util.Random

class AkkaGuiceSupportSpec extends Specification {
  "AkkaGuiceSupportSpec" should {
    "bind actor" in {
      val injector = new GuiceInjectorBuilder()
        .overrides(new AkkaGuiceSupportSpec.Module)
        .injector()

      (injector.instanceOf[AkkaGuiceSupportSpec.A].actor ? TestActor).mapTo[String].map {
        _ must be equalTo AkkaGuiceSupportSpec.message
      }
    }

    "require module" in {
      val injector = new GuiceInjectorBuilder()
        .injector()

      injector.instanceOf[AkkaGuiceSupportSpec.A] must throwAn[Exception]
    }
  }
}

object AkkaGuiceSupportSpec {
  private[this] val rand = new Random()
  private val message: String = rand.nextString(rand.nextInt())

  private object TestActor {
    case object Message
  }

  private class TestActor @Inject() extends Actor {
    import TestActor._

    override def receive: Receive = {
      case Message =>
        sender() ! message
    }
  }

  private class Module extends AbstractModule with AkkaGuiceSupport {
    override def configure(): Unit = {
      bindActor[TestActor]("test-actor")
    }
  }

  private class A @Inject() (@Named("test-actor") val actor: ActorRef)
}
