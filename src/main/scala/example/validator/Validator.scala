package example.validator

import cats.data.ValidatedNec
import example.validator.Validator.ErrorsOr

trait Validator[A] {
  def validate(x: A): ErrorsOr[A]
}

object Validator:
  type ErrorsOr[A] = ValidatedNec[String, A]
