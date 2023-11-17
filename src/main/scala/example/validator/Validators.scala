package example.validator

import example.validator.BuilderConfigUtils.BuilderConfig

import scala.annotation.{compileTimeOnly, implicitNotFound}
import scala.deriving.Mirror

case class ValidatorBuilder[A]() {
  import Transformations.*
  inline def withValidators(inline config: BuilderConfig[A]*)(using
      product: Mirror.ProductOf[A]
  ): Validator[A] =
    Transformations.withValidators(config*)(product)

}

object Validators {
  def validator[A]: ValidatorBuilder[A] = ValidatorBuilder()
  @compileTimeOnly(
    "'Field.computed' needs to be erased from the AST with a macro."
  )
  def withValidator[Source, FieldType, ActualType](
      selector: Source => ActualType,
      v: Validator[ActualType]
  )(using
      ev1: ActualType <:< FieldType,
      @implicitNotFound(
        "Field.computed is supported for product types only, but ${Source} is not a product type."
      )
      ev2: Mirror.ProductOf[Source]
  ): BuilderConfig[Source] = throw NotQuotedException("Field.computed")
}
