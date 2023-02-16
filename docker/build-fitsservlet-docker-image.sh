#!/bin/bash

# Builds docker image from the FitsServlet artifact pulled from Artifactory via Maven.
# Requires a parameter of the FitsServlet WAR artifact version.
# This script should be run from the root of the project

CURPATH=`dirname $0`

if [ "${CURPATH:0:8}" != "./docker" ] ; then
	echo "This script must be run from the root of the project"
	exit 1
fi

if [ $# -lt 1 ] ; then
    echo "MISSING...1st argument is the FitsServlet WAR version deployed to Maven repository to use for building the Docker image"
    exit 1
fi

WAR_VERSION="$1"
FITS_VERSION=1.6.0
ARTIFACT_VERSION=`git rev-parse --short @`
ARTIFACT_FINAL_NAME=registry.lts.harvard.edu/lts/fits-service

echo ARTIFACT_VERSION: "$ARTIFACT_FINAL_NAME":"$ARTIFACT_VERSION"

mkdir -p target
# Get the FitsServlet artifact from the LTS Artifactory
eval "mvn dependency:get -DgroupId=edu.harvard.huit.lts -DartifactId=fits-service -Dversion=\"$WAR_VERSION\" \
-Dpackaging=war \
-DremoteRepositories=https://artifactory.lts.harvard.edu/artifactory/lts-libs-all \
-Dtransitive=false \
-Ddest=target/"

# Get the FITS artifact from the LTS Artifactory
eval "mvn dependency:get -DgroupId=edu.harvard.huit.lts -DartifactId=fits -Dversion=\"$FITS_VERSION\" \
-Dpackaging=zip \
-DremoteRepositories=https://artifactory.lts.harvard.edu/artifactory/lts-libs-all \
-Dtransitive=false \
-Ddest=target/"

ret_code=$?
if [ $ret_code != 0 ]; then
  printf "Error : [$ret_code] when executing mvn command. Exiting...\n"
  exit $ret_code
fi

docker build -f docker/Dockerfile -t "$ARTIFACT_FINAL_NAME":"$ARTIFACT_VERSION" .
docker tag "$ARTIFACT_FINAL_NAME":"$ARTIFACT_VERSION" "$ARTIFACT_FINAL_NAME":"latest"
docker tag "$ARTIFACT_FINAL_NAME":"$ARTIFACT_VERSION" "$ARTIFACT_FINAL_NAME":"$WAR_VERSION"
