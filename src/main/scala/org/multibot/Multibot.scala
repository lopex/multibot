package org.multibot

import org.jibble.pircbot.PircBot
import dispatch._
import Http._
import org.json4s.native.JsonMethods._
import org.json4s.native._
import org.json4s.JsonAST._
import org.json4s.JsonDSL._

import java.io.{PrintStream, ByteArrayOutputStream}

object Multibottest extends PircBot {
    val PRODUCTION = Option(System getProperty "multibot.production") map (_ toBoolean) getOrElse false
    val BOTNAME = if (PRODUCTION) "multibot_" else "multibot__"
    val BOTMSG = BOTNAME + ":"
    val NUMLINES = 5
    val INNUMLINES = 8
    val LAMBDABOT = "lambdabot"
    val LAMBDABOTIGNORE = Set("#scala", "#scalaz")
    val ADMINS = List("imeredith", "lopex", "tpolecat")

    def main(args: Array[String]) {
        setName(BOTNAME)
        setVerbose(true)
        setEncoding("UTF-8")
        connect()
    }


    def connect() {
        connect("irc.freenode.net")
        val channels = if (PRODUCTION)
            List("#clojure.pl", "#scala.pl", "#jruby", "#ruby.pl", "#rubyonrails.pl", "#scala", "#scalaz", "#scala-fr", "#lift", "#playframework", "#bostonpython", "#fp-in-scala", "#CourseraProgfun")
        else
            List("#multibottest", "#multibottest2")

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
    object PasteCmd { def unapply(s: String) = if (s.indexOf("paste") == 1 && s.length > "?paste ".length) Some((s.substring(0, 1), s.substring("?paste ".length))) else None }

    case class Msg(channel: String, sender: String, login: String, hostname: String, message: String)

    val stdOut = System.out
    val stdErr = System.err
    val conOut = new ByteArrayOutputStream
    val conOutStream = new PrintStream(conOut)
    val conStdOut = Console.out
    val conStdErr = Console.err

    def captureOutput(block: => Unit) = try {
        System setOut conOutStream
        System setErr conOutStream
        Console setOut conOutStream
        Console setErr conOutStream
        block
    } finally {
        System setOut stdOut
        System setErr stdErr
        Console setOut conStdOut
        Console setErr conStdErr
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
            settings.feature.value = false
            // settings.YdepMethTpes.value = true
            val si = new IMain(settings) // { override def parentClassLoader = Thread.currentThread.getContextClassLoader }

            si.quietImport("scalaz._")
            si.quietImport("Scalaz._")
            si.quietImport("Tags._")
            si.quietImport("reflect.runtime.universe.reify")

            si.quietImport("org.scalacheck.Prop._")
            si
        })
        captureOutput{f(si, conOut)}
    }

    import org.jruby.{RubyInstanceConfig, Ruby}
    import org.jruby.runtime.scope.{ManyVarsDynamicScope}

    val jrubyInt = scala.collection.mutable.Map[String, (Ruby, ManyVarsDynamicScope)]()
    def jrubyInterpreter(channel: String)(f: (Ruby, ManyVarsDynamicScope, ByteArrayOutputStream) => Unit) = this.synchronized {
        val (jr, sc) = jrubyInt.getOrElseUpdate(channel, {
            val config = new RubyInstanceConfig
            config setOutput conOutStream
            config setError conOutStream
            config setInternalEncoding "utf-8"
            config setExternalEncoding "utf-8"

            val jruby = Ruby.newInstance(config)
            val scope = new ManyVarsDynamicScope(jruby.getStaticScopeFactory.newEvalScope(jruby.getCurrentContext.getCurrentScope.getStaticScope), jruby.getCurrentContext.getCurrentScope)
            (jruby, scope)
        })
        captureOutput{f(jr, sc, conOut)}
    }

    //import org.python.util.PythonInterpreter
    //val jythonInt = scala.collection.mutable.Map[String, PythonInterpreter]()
    //def jythonInterpreter(channel: String)(f: (PythonInterpreter, ByteArrayOutputStream) => Unit) = this.synchronized {
    //    val jy = jythonInt.getOrElseUpdate(channel, new PythonInterpreter())
    //    captureOutput{f(jy, conOut)}
    //}

    var pythonSession = ""
    def sendLines(channel: String, message: String) = message split ("\n") filter (! _.isEmpty) take NUMLINES foreach (m => sendMessage(channel, " " + (if (!m.isEmpty && m.charAt(0) == 13) m.substring(1) else m)))

    def serve(implicit msg: Msg): Unit = msg.message match {
        case Cmd(BOTMSG :: m :: Nil) if ADMINS contains msg.sender => m match {
            case Cmd("join" :: ch :: Nil) => joinChannel(ch)
            case Cmd("leave" :: ch :: Nil) => partChannel(ch)
            case Cmd("reply" :: ch :: Nil) => sendMessage(msg.channel, ch)
            case Cmd("cookies":: "" :: Nil) => sendMessage(msg.channel, cookies.map{case (k, v) => k + " -> " + v}.mkString(" - ")) //cookies foreach {case(k, v) => sendMessage(msg.channel, k + " -> " + v)}
            case _ => sendMessage(msg.channel, "unknown command")
        }

        // case "@listchans" => sendMessage(msg.channel, getChannels mkString " ")

        case "@bot" | "@bots" => sendMessage(msg.channel, ":)")
        case "@help" => sendMessage(msg.channel, "(!) scala (!reset|type|scalex), (%) ruby (%reset), (,) clojure, (>>) haskell, (^) python, (&) javascript, (##) groovy, (<prefix>paste url), lambdabot relay (" + !LAMBDABOTIGNORE.contains(msg.channel) + "), url: https://github.com/lopex/multibot")

        case Cmd("!" :: m :: Nil) => scalaInterpreter(msg.channel){(si, cout) =>
            import scala.tools.nsc.interpreter.Results._
            sendLines(msg.channel, (si interpret m match {
                case Success => cout.toString.replaceAll("(?m:^res[0-9]+: )", "") // + "\n" + iout.toString.replaceAll("(?m:^res[0-9]+: )", "")
                case Error => cout.toString.replaceAll("^<console>:[0-9]+: ", "")
                case Incomplete => "error: unexpected EOF found, incomplete expression"
            }))
            //.split("\n") take NUMLINES foreach (m => sendMessage(msg.channel, " " + (if (m.charAt(0) == 13) m.substring(1) else m)))
        }

        case PasteCmd(cmd, m) => // Http(url(m) >- {source => serve(msg.copy(message = "! " + source))})
            val conOut = new ByteArrayOutputStream
            (new Http with NoLogging)(url(m) >>> new PrintStream(conOut))
            serve(msg.copy(message = cmd + " " + conOut))

        case Cmd("!type" :: m :: Nil) => scalaInterpreter(msg.channel)((si, cout) => sendMessage(msg.channel, si.typeOfExpression(m).directObjectString))
        case "!reset" => scalaInt -= msg.channel
        case "!reset-all" => scalaInt.clear

        case Cmd("!scalex" :: m :: Nil) => respondJSON(:/("api.scalex.org") <<? Map("q" -> m)) { json => Some((
            for{
                    JObject(obj) <- json
                    JField("results", JArray(arr)) <- obj
                    JObject(res) <- arr
                    JField("resultType", JString(rtype)) <- res

                    JField("parent", JObject(parent)) <- res
                    JField("name", JString(pname)) <- parent
                    JField("typeParams", JString(ptparams)) <- parent

                    JField("name", JString(name)) <- res
                    JField("typeParams", JString(tparams)) <- res

                    JField("comment", JObject(comment)) <- res
                    JField("short", JObject(short)) <- comment
                    JField("txt", JString(txt)) <- short

                    JField("valueParams", JString(vparams)) <- res
            } yield pname + ptparams + " " + name + tparams + ": " + vparams + ": " + rtype + " '" + txt + "'").mkString("\n"))
        }

        case Cmd("!!" :: m :: Nil) => respond(:/("www.simplyscala.com") / "interp" <<? Map("bot" -> "irc", "code" -> m)) {
            case "warning: there were deprecation warnings; re-run with -deprecation for details" |
                 "warning: there were unchecked warnings; re-run with -unchecked for details" |
                 "New interpreter instance being created for you, this may take a few seconds." |"Please be patient." => None
            case line => Some(line.replaceAll("^res[0-9]+: ", ""))
        }

        case Cmd("," :: m :: Nil) => respondJSON(:/("try-clojure.org") / "eval.json" <<? Map("expr" -> m)) {
            case JObject(JField("expr", JString(_)) :: JField("result", JString(result)) :: Nil) => Some(result)
            case JObject(JField("error", JBool(true)) :: JField("message", JString(message)) :: Nil) => Some(message)
            case e => Some("unexpected: " + e)
        }

        case Cmd(">>" :: m :: Nil) => respondJSON(:/("tryhaskell.org") / "haskell.json" <<? Map("method" -> "eval", "expr" -> m)) {
            case JObject(JField("result", JString(result)) :: JField("type", JString(xtype)) :: JField("expr", JString(_)) :: Nil) => Some(result + " :: " + xtype)
            case JObject(JField("error", JString(error)) :: Nil) => Some(error)
            case e => Some("unexpected: " + e)
        }

        case Cmd("%%" :: m :: Nil) => respondJSON(:/("tryruby.org") / "/levels/1/challenges/0" <:<
                    Map("Accept" -> "application/json, text/javascript, */*; q=0.01",
                        "Content-Type" -> "application/x-www-form-urlencoded; charset=UTF-8",
                        "X-Requested-With" -> "XMLHttpRequest",
                        "Connection" -> "keep-alive") <<< "cmd=" + java.net.URLEncoder.encode(m, "UTF-8")) {
            case JObject(JField("success", JBool(true)) :: JField("output", JString(output)) :: _) => Some(output)
            case JObject(JField("success", JBool(false)) :: _ :: JField("result", JString(output)) :: _) => Some(output)
            case e => Some("unexpected: " + e)
        }

        case "%reset" => jrubyInt -= msg.channel
        case "%reset-all" => jrubyInt.clear

        case Cmd("%" :: m :: Nil) => jrubyInterpreter(msg.channel){(jr, sc, cout) =>
            try {
                val result = jr.evalScriptlet("# coding: utf-8\n" + m, sc).toString
                sendLines(msg.channel, cout.toString)
                sendLines(msg.channel, result.toString)
            } catch {
                case e: Exception => sendMessage(msg.channel, e.getMessage)
            }
        }

        case Cmd("&" :: m :: Nil) =>
            val src = """
                var http = require('http');
    
                http.createServer(function (req, res) {
                  res.writeHead(200, {'Content-Type': 'text/plain'});
                  var a = (""" + m + """) + "";
                  res.end(a);
                }).listen();
            """

            respondJSON((:/("jsapp.us") / "ajax" << compact(render( ("actions", List(("action", "test") ~ ("code", src) ~ ("randToken", "3901") ~ ("fileName", ""))) ~ ("user", "null") ~ ("token", "null"))))) {
                case JObject(JField("user", JNull) :: JField("data", JArray(JString(data) :: Nil)) :: Nil) => var s: String = ""; (new Http with NoLogging)(url(data) >- {source => s = source}); Some(s)
                case e => Some("unexpected: " + e)
            }


        //case Cmd("^" :: m :: Nil) => jythonInterpreter(msg.channel){(jy, cout) =>
        //    try {
        //        //val result = jr.evalScriptlet(m, sc).toString
        //        jy.exec(m)
        //        sendLines(msg.channel, cout.toString)
        //        //sendLines(msg.channel, result.toString)
        //    } catch {
        //        case e: Exception => sendMessage(msg.channel, e.getMessage)
        //    }
        //}

        case Cmd("^" :: m :: Nil) => respondJSON2(:/("try-python.appspot.com") / "json" << compact(render( ("method", "exec") ~ ("params", List(pythonSession, m)) ~ ("id" -> "null") )),
                                                 :/("try-python.appspot.com") / "json" << compact(render( ("method", "start_session") ~ ("params", List[String]()) ~ ("id" -> "null") ))) {
            case JObject(JField("error", JNull) :: JField("id" , JString("null")) :: JField("result", JObject(JField("text", JString(result)) :: _)) :: Nil) => Some(result)
            case e => Some("unexpected: " + e)
        } {
            case JObject(_ :: _ :: JField("result" , JString(session)) :: Nil) => pythonSession = session; None
            case e => None
        }

        case Cmd("##" :: m :: Nil) => respondJSON(:/("groovyconsole.appspot.com") / "executor.groovy"  <<? Map("script" -> m), true) {
            case JObject(JField("executionResult", JString(result)) :: JField("outputText", JString(output)) :: JField("stacktraceText", JString("")) :: Nil) => Some(result.trim + "\n" + output.trim)
            case JObject(JField("executionResult", JString("")) :: JField("outputText", JString("")) :: JField("stacktraceText", JString(err)) :: Nil) => Some(err)
            case e => Some("unexpected" + e)
        }

        case m if (m.startsWith("@") || m.startsWith(">") || m.startsWith("?")) && m.trim.length > 1 && !LAMBDABOTIGNORE.contains(msg.channel) =>
            lastChannel = Some(msg.channel)
            sendMessage(LAMBDABOT, m)

        case _ =>
    }

    val cookies = scala.collection.mutable.Map[String, String]()

    def respondJSON(req: Request, join: Boolean = false)(response: JValue => Option[String])(implicit msg: Msg) = respond(req, join){line => response(JsonParser.parse(line))}

    def respondJSON2(req: Request, init: Request)(response: JValue => Option[String])(initResponse: JValue => Option[String])(implicit msg: Msg) = try {
        respond(req){line => response(JsonParser.parse(line))}
    } catch {
        case t:Throwable =>
            respond(init){line => initResponse(JsonParser.parse(line))}
            respond(req){line => response(JsonParser.parse(line))}
    }

    def respond(req: Request, join: Boolean = false)(response: String => Option[String])(implicit msg: Msg) = {
        val Msg(channel, sender, login, hostname, message) = msg
        val host = req.host

        val request = cookies.get(channel + host) map (c => req <:< Map("Cookie" -> c)) getOrElse req
//         val request = cookies.get(channel + host) map (c => req <:< Map("Cookie" -> c)) getOrElse {
//             val req2 = req >:> { headers => println(headers.get("Set-Cookie"))}
//             (new Http)(req2)
//             req
//         }

        val handler = request >+> { r =>
            r >:> { headers => headers.get("Set-Cookie").foreach(h => h.foreach(c => cookies(channel + host) = c.split(";").head))
            r >~  { source =>
                val lines = source.getLines.take(NUMLINES)
                (if (join) List(lines.mkString) else lines).foreach(line => response(line).foreach(l => l.split("\n").take(INNUMLINES).foreach(ml => sendMessage(channel, ml))))
        }}}                                        // non empty lines

        (new Http with NoLogging)(handler) //  with thread.Safety
    }
}
