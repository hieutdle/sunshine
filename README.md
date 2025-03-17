# tlox -- A Lox Truffle Bytecode Interpreter

tlox is a bytecode interpreter for the Lox programming language. It is implemented using the Truffle framework, which is a self-optimizing runtime for programming languages.
This implementation is developed as part of the Build Your Own Programming Language course at Software Architecture Group, Hasso Plattner Institute, Potsdam.

## Getting Started

## Maven

To directly use the command line, this might be helpful:

### Compile

```bash
./mvnw package
```

### Run Tests

```bash
./mvnw test
```

### Running the main class

```bash
./mvnw exec:java -Dexec.args="-c 'print true;'"
```

### Cleanup

```bash
./mvnw clean
```
