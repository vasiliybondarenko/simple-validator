package example.validator

import cats.data.Validated.*
import cats.data.{NonEmptyChain, Validated}
import cats.implicits.given
import cats.syntax.all
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class MacrosValidatorTest extends AnyFreeSpec with Matchers {

  import Validators.*

  def nonEmptyValidatorValidator: Validator[String] =
    (x: String) =>
      Validated.cond(
        !x.isBlank,
        x,
        NonEmptyChain.one(s"{} cannot be empty")
      )

  val diameterValidator: Validator[Int] = (x: Int) =>
    Validated.cond(x > 1000, x, NonEmptyChain.one("{} is too small"))

  case class Planet(planetName: String, diameter: Int, sType: String)

  case class Star(planetName: String, diameter: Int, sType: String)

  def validator[A]: ValidatorBuilder[A] = ValidatorBuilder()

  "MacrosValidator" - {
    "should create a validator for case class" in {
      val v: Validator[Planet] =
        validator[Planet].withValidators(
          withValidator(_.planetName, nonEmptyValidatorValidator),
          withValidator(_.diameter, diameterValidator),
          withValidator(_.sType, nonEmptyValidatorValidator)
        )

      v.validate(Planet("", 100, "")) shouldBe Invalid(
        NonEmptyChain(
          "planetName cannot be empty",
          "diameter is too small",
          "sType cannot be empty"
        )
      )
    }

    "should create a validator for case class even for wrong fields order" in {
      val v: Validator[Planet] =
        validator[Planet].withValidators(
          withValidator(_.planetName, nonEmptyValidatorValidator),
          withValidator(_.sType, nonEmptyValidatorValidator),
          withValidator(_.diameter, diameterValidator)
        )

      v.validate(Planet("", 100, "")) shouldBe Invalid(
        NonEmptyChain(
          "planetName cannot be empty",
          "diameter is too small",
          "sType cannot be empty"
        )
      )
    }

    "should generate `always true` validator for missed validators" in {
      val v: Validator[Planet] =
        validator[Planet].withValidators(
          withValidator(_.planetName, nonEmptyValidatorValidator)
        )

      v.validate(Planet("", 100, "")) shouldBe Invalid(
        NonEmptyChain(
          "planetName cannot be empty"
        )
      )
    }

    "should work with anonymous functions" in {
      val v: Validator[Planet] =
        validator[Planet].withValidators(
          withValidator(_.planetName, x => Validated.cond(!x.isBlank, x, NonEmptyChain.one("{} cannot be empty")) ),
          withValidator(_.diameter, x => Validated.cond(x > 1000, x, NonEmptyChain.one("{} should be > 1000")) )
        )

      v.validate(Planet("", 100, "")) shouldBe Invalid(
        NonEmptyChain(
          "planetName cannot be empty",
          "diameter should be > 1000"
        )
      )
    }

  }
}
