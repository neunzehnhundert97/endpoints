package de.neunzehnhundert97.endpoints.zhttp

import zio.{Semaphore, ZLayer, ZIO, Has}

trait ParseLocker {
  def parsing: Semaphore
}

object ParseLocker {

  /** Mutex for payload parsing. */
  def parsing =
    ZIO.service[ParseLocker].map(_.parsing)

  def apply =
    for {
      s <- Semaphore.make(permits = 1)
    } yield new ParseLocker {
      def parsing = s
    }

  def live =
    ZLayer.fromEffect(ParseLocker.apply)
}
