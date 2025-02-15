# Lecture 2: Expression

## What beginLoxAdd() and endLoxAdd() Likely Do

`beginLoxAdd():`

- Signals the start of an addition/subtraction operation.
- It might store an operator (+ or -) found at `ctx.getChild(i)`.
- It could push the left-hand operand onto a stack or intermediate representation.
  `endLoxAdd():`
- Completes the addition/subtraction operation.
- It likely pops the last two operands from a stack and combines them based on the stored operator.
- It might generate bytecode or an intermediate representation for execution.
