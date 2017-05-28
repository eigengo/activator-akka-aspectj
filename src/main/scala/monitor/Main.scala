package monitor

import akka.actor.{ActorRef, ActorSystem}
import java.io.{InputStreamReader, BufferedReader}

object Main extends App {
  import akka.actor.ActorDSL._
  import Commands._

  implicit val system = ActorSystem()

  val chatter = actor(new Act {
    become {
      case i: Int =>
        self ! (sender, i)
      case (sender: ActorRef, i: Int) =>
        if (i > 0)
          self ! (sender, i - 1)
        else
          sender ! "zero"
    }
  })
  implicit val _ = actor(new Act {
    become {
      case x => println(">>> " + x)
    }
  })

  def commandLoop(): Unit = {
    readLine() match {
      case CountdownCommand(count) => chatter ! count.toInt
      case QuitCommand             => return
      case x                       => println(s"WTF '$x'")
    }

    commandLoop()
  }

  commandLoop()
  system.terminate()

}

object Commands {
  val CountdownCommand = """(\d+)""".r
  val QuitCommand      = "quit"
}