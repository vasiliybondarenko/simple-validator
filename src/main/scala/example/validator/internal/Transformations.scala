package example.validator.internal

import cats.data.Validated.*
import cats.data.{NonEmptyChain, Validated}
import cats.syntax.SemigroupalOps
import cats.syntax.all.*
import cats.{Invariant, Semigroup, SemigroupK}
import example.validator.*
import example.validator.Validator.ErrorsOr

import scala.collection.immutable.Seq
import scala.deriving.*
import scala.quoted.*

object Transformations {

  import BuilderConfigUtils.BuilderConfig

  type Pos = (quotes: Quotes) ?=> quotes.reflect.Position

  object Pos {
    def fromExpr(expr: Expr[Any])(using Quotes): quotes.reflect.Position =
      quotes.reflect.asTerm(expr).pos
  }

  inline def withValidators[A](inline config: BuilderConfig[A]*)(
      inline product: Mirror.ProductOf[A]
  ): Validator[A] =
    ${ transformConfiguredMacro[A]('config, 'product) }

  private def transformConfiguredMacro[Source: Type](
      config: Expr[Seq[BuilderConfig[Source]]],
      product: Expr[Mirror.ProductOf[Source]]
  )(using Quotes): Expr[Validator[Source]] = {
    given source: Fields.Source = Fields.Source.fromMirror(product)



    val defaultValidator: Expr[Validator[Any]] = '{ (x: Any) =>
      x.validNec[String]
    }

    val validatorsExprs =
      parseConfig(config)(_.flatMap(parseSingleProductConfig))
    
    val validatorsMap =
      validatorsExprs.map(p => p.fieldName -> p.validator).toMap

    val orderedValidators = source.value.map(f =>
      validatorsMap.applyOrElse(f.name, _ => defaultValidator)
    )

    val tupleExpr = Expr.ofTupleFromSeq(orderedValidators.toSeq)

    '{
      new Validator[Source] {
        override def validate(x: Source): ErrorsOr[Source] = {
          def enrichErrorMessage(fieldName: String)(e: ErrorsOr[Any]): ErrorsOr[Any] =
            e.leftMap(errors => errors.map(s => s.replace("{}", fieldName)))

          val elems = x.asInstanceOf[Product].productIterator
          val fieldNames = x.asInstanceOf[Product].productElementNames
          val validators = ${ tupleExpr }.asInstanceOf[Product].productIterator
          val allErrors = validators.zip(elems).zip(fieldNames).map { case ((validator, elem), fieldName) =>
            enrichErrorMessage(fieldName)(validator.asInstanceOf[Validator[Any]].validate(elem))
          }
          //todo: think about generalizing error type NonEmptyChain,
          // not depending on Validated
          val combinedErrors =
            SemigroupK[NonEmptyChain].combineAllOptionK(allErrors.collect {
              case Invalid(e) => e
            })
          combinedErrors match
            case Some(errors) => Invalid(errors)
            case _            => x.validNec

        }
      }
    }

  }

  private def parseConfig[
      Config,
      MaterializedConfig <: MaterializedConfiguration
  ](
      config: Expr[Seq[Config]]
  )(
      extractor: Seq[Expr[Config]] => Seq[MaterializedConfig]
  )(using Quotes): List[MaterializedConfig] = {
    extractor(
      Varargs
        .unapply[Config](config)
        .getOrElse(Failure.emit(Failure.UnsupportedConfig(config)))
    ).toList
  }

  private def parseSingleProductConfig[Source](
      config: Expr[BuilderConfig[Source]]
  )(using
      Quotes,
      Fields.Source
  ): List[MaterializedConfiguration.Product.Computed] = {
    import MaterializedConfiguration.Product.*

    config match
      case '{
            Validators.withValidator[source, fieldType, actualType](
              $selector,
              $validator
            )(using $ev1, $ev2)
          } =>
        val name = Selectors.fieldName(Fields.source, selector)
        Computed(name, validator.asInstanceOf[Expr[Validator[Any]]])(
          Pos.fromExpr(config)
        ) :: Nil

      case other => Failure.emit(Failure.UnsupportedConfig(other))
  }

  sealed trait MaterializedConfiguration {
    val pos: Pos
  }

  object MaterializedConfiguration {
    enum Product extends MaterializedConfiguration {
      case Computed(fieldName: String, validator: Expr[Validator[Any]])(
          val pos: Pos
      )
    }
  }

  sealed trait Failure {
    def position(using Quotes): quotes.reflect.Position =
      quotes.reflect.Position.ofMacroExpansion

    def render(using Quotes): String
  }

  object Failure {
    def emit(failure: Failure)(using Quotes): Nothing =
      quotes.reflect.report.errorAndAbort(failure.render, failure.position)

    final case class UnsupportedConfig(config: Expr[Any]) extends Failure {
      override def render(using Quotes): String = "Fucking error"
    }

    final case class MirrorMaterialization(
        mirroredType: Type[?],
        notFoundTypeMemberName: String
    ) extends Failure {

      override final def render(using Quotes): String = {

        s"""
           |Mirror materialization for ${mirroredType.show} failed.
           |Member type not found: '$notFoundTypeMemberName'.
          """.stripMargin
      }
    }

    final case class InvalidFieldSelector(
        selector: Expr[Any],
        sourceTpe: Type[?],
        suggestedFields: List[Suggestion]
    ) extends Failure {
      override final def position(using Quotes): quotes.reflect.Position =
        selector.pos

      override final def render(using Quotes): String =
        s"""
           |'${selector.show}' is not a valid field selector for ${sourceTpe.show}.
           |Try one of these: ${Suggestion.renderAll(suggestedFields)}
          """.stripMargin
    }

    extension (tpe: Type[?]) {
      private def show(using Quotes): String =
        quotes.reflect.TypeRepr.of(using tpe).show
    }

    extension (expr: Expr[Any]) {
      private def show(using Quotes): String = quotes.reflect.asTerm(expr).show

      private def pos(using Quotes): quotes.reflect.Position =
        quotes.reflect.asTerm(expr).pos
    }

  }

}

object BuilderConfigUtils {
  opaque type BuilderConfig[Source] = Unit
}
