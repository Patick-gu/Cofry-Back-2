.PHONY: build run stop logs clean deploy commit

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

commit:
	@if [ -f scripts/auto-commit.sh ]; then \
		bash scripts/auto-commit.sh "$(MESSAGE)"; \
	elif [ -f scripts/auto-commit.ps1 ]; then \
		powershell -ExecutionPolicy Bypass -File scripts/auto-commit.ps1 "$(MESSAGE)"; \
	else \
		echo "❌ Nenhum script de commit encontrado!"; \
	fi

