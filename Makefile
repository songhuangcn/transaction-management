COMPOSE  = docker compose -f docker-compose.yml

.DEFAULT_GOAL := help

## setup: Initialize environment and run tests
.PHONY: setup
setup: build test

## status: Check service status
.PHONY: status
status:
	$(COMPOSE) ps

## run: Run service in foreground
.PHONY: run
run:
	$(COMPOSE) up --remove-orphans app
	
## start: Start service in background
.PHONY: start
start:
	$(COMPOSE) up --remove-orphans app
	
## stop: Stop all services
.PHONY: stop
stop:
	$(COMPOSE) down --remove-orphans

## test: Run all tests in container
.PHONY: test
test:
	$(COMPOSE) run --rm runner mvn test
	
## build: Build Docker images
.PHONY: build
build:
	$(COMPOSE) build
	
## clean: Clean all data and containers
.PHONY: clean
clean:
	$(COMPOSE) down --volumes --remove-orphans

## bash: Enter container bash shell
.PHONY: bash
bash:
	$(COMPOSE) run --rm -it runner bash

## help: Show command help
.PHONY: help
help: Makefile
	@printf "\nUsage: make <TARGETS> <OPTIONS> ...\n\nTargets:\n"
	@sed -n 's/^##//p' $< | column -t -s ':' | sed -e 's/^/ /'
