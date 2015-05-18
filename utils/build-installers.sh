#! /bin/bash

SCRIPT_NAME=`readlink -f $0`
SCRIPT_DIR=`dirname ${SCRIPT_NAME}`
SRC_DIR=`readlink -f ${SCRIPT_DIR}/..`
HTML_TEMPLATE="${SCRIPT_DIR}/index.html.template"
WEBSTART_TEMPLATE="${SCRIPT_DIR}/WebStart.jnlp.template"
BASE_URL="http://kajeka.com/internal"

if [ -z "${OUTPUT_NAME}" ];
then
  OUTPUT_NAME="Kajeka"
fi

echo OUTPUT_NAME=${OUTPUT_NAME}
echo SCRIPT_NAME=${SCRIPT_NAME}
echo SCRIPT_DIR=${SCRIPT_DIR}
echo SRC_DIR=${SRC_DIR}

function signexe
{
  if [ -z "${SIGN_PASSWORD}" ];
  then
    echo "Not signing $1, no credentials supplied..."
    return
  fi

  osslsigncode sign \
    -pkcs12 ${SIGN_P12} \
    -pass ${SIGN_PASSWORD} \
    -n "${BASE_NAME}" \
    -i http://www.kajeka.com/ \
    -t http://tsa.starfieldtech.com/ \
    -in ${1} \
    -out ${1}.signed

  mv ${1}.signed ${1}
}

cd ${SRC_DIR}
VERSION=`${SCRIPT_DIR}/version.sh`

GIT_REV=`git rev-parse HEAD`
BUILDS_DIR="${SCRIPT_DIR}/builds"
BUILD_DIR="${BUILDS_DIR}/${VERSION}"
BUILD_URL="${BASE_URL}/${BUILD_NAME}"

echo VERSION=${VERSION}
echo GIT_REV=${GIT_REV}
echo BUILD_DIR=${BUILD_DIR}
echo BUILD_URL=${BUILD_URL}

#rm -r ${BUILD_DIR}
mkdir -p ${BUILD_DIR}

# Windows
cd ${SRC_DIR}/nsis-installer
cat installer.nsi | sed \
  -e "s/_BASE_NAME_/${BASE_NAME}/g" \
  -e "s/_OUTPUT_NAME_/${OUTPUT_NAME}/g" \
  -e "s/_VERSION_/${VERSION}/g" \
  | makensis -
if [ "$?" != "0" ];
then
    exit $?
fi
signexe ${SRC_DIR}/nsis-installer/${OUTPUT_NAME}-${VERSION}-installer.exe
cp ${SRC_DIR}/nsis-installer/${OUTPUT_NAME}-${VERSION}-installer.exe ${BUILD_DIR}

# OS X
cd ${SRC_DIR}/target
genisoimage -D -V ${OUTPUT_NAME} -no-pad -uid 0 -gid 0 \
    -apple -o ${BUILD_DIR}/${OUTPUT_NAME}-${VERSION}.dmg dmg/

# Everything else
cp ${SRC_DIR}/target/${BASE_NAME}-${VERSION}.jar \
  ${BUILD_DIR}/${OUTPUT_NAME}-${VERSION}.jar

# Source code
cd ${SRC_DIR}
git archive --format zip -9 --output ${BUILD_DIR}/${OUTPUT_NAME}-${VERSION}-source.zip ${GIT_REV}

cat ${HTML_TEMPLATE} | sed \
    -e "s/_BUILD_NAME_/${BUILD_NAME}/g" \
    -e "s/_BASE_NAME_/${BASE_NAME}/g" \
    -e "s/_VERSION_/${VERSION}/g" \
    -e "s/_GIT_REV_/${GIT_REV}/g" \
    > ${BUILD_DIR}/index.html
cat ${WEBSTART_TEMPLATE} | sed -e "s%_BUILD_URL_%${BUILD_URL}%g" \
    -e "s/_BASE_NAME_/${BASE_NAME}/g" \
    -e "s/_VERSION_/${VERSION}/g" \
    -e "s/_HEAP_SIZE_/920m/g" > ${BUILD_DIR}/WebStart32.jnlp
cat ${WEBSTART_TEMPLATE} | sed -e "s%_BUILD_URL_%${BUILD_URL}%g" \
    -e "s/_BASE_NAME_/${BASE_NAME}/g" \
    -e "s/_VERSION_/${VERSION}/g" \
    -e "s/_HEAP_SIZE_/32000m/g" > ${BUILD_DIR}/WebStart64.jnlp
cp ${SRC_DIR}/src/main/resources/Resources/Images/Splash.png ${BUILD_DIR}
cp ${SRC_DIR}/src/main/resources/Resources/Images/Icon512x512.png ${BUILD_DIR}

rm ${BUILDS_DIR}/current
ln -s ${BUILD_DIR} ${BUILDS_DIR}/current
