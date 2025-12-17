.PHONY: build run stop logs clean deploy

build:
	mvn clean package -DskipTests
	docker build -t cofry-backend:latest .

run:
	docker-compose up -d

stop:
	docker-compose down

logs:
	docker logs -f cofry-backend

clean:
	mvn clean
	docker system prune -f

deploy:
	./deploy.sh

health-check:
	./scripts/health-check.sh

