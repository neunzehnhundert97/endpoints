import mill._, scalalib._, scalajslib._
import mill.scalalib.publish._
import coursier.maven.MavenRepository

trait CommonConfiguration extends ScalaModule with PublishModule {
  def scalaVersion = "3.0.0"

  def scalacOptions = Seq("-explain", "-explain-types", "-no-indent", "-deprecation", "-Xfatal-warnings")
}

object Endpoints extends CommonConfiguration {
  def publishVersion = "0.0.1"
  def artifactName = "endpoints"

  def pomSettings = PomSettings(
    description = artifactName(),
    organization = "de.neunzehnhundert97",
    url = "",
    licenses = Seq(),
    versionControl = VersionControl.github("", ""),
    developers = Seq()
  )
}

object EndpointsZHTTP extends CommonConfiguration {
  def publishVersion = "0.0.1"
  def artifactName ="endpoints-zhttp"

  def pomSettings = PomSettings(
    description = artifactName(),
    organization = "de.neunzehnhundert97",
    url = "",
    licenses = Seq(),
    versionControl = VersionControl.github("", ""),
    developers = Seq()
  )

  def ivyDeps = Agg(
    ivy"io.d11::zhttp:1.0.0.0-RC17",
    ivy"com.lihaoyi::upickle::1.4.0"
  )

  def moduleDeps = Seq(
    Endpoints
  )
}

object EndpointsJS extends CommonConfiguration with ScalaJSModule {
  def scalaJSVersion = "1.5.0"

  def publishVersion = "0.0.1"
  def artifactName ="endpoints-js"

  def pomSettings = PomSettings(
    description = artifactName(),
    organization = "de.neunzehnhundert97",
    url = "",
    licenses = Seq(),
    versionControl = VersionControl.github("", ""),
    developers = Seq()
  )

  def ivyDeps = Agg(
    ivy"com.raquo::laminar::0.13.1",
    ivy"io.laminext::core::0.13.10",
    ivy"io.laminext::fetch::0.13.10",
    ivy"com.lihaoyi::upickle::1.4.0",
    ivy"io.laminext::fetch-upickle::0.13.10"
  )

  def moduleDeps = Seq(
    Endpoints
  )
}
