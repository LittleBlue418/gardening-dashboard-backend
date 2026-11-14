package models
package common

import play.api.libs.json._

import java.security.SecureRandom

// The id of any stored record: 24 lowercase hex characters (the format the existing data uses).
// Our own type, so the models don't depend on a database library.
final case class Id(value: String) extends AnyVal {
  override def toString: String = value
}

object Id {
  private val Pattern = "^[0-9a-f]{24}$".r
  private val random = new SecureRandom()

  def generate(): Id = Id(Array.fill(12)(random.nextInt(256)).map(byte => f"$byte%02x").mkString)

  def parse(text: String): Option[Id] = Option.when(Pattern.matches(text))(Id(text))

  implicit val format: Format[Id] = Format(
    Reads.StringReads.flatMap(text => parse(text).fold[Reads[Id]](Reads.failed("Invalid id: " + text))(Reads.pure(_))),
    Writes(id => JsString(id.value)),
  )
}
