#! /bin/sh

export BASE_NAME="Graphitto"
VERSION=`utils/version.sh`

mvn -DversionNumber=${VERSION} -DproductName=${BASE_NAME} clean package

utils/build-installers.sh
