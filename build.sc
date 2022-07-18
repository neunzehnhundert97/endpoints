import mill._, scalalib._, scalajslib._
import mill.scalalib.publish._
import coursier.maven.MavenRepository

import $ivy.`com.goyeau::mill-scalafix::0.2.8`
import com.goyeau.mill.scalafix.ScalafixModule

trait CommonConfiguration extends ScalaModule with PublishModule with ScalafixModule {
  def scalaVersion = "3.1.0"

  def scalacOptions = Seq(
    "-deprecation",
    "-Xfatal-warnings",
    "-Xsemanticdb"
  )

  def scalafixIvyDeps = Agg(
    ivy"com.github.liancheng::organize-imports:0.6.0",
    ivy"org.scalalint::rules:0.1.4"
  )

  object test extends Tests {
    def ivyDeps = Agg(
      ivy"dev.zio::zio-test:2.0.0",
      ivy"dev.zio::zio-test-sbt:2.0.0"
    )
    def testFramework = "zio.test.sbt.ZTestFramework"
  }
}

object Endpoints extends Module {

  trait Inner extends CommonConfiguration {
    def millSourcePath = os.pwd / "Endpoints"

    def publishVersion = "0.0.3"

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

  object JS extends Inner with ScalaJSModule {
    def scalaJSVersion = "1.7.1"
  }

  object JVM extends Inner
}

object EndpointsZHTTP extends CommonConfiguration with ScalafixModule {
  def publishVersion = "0.0.3"
  def artifactName   = "endpoints-zhttp"

  def pomSettings = PomSettings(
    description = artifactName(),
    organization = "de.neunzehnhundert97",
    url = "",
    licenses = Seq(),
    versionControl = VersionControl.github("", ""),
    developers = Seq()
  )

  def ivyDeps = Agg(
    ivy"io.d11::zhttp:2.0.0-RC7",
    ivy"com.lihaoyi::upickle::1.4.0"
  )

  def moduleDeps = Seq(
    Endpoints.JVM
  )
}

object EndpointsJS extends CommonConfiguration with ScalaJSModule {
  def scalaJSVersion = "1.7.1"

  def publishVersion = "0.0.3"
  def artifactName   = "endpoints-js"

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
    Endpoints.JS
  )
}
