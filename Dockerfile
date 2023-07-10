FROM azul/zulu-openjdk-alpine:17-jre-latest

RUN apk update
RUN apk add apk-tools
RUN apk upgrade

ARG NEW_RELIC_AGENT_PATH
ARG NEW_RELIC_YAML_PATH
ARG PROJECT_JAR_PATH

COPY ${NEW_RELIC_AGENT_PATH} newrelic-agent.jar
COPY ${NEW_RELIC_YAML_PATH} newrelic.yml
COPY ${PROJECT_JAR_PATH} app.jar

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS \
            -javaagent:newrelic-agent.jar \
            -Dnewrelic.environment=$SPRING_PROFILES_ACTIVE \
            -Dnewrelic.config.file=newrelic.yml \
            -jar app.jar"]
