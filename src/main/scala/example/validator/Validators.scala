package example.validator

import example.validator.internal.BuilderConfigUtils.BuilderConfig
import example.validator.internal.{NotQuotedException, Transformations}

import scala.annotation.{compileTimeOnly, implicitNotFound}
import scala.deriving.Mirror

case class ValidatorBuilder[A]() {
  import example.validator.internal.Transformations.*
  inline def withValidators(inline config: BuilderConfig[A]*)(using
      product: Mirror.ProductOf[A]
  ): Validator[A] =
    Transformations.withValidators(config*)(product)

}

object Validators {
  def validator[A]: ValidatorBuilder[A] = ValidatorBuilder()
  @compileTimeOnly(
    "'withValidator' needs to be erased from the AST with a macro."
  )
  def withValidator[Source, FieldType, ActualType](
      selector: Source => ActualType,
      v: Validator[ActualType]
  )(using
      ev1: ActualType <:< FieldType,
      @implicitNotFound(
        "Field validator is supported for product types only, but ${Source} is not a product type."
      )
      ev2: Mirror.ProductOf[Source]
  ): BuilderConfig[Source] = throw NotQuotedException("Field validator")
}
