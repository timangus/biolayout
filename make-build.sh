#! /bin/sh

export BASE_NAME="Miru"
VERSION=`utils/version.sh`

mvn -DversionNumber=${VERSION} -DproductName=${BASE_NAME} -Prelease clean package

export OUTPUT_NAME=${BASE_NAME}
utils/build-installers.sh

mvn -DversionNumber=${VERSION} -DproductName=${BASE_NAME} -Prelease,evaluation clean package

export OUTPUT_NAME=${BASE_NAME}-Evaluation
utils/build-installers.sh
