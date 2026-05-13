FROM tomcat:9-jdk8
COPY dist/project-root.war /usr/local/tomcat/webapps/ROOT.war
EXPOSE 8080