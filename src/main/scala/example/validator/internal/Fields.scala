package example.validator.internal

import scala.deriving.Mirror
import scala.quoted.*


sealed trait Fields {
  export byName.{ apply => unsafeGet, contains => containsFieldWithName, get }

  val value: List[Field]

  val byName: Map[String, Field] = value.map(f => f.name -> f).toMap
}

object Fields {
  def source(using sourceFields: Fields.Source): Fields.Source = sourceFields
  

  final case class Source(value: List[Field]) extends Fields
  object Source extends FieldsCompanion[Source]

  
  sealed abstract class FieldsCompanion[FieldsSubtype <: Fields] {

    def apply(fields: List[Field]): FieldsSubtype

    final def fromMirror[A: Type](mirror: Expr[Mirror.ProductOf[A]])(using Quotes): FieldsSubtype = {
      val materializedMirror = MaterializedMirror.createOrAbort(mirror)

      val fields = materializedMirror.mirroredElemLabels
        .zip(materializedMirror.mirroredElemTypes)
        .map((name, tpe) => Field(name, tpe.asType))
      apply(fields)
    }    
  }
}