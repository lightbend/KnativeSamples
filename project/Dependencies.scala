import Versions._
import sbt._

object Dependencies {

  val akkaActors      = "com.typesafe.akka"               %% "akka-actor"               % akkaVersion
  val akkaStreams     = "com.typesafe.akka"               %% "akka-stream"              % akkaVersion
  val akkaHTTP2       = "com.typesafe.akka"               %% "akka-http2-support"       % akkaHTTPVersion
  val akkaHTTP        = "com.typesafe.akka"               %% "akka-http"                % akkaHTTPVersion

  val typesafeConfig  = "com.typesafe"                    %  "config"                   % TypesafeConfigVersion
  val ficus           = "com.iheart"                      %% "ficus"                    % FicusVersion

  val slf4jAPI        = "org.slf4j"                       % "slf4j-api"                 % slf4jVersion
  val slf4jLog4J      = "org.slf4j"                       % "slf4j-log4j12"             % slf4jVersion
}