package org.multibot

import org.jibble.pircbot.PircBot

import java.io.{PrintStream, ByteArrayOutputStream}

object Multibottest extends PircBot {
    val stdOut = System.out
    val stdErr = System.err

    val PRODUCTION = Option(System getProperty "multibot.production") map (_ toBoolean) getOrElse false
    val BOTNAME = if (PRODUCTION) "multibot210_" else "multibot210__"
    val BOTMSG = BOTNAME + ":"
    val NUMLINES = 5
    val INNUMLINES = 8
    val LAMBDABOT = "lambdabot"
    val LAMBDABOTIGNORE = Set("#scala", "#scalaz")
    val ADMINS = List("****")


    def main(args: Array[String]) {
        setName(BOTNAME)
        setVerbose(true)
        setEncoding("UTF-8")
        connect()
    }

    def connect() {
        connect("irc.freenode.net")
        val channels = if (PRODUCTION) List("#clojure.pl", "#scala.pl", "#scala", "#scalaz", "#lift", "#playframework", "#fp-in-scala", "#progfun") else List("#multibottest")
        channels foreach joinChannel
    }

    override def onDisconnect: Unit = while (true)
        try {
            connect()
            return
        } catch { case e: Exception =>
            e.printStackTrace
            Thread sleep 10000
        }

    var lastChannel: Option[String] = None

    override def onPrivateMessage(sender: String, login: String, hostname: String, message: String) = sender match {
        case LAMBDABOT => lastChannel foreach (sendMessage(_, message))
        case _         => onMessage(sender, sender, login, hostname, message)
    }

    override def onNotice(sender: String, login: String, hostname: String, target: String, notice: String) = sender match {
        case LAMBDABOT => lastChannel foreach (sendNotice(_, notice))
        case _ =>
    }

    override def onAction(sender: String, login: String, hostname: String, target: String, action: String) = sender match {
        case LAMBDABOT => lastChannel foreach (sendAction(_, action))
        case _ =>
    }

    override def onMessage(channel: String, sender: String, login: String, hostname: String, message: String) =
        serve(Msg(channel, sender, login, hostname, message))

    object      Cmd {def unapply(s: String) = if (s.contains(' ')) Some(s.split(" ", 2).toList) else None}

    case class Msg(channel: String, sender: String, login: String, hostname: String, message: String)

    val conOut = new ByteArrayOutputStream
    val conOutStream = new PrintStream(conOut)
    val writer = new java.io.PrintWriter(conOutStream)

    def captureOutput(block: => Unit) = try {
        System setOut conOutStream
        System setErr conOutStream
        block
    } finally {
        System setOut stdOut
        System setErr stdErr
        conOut.flush
        conOut.reset
    }

    import scala.tools.nsc.interpreter.{IMain}

    val scalaInt = scala.collection.mutable.Map[String, IMain]()
    def scalaInterpreter(channel: String)(f: (IMain, ByteArrayOutputStream) => Unit) = this.synchronized {
        val si = scalaInt.getOrElseUpdate(channel, {
            val settings = new scala.tools.nsc.Settings(null)
            settings.usejavacp.value = true
            settings.deprecation.value = true
            // settings.YdepMethTpes.value = true
            val si = new IMain(settings, writer) { override def parentClassLoader = Thread.currentThread.getContextClassLoader }

            si.quietImport("scalaz._")
            si.quietImport("Scalaz._")
            si.quietImport("org.scalacheck.Prop._")
            si
        })
        captureOutput{f(si, conOut)}
    }

    def sendLines(channel: String, message: String) = message split ("\n") filter (! _.isEmpty) take NUMLINES foreach (m => sendMessage(channel, " " + (if (!m.isEmpty && m.charAt(0) == 13) m.substring(1) else m)))

    def serve(implicit msg: Msg): Unit = msg.message match {
        case Cmd(BOTMSG :: m :: Nil) if ADMINS contains msg.sender => m match {
            case Cmd("join" :: ch :: Nil) => joinChannel(ch)
            case Cmd("leave" :: ch :: Nil) => partChannel(ch)
            case Cmd("reply" :: ch :: Nil) => sendMessage(msg.channel, ch)
            case _ => sendMessage(msg.channel, "unknown command")
        }

        // case "@listchans" => sendMessage(msg.channel, getChannels mkString " ")

        case "@bot" | "@bots" => sendMessage(msg.channel, ":)")
        case "@help" => sendMessage(msg.channel, "(!!!) scala (reset|type|scalex)")

        case Cmd("!!!" :: m :: Nil) => scalaInterpreter(msg.channel){(si, cout) =>
            import scala.tools.nsc.interpreter.Results._
            val lines = (si interpret m match {
                case Success => cout.toString.replaceAll("(?m:^res[0-9]+: )", "") // + "\n" + iout.toString.replaceAll("(?m:^res[0-9]+: )", "")
                case Error => cout.toString.replaceAll("^<console>:[0-9]+: ", "")
                case Incomplete => "error: unexpected EOF found, incomplete expression"
            })
            sendLines(msg.channel, lines)
            //.split("\n") take NUMLINES foreach (m => sendMessage(msg.channel, " " + (if (m.charAt(0) == 13) m.substring(1) else m)))
        }

        case Cmd("!!!type" :: m :: Nil) => scalaInterpreter(msg.channel)((si, cout) => sendMessage(msg.channel, (si.typeOfExpression(m).directObjectString)))
        case "!!!reset" => scalaInt -= msg.channel
        case "!!!reset-all" => scalaInt.clear

        case _ =>
    }
}
