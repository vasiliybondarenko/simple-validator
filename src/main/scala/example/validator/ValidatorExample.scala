package example.validator

import cats.data.{NonEmptyChain, Validated}

import scala.annotation.{compileTimeOnly, implicitNotFound}
import scala.deriving.Mirror

object ValidatorExample extends App {
  import Validators.*

  def nonEmptyValidatorValidator(fieldName: String): Validator[String] = (x: String) =>
    Validated.cond(!x.isBlank, x, NonEmptyChain.one(s"$fieldName cannot be empty"))

  val diameterValidator: Validator[Int] = (x: Int) =>
    Validated.cond(x > 1000, x, NonEmptyChain.one("Diameter is too small"))

  case class Planet(planetName: String, diameter: Int, sType: String)
  
  val v: Validator[Planet] =
    validator[Planet].withValidators(
      withValidator(_.planetName, nonEmptyValidatorValidator("Name")),
      withValidator(_.diameter, diameterValidator),
      withValidator(_.sType, nonEmptyValidatorValidator("Type"))
    )


  println(v.validate(Planet("", 100, "")))
  println(v.validate(Planet("Mars", 10000, "Habitable")))
}


