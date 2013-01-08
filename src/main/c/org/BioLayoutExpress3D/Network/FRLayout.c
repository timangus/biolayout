/*
 
 BioLayoutExpress3D - A tool for visualisation
 and analysis of biological networks

 Copyright (c) 2006-2012 Genome Research Ltd.
 Authors: Thanos Theo, Anton Enright, Leon Goldovsky, Ildefonso Cases, Markus Brosch, Stijn van Dongen, Benjamin Boyer and Tom Freeman
 Contact: support@biolayout.org
 
 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

 @ author, JNI C code author Thanos Theo, 2008-2009-2010

*/

// #include <iostream> // for C++ based native functions
// this implementation is C based
#include <stdio.h> /* for printf("%d \n", int i); based commands */
#include <stdlib.h> /* so as to supress the warnings of 'implicit declaration of function ‘malloc’ & ‘free’' */
#include <stdbool.h>
#include <math.h>
#include "org/BioLayoutExpress3D/Network/FRLayout.h"



// Note: this dll is used statically, i.e. the same dll for all the FRLayout instances.
// Other tricks, like using multiple pointer references and C structs should be used for simulating 
// on the C side multiple Java objects instatiations from the Java side.


#define NAME_OF_UPDATE_GUI_JAVA_FUNCTION "updateGUI"
#define RETURN_TYPE_OF_UPDATE_GUI_JAVA_FUNCTION "()V"
#define MAX_SHORT_DECIMAL_PART_LENGTH 16
#define UNSIGNED_SHORT_MAX_VALUE 65536
#define UNSIGNED_SHORT_MAX_VALUE_FOR_MODULO 65535

static jint BOOLEAN_PACKED_DATA_POWER_OF_TWO_VALUE = 0;
static jint BOOLEAN_PACKED_DATA_BIT_SIZE = 0;
static jbyte FIXED_POINT_DECIMAL_PART_LENGTH = 0;
static jfloat TEMPERATURE_SCALING = 0.0f;
static jint canvasXSize = 0;
static jint canvasYSize = 0;
static jint canvasZSize = 0;
static jint displacementMatrixDimensionality;
static jfloat temperature = 0.0f;
static jfloat kValue = 0.0f;
static jfloat kSquareValue = 0.0f;
static jfloat kDoubled = 0.0f;
static jboolean useEdgeWeights = false;

static inline float convertFromFixedPointShortNumberToUnsignedFloat(jshort number, int decimalPartLength)
{
    if (decimalPartLength <= 0 || decimalPartLength > MAX_SHORT_DECIMAL_PART_LENGTH)
        decimalPartLength = MAX_SHORT_DECIMAL_PART_LENGTH;

    // make sure to convert to unsigned short first
    return (float)( (UNSIGNED_SHORT_MAX_VALUE + number) & UNSIGNED_SHORT_MAX_VALUE_FOR_MODULO ) / (float)(1 << decimalPartLength);
}

static inline void calcBiDirForce2D
(jfloat *cachedVertexPointCoordsMatrix, jint *cachedVertexConnectionMatrix, jint *cachedVertexConnectionRowSkipSizeValuesMatrix, jshort *cachedVertexNormalizedWeightMatrix, jfloat *displacementMatrix, jint *displacementValues,
 jint vertexID1, jint vertexID2, int *cachedVertexNormalizedWeightIndex) // for passing by reference simulation in C
{
    int vertexID1Index0 = vertexID1 << 1;
    int vertexID2Index0 = vertexID2 << 1;
    int vertexID1Index1 = vertexID1Index0 + 1;
    int vertexID2Index1 = vertexID2Index0 + 1;
    int dimensionalityIndex = cachedVertexConnectionRowSkipSizeValuesMatrix[vertexID1 - 1] + vertexID2;

    float distX = cachedVertexPointCoordsMatrix[vertexID1Index0] - cachedVertexPointCoordsMatrix[vertexID2Index0];
    float distY = cachedVertexPointCoordsMatrix[vertexID1Index1] - cachedVertexPointCoordsMatrix[vertexID2Index1];

    if (distX == 0.0f)
        distX = 1.0f;
    if (distY == 0.0f)
        distY = 1.0f;

    int absDistX = (int)( (distX > 0.0f) ? distX : -distX );
    int absDistY = (int)( (distY > 0.0f) ? distY : -distY );

    float signX = (distX > 0.0f) ? 1.0f : -1.0f;
    float signY = (distY > 0.0f) ? 1.0f : -1.0f;

    int distanceCache = (6000 * absDistX + 6 * absDistY);
    if (distanceCache >= displacementMatrixDimensionality)
        distanceCache = displacementMatrixDimensionality;
    int dispCalcX = 0, dispCalcY = 0;

    if ( ( cachedVertexConnectionMatrix[dimensionalityIndex >> BOOLEAN_PACKED_DATA_POWER_OF_TWO_VALUE] >> (dimensionalityIndex & BOOLEAN_PACKED_DATA_BIT_SIZE) ) & 1 )
    {
        if (useEdgeWeights)
        {
            float weight = convertFromFixedPointShortNumberToUnsignedFloat(cachedVertexNormalizedWeightMatrix[(*cachedVertexNormalizedWeightIndex)++], FIXED_POINT_DECIMAL_PART_LENGTH); // for passing by reference simulation in C
            dispCalcX = (int)( ( ( (displacementMatrix[distanceCache    ] - displacementMatrix[distanceCache + 4]) * weight ) + displacementMatrix[distanceCache + 4] ) * signX );
            dispCalcY = (int)( ( ( (displacementMatrix[distanceCache + 1] - displacementMatrix[distanceCache + 5]) * weight ) + displacementMatrix[distanceCache + 5] ) * signY );
        }
        else
        {
            dispCalcX = (int)(displacementMatrix[distanceCache    ] * signX);
            dispCalcY = (int)(displacementMatrix[distanceCache + 1] * signY);
        }

        displacementValues[vertexID1Index0] += dispCalcX;
        displacementValues[vertexID1Index1] += dispCalcY;

        displacementValues[vertexID2Index0] -= dispCalcX;
        displacementValues[vertexID2Index1] -= dispCalcY;
    }
    else
    {
        if ( !( (absDistX > kDoubled) && (absDistY > kDoubled) ) )
        {
            dispCalcX = (int)(displacementMatrix[distanceCache + 2] * signX);
            dispCalcY = (int)(displacementMatrix[distanceCache + 3] * signY);

            displacementValues[vertexID1Index0] += dispCalcX;
            displacementValues[vertexID1Index1] += dispCalcY;

            displacementValues[vertexID2Index0] -= dispCalcX;
            displacementValues[vertexID2Index1] -= dispCalcY;
        }
    }
}

static inline void set2DForceToVertex(jint *displacementValues, jfloat *cachedVertexPointCoordsMatrix, jint vertexID)
{
    int vertexIDIndex = vertexID << 1;
    float currentDisplacementValue = (float)displacementValues[vertexIDIndex];

    // for the X axis
    float value = (currentDisplacementValue < 0.0f)
                ? (-temperature >= currentDisplacementValue) ? -temperature : currentDisplacementValue  // max(a, b)
                : ( temperature <= currentDisplacementValue) ?  temperature : currentDisplacementValue; // min(a, b)
    cachedVertexPointCoordsMatrix[vertexIDIndex] += value;

    if (cachedVertexPointCoordsMatrix[vertexIDIndex] > canvasXSize)
        cachedVertexPointCoordsMatrix[vertexIDIndex] = (float)canvasXSize;
    // commented out so as to avoid the relayout being bounded by the layout minimum threshold
    // else if (cachedVertexPointCoordsMatrix[vertexIDIndex] < 0)
    //    cachedVertexPointCoordsMatrix[vertexIDIndex] = 0;

    displacementValues[vertexIDIndex] = 0;



    // for the Y axis
    vertexIDIndex++;
    currentDisplacementValue = (float)displacementValues[vertexIDIndex];
    value = (currentDisplacementValue < 0.0f)
            ? (-temperature >= currentDisplacementValue) ? -temperature : currentDisplacementValue  // max(a, b)
            : ( temperature <= currentDisplacementValue) ?  temperature : currentDisplacementValue; // min(a, b)
    cachedVertexPointCoordsMatrix[vertexIDIndex] += value;

    if (cachedVertexPointCoordsMatrix[vertexIDIndex] > canvasYSize)
        cachedVertexPointCoordsMatrix[vertexIDIndex] = (float)canvasYSize;
    // commented out so as to avoid the relayout being bounded by the layout minimum threshold
    // else if (cachedVertexPointCoordsMatrix[vertexIDIndex] < 0)
    //    cachedVertexPointCoordsMatrix[vertexIDIndex] = 0;

    displacementValues[vertexIDIndex] = 0;       
}

static inline void calcBiDirForce3D
(jfloat *cachedVertexPointCoordsMatrix, jint *cachedVertexConnectionMatrix, jint *cachedVertexConnectionRowSkipSizeValuesMatrix, jint *cachedPseudoVertexMatrix, jshort *cachedVertexNormalizedWeightMatrix, jint *displacementValues,
 jint vertexID1, jint vertexID2, int *cachedVertexNormalizedWeightIndex) // for passing by reference simulation in C
{
    int vertexID1Index0 = 3 * vertexID1;
    int vertexID2Index0 = 3 * vertexID2;
    int vertexID1Index1 = vertexID1Index0 + 1;
    int vertexID2Index1 = vertexID2Index0 + 1;
    int vertexID1Index2 = vertexID1Index0 + 2;
    int vertexID2Index2 = vertexID2Index0 + 2;
    int dimensionalityIndex = cachedVertexConnectionRowSkipSizeValuesMatrix[vertexID1 - 1] + vertexID2;

    float distX = cachedVertexPointCoordsMatrix[vertexID1Index0] - cachedVertexPointCoordsMatrix[vertexID2Index0];
    float distY = cachedVertexPointCoordsMatrix[vertexID1Index1] - cachedVertexPointCoordsMatrix[vertexID2Index1];
    float distZ = cachedVertexPointCoordsMatrix[vertexID1Index2] - cachedVertexPointCoordsMatrix[vertexID2Index2];

    if (distX == 0.0f)
        distX = 1.0f;
    if (distY == 0.0f)
        distY = 1.0f;
    if (distZ == 0.0f)
        distZ = 1.0f;

    bool connected = ( cachedVertexConnectionMatrix[dimensionalityIndex >> BOOLEAN_PACKED_DATA_POWER_OF_TWO_VALUE] >> (dimensionalityIndex & BOOLEAN_PACKED_DATA_BIT_SIZE) ) & 1;
    if ( !connected && ( ( (distX < 0) ? -distX : distX ) > kDoubled ) && ( ( (distY < 0) ? -distY : distY ) > kDoubled ) ) // abs(distX) & abs(distY)
        return;

    float squaredDistance = (distX * distX + distY * distY + distZ * distZ);
    float distance = (float)sqrt(squaredDistance);

    if (distance <= kDoubled)
    {        
        if ( !( ( ( cachedPseudoVertexMatrix[vertexID1 >> BOOLEAN_PACKED_DATA_POWER_OF_TWO_VALUE] >> (vertexID1 & BOOLEAN_PACKED_DATA_BIT_SIZE) ) & 1 ) && ( ( cachedPseudoVertexMatrix[vertexID2 >> BOOLEAN_PACKED_DATA_POWER_OF_TWO_VALUE] >> (vertexID2 & BOOLEAN_PACKED_DATA_BIT_SIZE) ) & 1 ) ) )
        {
            float kDist = kSquareValue / distance;
            int dispCalcX = (int)( (distX / distance) * kDist );
            int dispCalcY = (int)( (distY / distance) * kDist );
            int dispCalcZ = (int)( (distZ / distance) * kDist );

            displacementValues[vertexID1Index0] += dispCalcX;
            displacementValues[vertexID1Index1] += dispCalcY;
            displacementValues[vertexID1Index2] += dispCalcZ;

            displacementValues[vertexID2Index0] -= dispCalcX;
            displacementValues[vertexID2Index1] -= dispCalcY;
            displacementValues[vertexID2Index2] -= dispCalcZ;
        }
    }

    if (connected)
    {
        float kDist = squaredDistance / kValue;
        int dispCalcX = 0, dispCalcY = 0, dispCalcZ = 0;
        
        if (useEdgeWeights)
        {
            float kDistWeight = kDist * convertFromFixedPointShortNumberToUnsignedFloat(cachedVertexNormalizedWeightMatrix[(*cachedVertexNormalizedWeightIndex)++], FIXED_POINT_DECIMAL_PART_LENGTH); // for passing by reference simulation in C
            dispCalcX = (int)( (distX / distance) * kDistWeight );
            dispCalcY = (int)( (distY / distance) * kDistWeight );
            dispCalcZ = (int)( (distZ / distance) * kDistWeight );
        }
        else
        {
            dispCalcX = (int)( (distX / distance) * kDist );
            dispCalcY = (int)( (distY / distance) * kDist );
            dispCalcZ = (int)( (distZ / distance) * kDist );
        }

        displacementValues[vertexID1Index0] -= dispCalcX;
        displacementValues[vertexID1Index1] -= dispCalcY;
        displacementValues[vertexID1Index2] -= dispCalcZ;

        displacementValues[vertexID2Index0] += dispCalcX;
        displacementValues[vertexID2Index1] += dispCalcY;
        displacementValues[vertexID2Index2] += dispCalcZ;
    }
}

static inline void set3DForceToVertex(jint *displacementValues, jfloat *cachedVertexPointCoordsMatrix, jint vertexID)
{
    int vertexIDIndex = 3 * vertexID;
    float currentDisplacementValue = (float)displacementValues[vertexIDIndex];

    // for the X axis
    float value = (currentDisplacementValue < 0.0f)
                ? (-temperature >= currentDisplacementValue) ? -temperature : currentDisplacementValue  // max(a, b)
                : ( temperature <= currentDisplacementValue) ?  temperature : currentDisplacementValue; // min(a, b)
    cachedVertexPointCoordsMatrix[vertexIDIndex] += value;

    if (cachedVertexPointCoordsMatrix[vertexIDIndex] > canvasXSize)
        cachedVertexPointCoordsMatrix[vertexIDIndex] = (float)canvasXSize;
    // commented out so as to avoid the relayout being bounded by the layout minimum threshold
    // else if (cachedVertexPointCoordsMatrix[vertexIDIndex] < 0)
    //    cachedVertexPointCoordsMatrix[vertexIDIndex] = 0;

    displacementValues[vertexIDIndex] = 0;



    // for the Y axis
    vertexIDIndex++;
    currentDisplacementValue = (float)displacementValues[vertexIDIndex];
    value = (currentDisplacementValue < 0.0f)
            ? (-temperature >= currentDisplacementValue) ? -temperature : currentDisplacementValue  // max(a, b)
            : ( temperature <= currentDisplacementValue) ?  temperature : currentDisplacementValue; // min(a, b)
    cachedVertexPointCoordsMatrix[vertexIDIndex] += value;

    if (cachedVertexPointCoordsMatrix[vertexIDIndex] > canvasYSize)
        cachedVertexPointCoordsMatrix[vertexIDIndex] = (float)canvasYSize;
    // commented out so as to avoid the relayout being bounded by the layout minimum threshold
    // else if (cachedVertexPointCoordsMatrix[vertexIDIndex] < 0)
    //    cachedVertexPointCoordsMatrix[vertexIDIndex] = 0;

    displacementValues[vertexIDIndex] = 0;



    // for the Z axis
    vertexIDIndex++;
    currentDisplacementValue = (float)displacementValues[vertexIDIndex];
    value = (currentDisplacementValue < 0.0f)
            ? (-temperature >= currentDisplacementValue) ? -temperature : currentDisplacementValue  // max(a, b)
            : ( temperature <= currentDisplacementValue) ?  temperature : currentDisplacementValue; // min(a, b)
    cachedVertexPointCoordsMatrix[vertexIDIndex] += value;

    if (cachedVertexPointCoordsMatrix[vertexIDIndex] > canvasZSize)
        cachedVertexPointCoordsMatrix[vertexIDIndex] = (float)canvasZSize;
    // commented out so as to avoid the relayout being bounded by the layout minimum threshold
    // else if (cachedVertexPointCoordsMatrix[vertexIDIndex] < 0)
    //    cachedVertexPointCoordsMatrix[vertexIDIndex] = 0;

    displacementValues[vertexIDIndex] = 0;     
}

static inline void temperatureHandling()
{
    temperature = TEMPERATURE_SCALING * temperature;
    if (temperature < 1.0f) temperature = 1.0f;
}

JNIEXPORT void JNICALL Java_org_BioLayoutExpress3D_Network_FRLayout_setAllValuesNative
(JNIEnv *env, jobject thisObject,
 jint BOOLEAN_PACKED_DATA_POWER_OF_TWO_VALUEJava, jint BOOLEAN_PACKED_DATA_BIT_SIZEJava, jbyte FIXED_POINT_DECIMAL_PART_LENGTHJava, jfloat TEMPERATURE_SCALINGJava,
 jint canvasXSizeJava, jint canvasYSizeJava, jint canvasZSizeJava, jint displacementMatrixDimensionalityJava,
 jfloat temperatureJava, jfloat kValueJava, jfloat kSquareValueJava, jfloat kDoubledJava, jboolean useEdgeWeightsJava)
{
      BOOLEAN_PACKED_DATA_POWER_OF_TWO_VALUE = BOOLEAN_PACKED_DATA_POWER_OF_TWO_VALUEJava;
      BOOLEAN_PACKED_DATA_BIT_SIZE = BOOLEAN_PACKED_DATA_BIT_SIZEJava;
      FIXED_POINT_DECIMAL_PART_LENGTH = FIXED_POINT_DECIMAL_PART_LENGTHJava;
      TEMPERATURE_SCALING = TEMPERATURE_SCALINGJava;
      canvasXSize = canvasXSizeJava;
      canvasYSize = canvasYSizeJava;
      canvasZSize = canvasZSizeJava;
      displacementMatrixDimensionality = displacementMatrixDimensionalityJava;
      temperature = temperatureJava;
      kValue = kValueJava;
      kSquareValue = kSquareValueJava;
      kDoubled = kDoubledJava;      
      useEdgeWeights = useEdgeWeightsJava;
}

JNIEXPORT void JNICALL Java_org_BioLayoutExpress3D_Network_FRLayout_setTemperatureValueNative(JNIEnv *env, jobject thisObject, jfloat temperatureJava)
{
      temperature = temperatureJava;
}

JNIEXPORT void JNICALL Java_org_BioLayoutExpress3D_Network_FRLayout_allIterationsCalcBiDirForce2DNative
(JNIEnv *env, jobject thisObject, 
 jintArray vertexIndicesMatrixJava, jfloatArray cachedVertexPointCoordsMatrixJava, jintArray cachedVertexConnectionMatrixJava, jintArray cachedVertexConnectionRowSkipSizeValuesMatrixJava,
 jshortArray cachedVertexNormalizedWeightMatrixJava, jfloatArray displacementMatrixJava, jintArray displacementValuesJava,
 jint numberOfVertices, jint iterations)
{
     jint *vertexIndicesMatrix = (*env) -> GetPrimitiveArrayCritical(env, vertexIndicesMatrixJava, 0);
     jfloat *cachedVertexPointCoordsMatrix = (*env) -> GetPrimitiveArrayCritical(env, cachedVertexPointCoordsMatrixJava, 0);
     jint *cachedVertexConnectionMatrix = (*env) -> GetPrimitiveArrayCritical(env, cachedVertexConnectionMatrixJava, 0);
     jint *cachedVertexConnectionRowSkipSizeValuesMatrix = (*env) -> GetPrimitiveArrayCritical(env, cachedVertexConnectionRowSkipSizeValuesMatrixJava, 0);
     jshort *cachedVertexNormalizedWeightMatrix = (*env) -> GetPrimitiveArrayCritical(env, cachedVertexNormalizedWeightMatrixJava, 0);
     jfloat *displacementMatrix = (*env) -> GetPrimitiveArrayCritical(env, displacementMatrixJava, 0);
     jint *displacementValues = (*env) -> GetPrimitiveArrayCritical(env, displacementValuesJava, 0);

     jclass frLayoutClass = (*env) -> GetObjectClass(env, thisObject);
     jmethodID methodID = (*env) -> GetMethodID(env, frLayoutClass, NAME_OF_UPDATE_GUI_JAVA_FUNCTION, RETURN_TYPE_OF_UPDATE_GUI_JAVA_FUNCTION);
     
     // main common layout algorithm code
     
     int cachedVertexNormalizedWeightIndex = 0;
     int from = numberOfVertices;
     int to = 0;
     int vertexID = 0;
     while (--iterations >= 0)
     {     
         cachedVertexNormalizedWeightIndex = 0;
         from = numberOfVertices;
         to = 0;
         while (--from >= 0)
         // for (int from = 0; from < numberOfVertices; from++)
         {
             to = numberOfVertices;
             while (--to >= from + 1)
             // for (int to = from + 1; to < numberOfVertices; to++)
                 calcBiDirForce2D(cachedVertexPointCoordsMatrix, cachedVertexConnectionMatrix, cachedVertexConnectionRowSkipSizeValuesMatrix, cachedVertexNormalizedWeightMatrix, displacementMatrix, displacementValues,
                                  vertexIndicesMatrix[from], vertexIndicesMatrix[to], &cachedVertexNormalizedWeightIndex); // for passing by reference simulation in C
         }

         vertexID = numberOfVertices;
         while (--vertexID >= 0)
             set2DForceToVertex(displacementValues, cachedVertexPointCoordsMatrix, vertexIndicesMatrix[vertexID]);     

         temperatureHandling();

         (*env ) -> CallVoidMethod(env, thisObject, methodID);
     }
     
     // end of common layout algorithm code

     (*env) -> DeleteLocalRef(env, frLayoutClass);

     (*env) -> ReleasePrimitiveArrayCritical(env, vertexIndicesMatrixJava, vertexIndicesMatrix, 0);
     (*env) -> ReleasePrimitiveArrayCritical(env, cachedVertexPointCoordsMatrixJava, cachedVertexPointCoordsMatrix, 0);
     (*env) -> ReleasePrimitiveArrayCritical(env, cachedVertexConnectionMatrixJava, cachedVertexConnectionMatrix, 0);
     (*env) -> ReleasePrimitiveArrayCritical(env, cachedVertexConnectionRowSkipSizeValuesMatrixJava, cachedVertexConnectionRowSkipSizeValuesMatrix, 0);
     (*env) -> ReleasePrimitiveArrayCritical(env, cachedVertexNormalizedWeightMatrixJava, cachedVertexNormalizedWeightMatrix, 0);               
     (*env) -> ReleasePrimitiveArrayCritical(env, displacementMatrixJava, displacementMatrix, 0);
     (*env) -> ReleasePrimitiveArrayCritical(env, displacementValuesJava, displacementValues, 0);        
}

JNIEXPORT void JNICALL Java_org_BioLayoutExpress3D_Network_FRLayout_iterateCalcBiDirForce2DNative
(JNIEnv *env, jobject thisObject, 
 jintArray vertexIndicesMatrixJava, jfloatArray cachedVertexPointCoordsMatrixJava, jintArray cachedVertexConnectionMatrixJava, jintArray cachedVertexConnectionRowSkipSizeValuesMatrixJava,
 jshortArray cachedVertexNormalizedWeightMatrixJava, jfloatArray displacementMatrixJava, jintArray displacementValuesJava,
 jint numberOfVertices)
{
     jint *vertexIndicesMatrix = (*env) -> GetPrimitiveArrayCritical(env, vertexIndicesMatrixJava, 0);
     jfloat *cachedVertexPointCoordsMatrix = (*env) -> GetPrimitiveArrayCritical(env, cachedVertexPointCoordsMatrixJava, 0);
     jint *cachedVertexConnectionMatrix = (*env) -> GetPrimitiveArrayCritical(env, cachedVertexConnectionMatrixJava, 0);
     jint *cachedVertexConnectionRowSkipSizeValuesMatrix = (*env) -> GetPrimitiveArrayCritical(env, cachedVertexConnectionRowSkipSizeValuesMatrixJava, 0);
     jshort *cachedVertexNormalizedWeightMatrix = (*env) -> GetPrimitiveArrayCritical(env, cachedVertexNormalizedWeightMatrixJava, 0);
     jfloat *displacementMatrix = (*env) -> GetPrimitiveArrayCritical(env, displacementMatrixJava, 0);
     jint *displacementValues = (*env) -> GetPrimitiveArrayCritical(env, displacementValuesJava, 0);
     
     // main common layout algorithm code
     
     int cachedVertexNormalizedWeightIndex = 0;
     int from = numberOfVertices;
     int to = 0;
     while (--from >= 0)
     // for (int from = 0; from < numberOfVertices; from++)
     {
         to = numberOfVertices;
         while (--to >= from + 1)
         // for (int to = from + 1; to < numberOfVertices; to++)
             calcBiDirForce2D(cachedVertexPointCoordsMatrix, cachedVertexConnectionMatrix, cachedVertexConnectionRowSkipSizeValuesMatrix, cachedVertexNormalizedWeightMatrix, displacementMatrix, displacementValues,
                              vertexIndicesMatrix[from], vertexIndicesMatrix[to], &cachedVertexNormalizedWeightIndex); // for passing by reference simulation in C
     }

     int vertexID = numberOfVertices;
     while (--vertexID >= 0)
         set2DForceToVertex(displacementValues, cachedVertexPointCoordsMatrix, vertexIndicesMatrix[vertexID]);     
     
    temperatureHandling();
     
     // end of common layout algorithm code
     
     (*env) -> ReleasePrimitiveArrayCritical(env, vertexIndicesMatrixJava, vertexIndicesMatrix, 0);
     (*env) -> ReleasePrimitiveArrayCritical(env, cachedVertexPointCoordsMatrixJava, cachedVertexPointCoordsMatrix, 0);
     (*env) -> ReleasePrimitiveArrayCritical(env, cachedVertexConnectionMatrixJava, cachedVertexConnectionMatrix, 0);
     (*env) -> ReleasePrimitiveArrayCritical(env, cachedVertexConnectionRowSkipSizeValuesMatrixJava, cachedVertexConnectionRowSkipSizeValuesMatrix, 0);
     (*env) -> ReleasePrimitiveArrayCritical(env, cachedVertexNormalizedWeightMatrixJava, cachedVertexNormalizedWeightMatrix, 0);               
     (*env) -> ReleasePrimitiveArrayCritical(env, displacementMatrixJava, displacementMatrix, 0);
     (*env) -> ReleasePrimitiveArrayCritical(env, displacementValuesJava, displacementValues, 0);                  
}

JNIEXPORT void JNICALL Java_org_BioLayoutExpress3D_Network_FRLayout_iterateCalcBiDirForce2DThreadNative
(JNIEnv *env, jobject thisObject,
 jboolean isPowerOfTwo, jintArray vertexIndicesMatrixJava, jfloatArray cachedVertexPointCoordsMatrixJava, jintArray cachedVertexConnectionMatrixJava, jintArray cachedVertexConnectionRowSkipSizeValuesMatrixJava,
 jshortArray cachedVertexNormalizedWeightMatrixJava, jfloatArray displacementMatrixJava, jintArray displacementValuesJava,
 jintArray cachedVertexNormalizedWeightIndicesToSkipJava, jint NUMBER_OF_AVAILABLE_PROCESSORS, jint numberOfVertices, jint threadId)
{
    jint *vertexIndicesMatrix = (*env) -> GetPrimitiveArrayCritical(env, vertexIndicesMatrixJava, 0);
    jfloat *cachedVertexPointCoordsMatrix = (*env) -> GetPrimitiveArrayCritical(env, cachedVertexPointCoordsMatrixJava, 0);
    jint *cachedVertexConnectionMatrix = (*env) -> GetPrimitiveArrayCritical(env, cachedVertexConnectionMatrixJava, 0);
    jint *cachedVertexConnectionRowSkipSizeValuesMatrix = (*env) -> GetPrimitiveArrayCritical(env, cachedVertexConnectionRowSkipSizeValuesMatrixJava, 0);
    jshort *cachedVertexNormalizedWeightMatrix = (*env) -> GetPrimitiveArrayCritical(env, cachedVertexNormalizedWeightMatrixJava, 0);
    jfloat *displacementMatrix = (*env) -> GetPrimitiveArrayCritical(env, displacementMatrixJava, 0);
    jint *displacementValues = (*env) -> GetPrimitiveArrayCritical(env, displacementValuesJava, 0);
    jint *cachedVertexNormalizedWeightIndicesToSkip = (*env) -> GetPrimitiveArrayCritical(env, cachedVertexNormalizedWeightIndicesToSkipJava, 0);

    // main common layout algorithm code

    int cachedVertexNormalizedWeightIndex = 0;
    int from = numberOfVertices;
    int to = 0;
    if (isPowerOfTwo)
    {
        while (--from >= 0)
        // for (int from = 0; from < numberOfVertices; from++)
        {
            // distribute every (from % NUMBER_OF_AVAILABLE_PROCESSORS) execution to the given threadId
            if ( ( from & (NUMBER_OF_AVAILABLE_PROCESSORS - 1) ) == threadId )
            {
                to = numberOfVertices;
                while (--to >= from + 1)
                // for (int to = from + 1; to < numberOfVertices; to++)
                 calcBiDirForce2D(cachedVertexPointCoordsMatrix, cachedVertexConnectionMatrix, cachedVertexConnectionRowSkipSizeValuesMatrix, cachedVertexNormalizedWeightMatrix, displacementMatrix, displacementValues,
                                  vertexIndicesMatrix[from], vertexIndicesMatrix[to], &cachedVertexNormalizedWeightIndex); // for passing by reference simulation in C
            }
            else
            {
                if (useEdgeWeights)
                    cachedVertexNormalizedWeightIndex += cachedVertexNormalizedWeightIndicesToSkip[from];
            }
        }
    }
    else
    {
        while (--from >= 0)
        // for (int from = 0; from < numberOfVertices; from++)
        {
            // distribute every (from % NUMBER_OF_AVAILABLE_PROCESSORS) execution to the given threadId
            if ( (from % NUMBER_OF_AVAILABLE_PROCESSORS) == threadId )
            {
                to = numberOfVertices;
                while (--to >= from + 1)
                // for (int to = from + 1; to < numberOfVertices; to++)
                 calcBiDirForce2D(cachedVertexPointCoordsMatrix, cachedVertexConnectionMatrix, cachedVertexConnectionRowSkipSizeValuesMatrix, cachedVertexNormalizedWeightMatrix, displacementMatrix, displacementValues,
                                  vertexIndicesMatrix[from], vertexIndicesMatrix[to], &cachedVertexNormalizedWeightIndex); // for passing by reference simulation in C
            }
            else
            {
                if (useEdgeWeights)
                    cachedVertexNormalizedWeightIndex += cachedVertexNormalizedWeightIndicesToSkip[from];
            }
        }
    }

    // end of common layout algorithm code

    (*env) -> ReleasePrimitiveArrayCritical(env, vertexIndicesMatrixJava, vertexIndicesMatrix, 0);
    (*env) -> ReleasePrimitiveArrayCritical(env, cachedVertexPointCoordsMatrixJava, cachedVertexPointCoordsMatrix, 0);
    (*env) -> ReleasePrimitiveArrayCritical(env, cachedVertexConnectionMatrixJava, cachedVertexConnectionMatrix, 0);
    (*env) -> ReleasePrimitiveArrayCritical(env, cachedVertexConnectionRowSkipSizeValuesMatrixJava, cachedVertexConnectionRowSkipSizeValuesMatrix, 0);
    (*env) -> ReleasePrimitiveArrayCritical(env, cachedVertexNormalizedWeightMatrixJava, cachedVertexNormalizedWeightMatrix, 0);
    (*env) -> ReleasePrimitiveArrayCritical(env, displacementMatrixJava, displacementMatrix, 0);
    (*env) -> ReleasePrimitiveArrayCritical(env, displacementValuesJava, displacementValues, 0);
    (*env) -> ReleasePrimitiveArrayCritical(env, cachedVertexNormalizedWeightIndicesToSkipJava, cachedVertexNormalizedWeightIndicesToSkip, 0);
}

JNIEXPORT void JNICALL Java_org_BioLayoutExpress3D_Network_FRLayout_allIterationsCalcBiDirForce3DNative
(JNIEnv *env, jobject thisObject, 
 jintArray vertexIndicesMatrixJava, jfloatArray cachedVertexPointCoordsMatrixJava, jintArray cachedVertexConnectionMatrixJava, jintArray cachedVertexConnectionRowSkipSizeValuesMatrixJava,
 jintArray cachedPseudoVertexMatrixJava, jshortArray cachedVertexNormalizedWeightMatrixJava, jintArray displacementValuesJava,
 jint numberOfVertices, jint iterations)
{
     jint *vertexIndicesMatrix = (*env) -> GetPrimitiveArrayCritical(env, vertexIndicesMatrixJava, 0);
     jfloat *cachedVertexPointCoordsMatrix = (*env) -> GetPrimitiveArrayCritical(env, cachedVertexPointCoordsMatrixJava, 0);
     jint *cachedVertexConnectionMatrix = (*env) -> GetPrimitiveArrayCritical(env, cachedVertexConnectionMatrixJava, 0);
     jint *cachedVertexConnectionRowSkipSizeValuesMatrix = (*env) -> GetPrimitiveArrayCritical(env, cachedVertexConnectionRowSkipSizeValuesMatrixJava, 0);
     jint *cachedPseudoVertexMatrix = (*env) -> GetPrimitiveArrayCritical(env, cachedPseudoVertexMatrixJava, 0);
     jshort *cachedVertexNormalizedWeightMatrix = (*env) -> GetPrimitiveArrayCritical(env, cachedVertexNormalizedWeightMatrixJava, 0);
     jint *displacementValues = (*env) -> GetPrimitiveArrayCritical(env, displacementValuesJava, 0);

     jclass frLayoutClass = (*env) -> GetObjectClass(env, thisObject);
     jmethodID methodID = (*env) -> GetMethodID(env, frLayoutClass, NAME_OF_UPDATE_GUI_JAVA_FUNCTION, RETURN_TYPE_OF_UPDATE_GUI_JAVA_FUNCTION);

     // main common layout algorithm code
     
     int cachedVertexNormalizedWeightIndex = 0;
     int from = numberOfVertices;
     int to = 0;
     int vertexID = 0;
     while (--iterations >= 0)
     {     
         cachedVertexNormalizedWeightIndex = 0;
         from = numberOfVertices;
         to = 0;
         while (--from >= 0)
         // for (int from = 0; from < numberOfVertices; from++)
         {
             to = numberOfVertices;
             while (--to >= from + 1)
             // for (int to = from + 1; to < numberOfVertices; to++)
                 calcBiDirForce3D(cachedVertexPointCoordsMatrix, cachedVertexConnectionMatrix, cachedVertexConnectionRowSkipSizeValuesMatrix, cachedPseudoVertexMatrix, cachedVertexNormalizedWeightMatrix, displacementValues,
                                  vertexIndicesMatrix[from], vertexIndicesMatrix[to], &cachedVertexNormalizedWeightIndex); // for passing by reference simulation in C
         }

         vertexID = numberOfVertices;
         while (--vertexID >= 0)
             set3DForceToVertex(displacementValues, cachedVertexPointCoordsMatrix, vertexIndicesMatrix[vertexID]);        
     
         temperatureHandling();
         
         (*env ) -> CallVoidMethod(env, thisObject, methodID);
     }
     
     // end of common layout algorithm code     

     (*env) -> DeleteLocalRef(env, frLayoutClass);

     (*env) -> ReleasePrimitiveArrayCritical(env, vertexIndicesMatrixJava, vertexIndicesMatrix, 0);
     (*env) -> ReleasePrimitiveArrayCritical(env, cachedVertexPointCoordsMatrixJava, cachedVertexPointCoordsMatrix, 0);
     (*env) -> ReleasePrimitiveArrayCritical(env, cachedVertexConnectionMatrixJava, cachedVertexConnectionMatrix, 0);
     (*env) -> ReleasePrimitiveArrayCritical(env, cachedVertexConnectionRowSkipSizeValuesMatrixJava, cachedVertexConnectionRowSkipSizeValuesMatrix, 0);
     (*env) -> ReleasePrimitiveArrayCritical(env, cachedPseudoVertexMatrixJava, cachedPseudoVertexMatrix, 0);
     (*env) -> ReleasePrimitiveArrayCritical(env, cachedVertexNormalizedWeightMatrixJava, cachedVertexNormalizedWeightMatrix, 0);
     (*env) -> ReleasePrimitiveArrayCritical(env, displacementValuesJava, displacementValues, 0);
}

JNIEXPORT void JNICALL Java_org_BioLayoutExpress3D_Network_FRLayout_iterateCalcBiDirForce3DNative
(JNIEnv *env, jobject thisObject, 
 jintArray vertexIndicesMatrixJava, jfloatArray cachedVertexPointCoordsMatrixJava, jintArray cachedVertexConnectionMatrixJava, jintArray cachedVertexConnectionRowSkipSizeValuesMatrixJava,
 jintArray cachedPseudoVertexMatrixJava, jshortArray cachedVertexNormalizedWeightMatrixJava, jintArray displacementValuesJava,
 jint numberOfVertices)
{
     jint *vertexIndicesMatrix = (*env) -> GetPrimitiveArrayCritical(env, vertexIndicesMatrixJava, 0);
     jfloat *cachedVertexPointCoordsMatrix = (*env) -> GetPrimitiveArrayCritical(env, cachedVertexPointCoordsMatrixJava, 0);
     jint *cachedVertexConnectionMatrix = (*env) -> GetPrimitiveArrayCritical(env, cachedVertexConnectionMatrixJava, 0);
     jint *cachedVertexConnectionRowSkipSizeValuesMatrix = (*env) -> GetPrimitiveArrayCritical(env, cachedVertexConnectionRowSkipSizeValuesMatrixJava, 0);
     jint *cachedPseudoVertexMatrix = (*env) -> GetPrimitiveArrayCritical(env, cachedPseudoVertexMatrixJava, 0);
     jshort *cachedVertexNormalizedWeightMatrix = (*env) -> GetPrimitiveArrayCritical(env, cachedVertexNormalizedWeightMatrixJava, 0);
     jint *displacementValues = (*env) -> GetPrimitiveArrayCritical(env, displacementValuesJava, 0);
     
     // main common layout algorithm code
     
     int cachedVertexNormalizedWeightIndex = 0;
     int from = numberOfVertices;
     int to = 0;
     while (--from >= 0)
     // for (int from = 0; from < numberOfVertices; from++)
     {
         to = numberOfVertices;
         while (--to >= from + 1)
         // for (int to = from + 1; to < numberOfVertices; to++)
             calcBiDirForce3D(cachedVertexPointCoordsMatrix, cachedVertexConnectionMatrix, cachedVertexConnectionRowSkipSizeValuesMatrix, cachedPseudoVertexMatrix, cachedVertexNormalizedWeightMatrix, displacementValues,
                              vertexIndicesMatrix[from], vertexIndicesMatrix[to], &cachedVertexNormalizedWeightIndex); // for passing by reference simulation in C
     }

     int vertexID = numberOfVertices;
     while (--vertexID >= 0)
         set3DForceToVertex(displacementValues, cachedVertexPointCoordsMatrix, vertexIndicesMatrix[vertexID]);        
     
     temperatureHandling();
     
     // end of common layout algorithm code     
     
     (*env) -> ReleasePrimitiveArrayCritical(env, vertexIndicesMatrixJava, vertexIndicesMatrix, 0);
     (*env) -> ReleasePrimitiveArrayCritical(env, cachedVertexPointCoordsMatrixJava, cachedVertexPointCoordsMatrix, 0);
     (*env) -> ReleasePrimitiveArrayCritical(env, cachedVertexConnectionMatrixJava, cachedVertexConnectionMatrix, 0);
     (*env) -> ReleasePrimitiveArrayCritical(env, cachedVertexConnectionRowSkipSizeValuesMatrixJava, cachedVertexConnectionRowSkipSizeValuesMatrix, 0);
     (*env) -> ReleasePrimitiveArrayCritical(env, cachedPseudoVertexMatrixJava, cachedPseudoVertexMatrix, 0);               
     (*env) -> ReleasePrimitiveArrayCritical(env, cachedVertexNormalizedWeightMatrixJava, cachedVertexNormalizedWeightMatrix, 0);
     (*env) -> ReleasePrimitiveArrayCritical(env, displacementValuesJava, displacementValues, 0);     
}

JNIEXPORT void JNICALL Java_org_BioLayoutExpress3D_Network_FRLayout_iterateCalcBiDirForce3DThreadNative
(JNIEnv *env, jobject thisObject,
 jboolean isPowerOfTwo, jintArray vertexIndicesMatrixJava, jfloatArray cachedVertexPointCoordsMatrixJava, jintArray cachedVertexConnectionMatrixJava, jintArray cachedVertexConnectionRowSkipSizeValuesMatrixJava,
 jintArray cachedPseudoVertexMatrixJava, jshortArray cachedVertexNormalizedWeightMatrixJava, jintArray displacementValuesJava,
 jintArray cachedVertexNormalizedWeightIndicesToSkipJava, jint NUMBER_OF_AVAILABLE_PROCESSORS, jint numberOfVertices, jint threadId)
{
    jint *vertexIndicesMatrix = (*env) -> GetPrimitiveArrayCritical(env, vertexIndicesMatrixJava, 0);
    jfloat *cachedVertexPointCoordsMatrix = (*env) -> GetPrimitiveArrayCritical(env, cachedVertexPointCoordsMatrixJava, 0);
    jint *cachedVertexConnectionMatrix = (*env) -> GetPrimitiveArrayCritical(env, cachedVertexConnectionMatrixJava, 0);
    jint *cachedVertexConnectionRowSkipSizeValuesMatrix = (*env) -> GetPrimitiveArrayCritical(env, cachedVertexConnectionRowSkipSizeValuesMatrixJava, 0);
    jint *cachedPseudoVertexMatrix = (*env) -> GetPrimitiveArrayCritical(env, cachedPseudoVertexMatrixJava, 0);
    jshort *cachedVertexNormalizedWeightMatrix = (*env) -> GetPrimitiveArrayCritical(env, cachedVertexNormalizedWeightMatrixJava, 0);
    jint *displacementValues = (*env) -> GetPrimitiveArrayCritical(env, displacementValuesJava, 0);
    jint *cachedVertexNormalizedWeightIndicesToSkip = (*env) -> GetPrimitiveArrayCritical(env, cachedVertexNormalizedWeightIndicesToSkipJava, 0);

    // main common layout algorithm code

    int cachedVertexNormalizedWeightIndex = 0;
    int from = numberOfVertices;
    int to = 0;
    if (isPowerOfTwo)
    {
        while (--from >= 0)
        // for (int from = 0; from < numberOfVertices; from++)
        {
            // distribute every (from % NUMBER_OF_AVAILABLE_PROCESSORS) execution to the given threadId
            if ( ( from & (NUMBER_OF_AVAILABLE_PROCESSORS - 1) ) == threadId )
            {
                to = numberOfVertices;
                while (--to >= from + 1)
                // for (int to = from + 1; to < numberOfVertices; to++)
                 calcBiDirForce3D(cachedVertexPointCoordsMatrix, cachedVertexConnectionMatrix, cachedVertexConnectionRowSkipSizeValuesMatrix, cachedPseudoVertexMatrix, cachedVertexNormalizedWeightMatrix, displacementValues,
                                  vertexIndicesMatrix[from], vertexIndicesMatrix[to], &cachedVertexNormalizedWeightIndex); // for passing by reference simulation in C
            }
            else
            {
                if (useEdgeWeights)
                    cachedVertexNormalizedWeightIndex += cachedVertexNormalizedWeightIndicesToSkip[from];
            }
        }
    }
    else
    {
        while (--from >= 0)
        // for (int from = 0; from < numberOfVertices; from++)
        {
            // distribute every (from % NUMBER_OF_AVAILABLE_PROCESSORS) execution to the given threadId
            if ( (from % NUMBER_OF_AVAILABLE_PROCESSORS) == threadId )
            {
                to = numberOfVertices;
                while (--to >= from + 1)
                // for (int to = from + 1; to < numberOfVertices; to++)
                 calcBiDirForce3D(cachedVertexPointCoordsMatrix, cachedVertexConnectionMatrix, cachedVertexConnectionRowSkipSizeValuesMatrix, cachedPseudoVertexMatrix, cachedVertexNormalizedWeightMatrix, displacementValues,
                                  vertexIndicesMatrix[from], vertexIndicesMatrix[to], &cachedVertexNormalizedWeightIndex); // for passing by reference simulation in C
            }
            else
            {
                if (useEdgeWeights)
                    cachedVertexNormalizedWeightIndex += cachedVertexNormalizedWeightIndicesToSkip[from];
            }
        }
    }

    // end of common layout algorithm code

    (*env) -> ReleasePrimitiveArrayCritical(env, vertexIndicesMatrixJava, vertexIndicesMatrix, 0);
    (*env) -> ReleasePrimitiveArrayCritical(env, cachedVertexPointCoordsMatrixJava, cachedVertexPointCoordsMatrix, 0);
    (*env) -> ReleasePrimitiveArrayCritical(env, cachedVertexConnectionMatrixJava, cachedVertexConnectionMatrix, 0);
    (*env) -> ReleasePrimitiveArrayCritical(env, cachedVertexConnectionRowSkipSizeValuesMatrixJava, cachedVertexConnectionRowSkipSizeValuesMatrix, 0);
    (*env) -> ReleasePrimitiveArrayCritical(env, cachedPseudoVertexMatrixJava, cachedPseudoVertexMatrix, 0);
    (*env) -> ReleasePrimitiveArrayCritical(env, cachedVertexNormalizedWeightMatrixJava, cachedVertexNormalizedWeightMatrix, 0);
    (*env) -> ReleasePrimitiveArrayCritical(env, displacementValuesJava, displacementValues, 0);
    (*env) -> ReleasePrimitiveArrayCritical(env, cachedVertexNormalizedWeightIndicesToSkipJava, cachedVertexNormalizedWeightIndicesToSkip, 0);
}
