package example.validator

sealed trait FunctionArguments extends Selectable {
  def selectDynamic(value: String): Nothing
}