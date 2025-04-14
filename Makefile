KOTLINC=kotlinc
SRC=main.kt
OUTPUT=hello
LIBS="libs/*"
CLASSPATH=$(LIBS:libs/%=%)

all: build

build:
	$(KOTLINC) -cp "$(LIBS)" $(SRC) -d $(OUTPUT).jar -include-runtime

run: build
	java -cp "$(OUTPUT).jar:$(LIBS)" HelloWorldKt

clean:
	rm -rf $(OUTPUT).jar
