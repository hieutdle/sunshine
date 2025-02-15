.PHONY: native package test run clean benchmark bmnative format-linux

package:
	@./mvnw package

test:
	@./mvnw test

run:
	@./mvnw exec:java

clean:
	@./mvnw clean

native:
	@./mvnw -Pnative package

benchmark:
	@./lox examples/benchmarks/harness.lox $(filter-out $@,$(MAKECMDGOALS))  # Also uses a TAB

bmnative:
	@./target/lox examples/benchmarks/harness.lox $(filter-out $@,$(MAKECMDGOALS))  # Also uses a TAB

format-linux:
	@find . -type f \( -name "lox" -o -name "dlox" -o -name "debuglox" -o -name "mvnw" \) ! -path "./.git/*" -exec grep -Iq . {} \; -and -exec sed -i "s/\r//g" {} +