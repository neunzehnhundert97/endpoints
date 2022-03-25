package de.neunzehnhundert97.endpoints.zhttp

/** Exeption during parsing of the reponse's json content. */
final case class ParsingException(msg: String) extends Throwable
