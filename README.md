# sis-gfl
Discord bot for game Girls' Frontline

Interactive inforamtional bot with database.

## Requirements
java 11

## Runing with docker:
- After downloading project you need to set discord secret token, google api token (for requests to google docs) and mongo database data in `application.yml` file:
 ```
mongo:
  url: mongodb://#login#:#password#@127.0.0.1:27017/?authSource=#database_name#
  name: #database_name#

discord:
  token: #discord_token#

google:
  api:
    token: #google_token#
 ```
- Run in project folder: `./gradlew build`. You may need to set gradlew file executable.
- `docker build --build-arg JAR_FILE=build/libs/*SNAPSHOT.jar -t gf/sis .`
- docker-compose.yml file example:
```
version: '3.1'

services:
  sis:
    image: gf/sis
    volumes:
      - "/etc/timezone:/etc/timezone:ro"
      - "/etc/localtime:/etc/localtime:ro"
```
- Run docker container with `docker-compose -f docker-compose.yml up -d`
