resolvers += "Bintray Repository" at "https://dl.bintray.com/shmishleniy/"

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.15.0")
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.3.17")
addSbtPlugin("se.marcuslonnberg" % "sbt-docker" % "1.7.0")

addSbtPlugin("com.lightbend.sbt" % "sbt-javaagent" % "0.1.5")
addSbtPlugin("com.lightbend.akka.grpc" % "sbt-akka-grpc" % "1.0.1")

addSbtPlugin("com.thesamet" % "sbt-protoc" % "0.99.34")
libraryDependencies += "com.thesamet.scalapb" %% "compilerplugin" % "0.10.7"

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.9.2")
