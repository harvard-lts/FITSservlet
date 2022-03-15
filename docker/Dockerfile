# This file creates a Docker image for running FITS within Tomcat.
#
# Usage:
#   # Build the image
#   mvn -DskipTests clean package
#   mvn dependency:copy-dependencies -DincludeArtifactIds=fits -DincludeTypes=zip
#   docker build -f docker/Dockerfile -t fitsservlet .
#
#   # Run the service in the foreground
#   docker run --rm -it -p 8080:8080 fitsservlet
#
#   # Navigate to http://localhost:8080 in your browser

FROM docker.io/tomcat:9-jre17-temurin-focal

ARG FILE_VERSION=5.41

RUN apt-get update && \
    apt-get install -yqq make gcc unzip nano vim less && \
    rm -rf /var/lib/apt/lists/*

# Install file https://github.com/file/file
RUN cd /var/tmp && \
    curl -so file-${FILE_VERSION}.tar.gz https://astron.com/pub/file/file-${FILE_VERSION}.tar.gz && \
    tar xzf file-${FILE_VERSION}.tar.gz && \
    cd file-${FILE_VERSION} && \
    ./configure && \
    make -j4 && \
    make install && \
    ldconfig && \
    cd .. && \
    rm -rf file-${FILE_VERSION}*

# Install FITS
COPY target/dependency/fits-*.zip /opt/fits.zip
RUN unzip -q /opt/fits.zip -d /opt/fits && \
    rm /opt/fits.zip && \
    mkdir -p /var/log/fits

# Install FITS Servlet into WebApps folder as ROOT.
COPY target/fits-*.war $CATALINA_HOME/webapps/fits.war
COPY docker/*.properties $CATALINA_HOME/conf
RUN mkdir $CATALINA_HOME/webapps/ROOT && \
    echo '<% response.sendRedirect("/fits/"); %>' > $CATALINA_HOME/webapps/ROOT/index.jsp

# Expose our Volume and Ports
VOLUME ["/processing"]
