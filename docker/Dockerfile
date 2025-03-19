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
FROM tomcat:9.0.102-jre8-temurin-jammy

ARG FILE_VERSION=5.43
ARG FILE_SHA256=8c8015e91ae0e8d0321d94c78239892ef9dbc70c4ade0008c0e95894abfb1991

RUN apt-get update && \
    apt-get install -yqq \
    unzip nano vim less \
    # jpylyzer dependencies
    python3 \
    python-is-python3 \
    # exiftool dependencies https://github.com/exiftool/exiftool
    libarchive-zip-perl \
    libio-compress-perl \
    libcompress-raw-zlib-perl \
    libcompress-bzip2-perl \
    libcompress-raw-bzip2-perl \
    libio-digest-perl \
    libdigest-md5-file-perl \
    libdigest-perl-md5-perl \
    libdigest-sha-perl \
    libposix-strptime-perl \
    libunicode-linebreak-perl\
    # file dependencies
    make \
    gcc \
    # mediainfo dependencies
    libmms0 \
    libcurl3-gnutls \
    && rm -rf /var/lib/apt/lists/*

# Install file https://github.com/file/file
RUN cd /var/tmp && \
    curl -so file-${FILE_VERSION}.tar.gz https://astron.com/pub/file/file-${FILE_VERSION}.tar.gz && \
    echo "${FILE_SHA256}  file-${FILE_VERSION}.tar.gz" | sha256sum --check && \
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
COPY docker/*.properties $CATALINA_HOME/conf/
RUN mkdir $CATALINA_HOME/webapps/ROOT && \
    echo '<% response.sendRedirect("/fits/"); %>' > $CATALINA_HOME/webapps/ROOT/index.jsp

# Expose our Volume and Ports
VOLUME ["/processing"]
