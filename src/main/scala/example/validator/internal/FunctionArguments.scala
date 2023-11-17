package example.validator.internal

sealed trait FunctionArguments extends Selectable {
  def selectDynamic(value: String): Nothing
}