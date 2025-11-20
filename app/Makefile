.PHONY: build run run-dist test lint

build:
	bash -c "./gradlew build"

run-dist:
	bash -c "./gradlew installDist"
	bash -c "./build/install/app/bin/app"

run:
	bash -c "./gradlew run"

test:
	bash -c "./gradlew test"

lint:
	bash -c "./gradlew checkstyleMain checkstyleTest"