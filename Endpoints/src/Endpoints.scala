package de.neunzehnhundert97
package endpoints

trait Endpoint[+A, -B] {
  def path: String
  def method: Method
}

enum Method {
  case GET, POST, DELETE, PUT
}
