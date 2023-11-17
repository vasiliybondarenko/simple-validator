package example.validator.internal

import scala.quoted.*

final class Field(val name: String, val tpe: Type[?], val default: Option[Expr[Any]]) {


  override def toString: String = s"Field($name)"

  def <:<(that: Field)(using Quotes): Boolean = {
    import quotes.reflect.*
    TypeRepr.of(using tpe) <:< TypeRepr.of(using that.tpe)
  }

}
