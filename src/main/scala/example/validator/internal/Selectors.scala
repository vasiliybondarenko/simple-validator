package example.validator.internal

import example.validator.internal.Transformations.Failure
import example.validator.internal.{Fields, Suggestion}

import scala.quoted.*

object Selectors {
  def fieldName[From: Type, FieldType](
      validFields: Fields,
      selector: Expr[From => FieldType]
  )(using Quotes): String = {
    selector match {
      case FieldSelector(fieldName)
          if validFields.containsFieldWithName(fieldName) =>
        fieldName
      case other =>
        Failure.emit(
          Failure.InvalidFieldSelector(
            other,
            summon,
            Suggestion.fromFields(validFields)
          )
        )
    }
  }

  private object FieldSelector {

    def unapply(arg: Expr[Any])(using Quotes): Option[String] = {
      import quotes.reflect.*
      PartialFunction.condOpt(arg.asTerm) {
        case Lambda(_, Select(Ident(_), fieldName)) => fieldName
      }
    }
  }

}
