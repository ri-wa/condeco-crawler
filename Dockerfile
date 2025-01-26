FROM gradle:8.11.1-jdk21 as TEMP_BUILD_IMAGE

ENV APP_HOME=/usr/app/
WORKDIR $APP_HOME
COPY build.gradle.kts settings.gradle.kts $APP_HOME

COPY gradle $APP_HOME/gradle
COPY --chown=gradle:gradle . /home/gradle/src
USER root
RUN chown -R gradle /home/gradle/src

RUN gradle build || return 0
COPY . .
RUN gradle clean build

#actual container
FROM selenium/standalone-chrome:4.27.0
ENV ARTIFACT_NAME=condeco-crawler-1.0-SNAPSHOT-all.jar
ENV APP_HOME=/usr/app/
WORKDIR $APP_HOME
COPY --from=TEMP_BUILD_IMAGE $APP_HOME/build/libs/$ARTIFACT_NAME .
EXPOSE 4444
EXPOSE 7900
ENTRYPOINT exec java -jar ${ARTIFACT_NAME}