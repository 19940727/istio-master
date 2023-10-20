FROM openjdk:17-alpine
ENV APP_HOME /usr/apps
EXPOSE 8080
COPY target/*.jar $APP_HOME/app.jar
WORKDIR $APP_HOME
ENTRYPOINT ["sh", "-c"]
CMD ["exec java -jar app.jar"]