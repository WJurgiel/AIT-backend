# AIT-backend
Backend for webpage that gathers all sales and attractive discounts across many gaming platforms (Steam, Epic, Gog) etc

# How to run:
1. create .env file in the root of the project (`AIT-Backend/.env`)
```.dotenv
POSTGRES_USER=root
POSTGRES_PASSWORD=rootpassword
POSTGRES_DB=application

MONGO_USER=user
MONGO_PASSWORD=password
MONGO_DB=games_data

JWT_TOKEN=VdXWkuFdVucavTJUmRcNbL0fi/5pTzV+EXkjJDmNg5w=

RAWG_TOKEN=<your RAWG token>
```
2. Download docker (can be Docker Desktop)
3. Run docker engine ()
3. In root run
``docker-compose up``
3. Build project with gradle (`AIT-Backend/`)
```shell
> pwd
.../AIT-Backend/
> ./gradlew build
```
4. If project has been built successfully and you have .env file run
``./gradlew bootRun``
5. Project should run just fine.