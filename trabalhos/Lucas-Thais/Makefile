INPUT = entrada.in

.PHONY: build run

build:
	mkdir -p bin
	javac --release 25 -d bin src/*.java

run:
	@java -cp bin Main $(INPUT)
