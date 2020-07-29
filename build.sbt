import sbt._
import sbt.Keys._
import Dependencies._
import Versions._

name := "KnativeSamples"

lazy val thisVersion = "0.1"
organization in ThisBuild := "lightbend"
version in ThisBuild := thisVersion
scalaVersion in ThisBuild := "2.12.10"

// settings for a native-packager based docker project based on sbt-docker plugin
def sbtdockerAppBase(id: String)(base: String = id): Project = Project(id, base = file(base))
  .enablePlugins(sbtdocker.DockerPlugin, JavaAppPackaging)
  .settings(
    dockerfile in docker := {
      val appDir = stage.value
      val targetDir = "/opt/app"

      new Dockerfile {
        from("lightbend/java-bash-base:0.0.1")
        copy(appDir, targetDir)
        run("chmod", "-R", "777", "/opt/app")
        entryPoint(s"$targetDir/bin/${executableScriptName.value}")
      }
    },

    // Set name for the image
    imageNames in docker := Seq(
      ImageName(namespace = Some(organization.value),
        repository = name.value.toLowerCase,
        tag = Some(version.value))
    ),
    buildOptions in docker := BuildOptions(cache = false)
  )
lazy val httpservice = sbtdockerAppBase("httpservice")("./httpservice")
  .enablePlugins(JavaAgent)
  .settings(
    libraryDependencies ++= Seq(akkaActors, akkaStreams, akkaHTTP, akkaHTTP2, slf4jAPI, slf4jLog4J),
    javaAgents += "org.mortbay.jetty.alpn" % "jetty-alpn-agent" % alpnVersion % "runtime",
    mainClass in Compile := Some("com.lightbend.knative.serving.HelloWorldHTTP")
  )

lazy val grpcservice = sbtdockerAppBase("grpcservice")("./grpcservice")
  .settings(
    PB.targets in Compile := Seq(
      scalapb.gen(grpc=true) -> (sourceManaged in Compile).value
    ),
    libraryDependencies ++= Seq(
      "com.thesamet.scalapb" %% "scalapb-runtime"      % scalapb.compiler.Version.scalapbVersion % "protobuf",
      "com.thesamet.scalapb"  %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion,
      "io.grpc"               % "grpc-netty-shaded"     % scalapb.compiler.Version.grpcJavaVersion,
      typesafeConfig, ficus, slf4jAPI, slf4jLog4J),
    mainClass in Compile := Some("com.lightbend.knative.serving.HelloWorldGRPC")
  )

lazy val akkagrpcservice = sbtdockerAppBase("akkagrpcservice")("./akkagrpcservice")
  .enablePlugins(AkkaGrpcPlugin, JavaAgent)
  .settings(
    libraryDependencies ++= Seq(akkaActors, akkaStreams, akkaHTTP, akkaHTTP2, akkaDiscovery, akkaSLF4, logback),
    javaAgents += "org.mortbay.jetty.alpn" % "jetty-alpn-agent" % alpnVersion % "runtime",
    mainClass in Compile := Some("com.lightbend.knative.serving.GreeterServer")
  )

lazy val simpleevents = sbtdockerAppBase("simpleevents")("./simpleevents")
  .enablePlugins(JavaAgent)
  .settings(
    libraryDependencies ++= Seq(akkaActors, akkaStreams, akkaHTTP, akkaHTTP2, akkaJson, slf4jAPI, slf4jLog4J),
    javaAgents += "org.mortbay.jetty.alpn" % "jetty-alpn-agent" % alpnVersion % "runtime",
    mainClass in Compile := Some("com.lightbend.knative.eventing.SimpleEvents")
  )

lazy val cloudevents = sbtdockerAppBase("cloudevents")("./cloudevents")
  .enablePlugins(JavaAgent)
  .settings(
    libraryDependencies ++= Seq(akkaActors, akkaStreams, akkaHTTP, akkaHTTP2, akkaJson, slf4jAPI, slf4jLog4J),
    javaAgents += "org.mortbay.jetty.alpn" % "jetty-alpn-agent" % alpnVersion % "runtime",
    mainClass in Compile := Some("com.lightbend.knative.eventing.CloudEventsSender")
  )
