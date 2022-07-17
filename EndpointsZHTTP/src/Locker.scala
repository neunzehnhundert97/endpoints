package de.neunzehnhundert97.endpoints.zhttp

import zio.{Semaphore, ZIO, ZLayer}

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
    ZLayer.fromZIO(ParseLocker.apply)
}
