APP_NAME="fits-service"
 
# Get the latest git hash
VERSION=2.3.0
echo "building fits-service version: $VERSION"
 
# Build the image
echo "Building artifactory.huit.harvard.edu/lts/$APP_NAME:$VERSION"
docker build -f docker/Dockerfile -t artifactory.huit.harvard.edu/lts/$APP_NAME:$VERSION --platform linux/amd64 .
 
# Push the images
echo "Pushing artifactory.huit.harvard.edu/lts/$APP_NAME:$VERSION"
docker push artifactory.huit.harvard.edu/lts/$APP_NAME:$VERSION 