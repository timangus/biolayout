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
#include "org/BioLayoutExpress3D/Expression/ExpressionData.h"



// Note: this dll is used statically, i.e. the same dll for all the ExpressionData instances.
// Other tricks, like using multiple pointer references and C structs should be used for simulating 
// on the C side multiple Java objects instatiations from the Java side.


#define NAME_OF_UPDATE_GUI_JAVA_FUNCTION "updateMultiCoreGUI"
#define RETURN_TYPE_OF_UPDATE_GUI_JAVA_FUNCTION "()V"

static inline jfloat calculateCorrelation(jint firstRow, jint secondRow, jfloat *matrix, jfloat *sumColumns_X2_cacheArray, jfloat *sumX_sumX2_cacheArray, jfloat *sumX_cacheArray, jint totalColumns)
{
    float denominator = sqrtf( (sumColumns_X2_cacheArray[firstRow] - sumX_sumX2_cacheArray[firstRow]) * (sumColumns_X2_cacheArray[secondRow] - sumX_sumX2_cacheArray[secondRow]) );
    if ( (denominator != 0.0f) && !(denominator != denominator) ) // second check is to avoid an NaN problem, see definition of Float.isNaN()
    {
        int indexFirstRowDimension = firstRow * totalColumns;
        int indexSecondRowDimension = secondRow * totalColumns;
        float sumXY = 0.0f;
        int i;
        for (i = 0; i < totalColumns; i++)
            sumXY += (matrix[indexFirstRowDimension + i] * matrix[indexSecondRowDimension + i]);

        float result = ( ((float)totalColumns * sumXY) - (sumX_cacheArray[firstRow] * sumX_cacheArray[secondRow]) ) / denominator;
        return (result > 1.0f) ? 1.0f : ( (result < -1.0f) ? -1.0f : result );
    }
    else
        return -1.0f;
}

JNIEXPORT void JNICALL Java_org_BioLayoutExpress3D_Expression_ExpressionData_allCorrelationCalculationsNative
(JNIEnv *env, jobject thisObject,
 jint threadId, jboolean isPowerOfTwo, jint startRow, jint endRow, jfloatArray stepResultsJava, jintArray cachedRowsResultsIndicesToSkipJava,
 jint totalColumns, jint totalRows, jfloatArray sumColumns_X2_cacheArrayJava, jfloatArray sumX_sumX2_cacheArrayJava, jfloatArray sumX_cacheArrayJava,
 jfloatArray expressionDataArrayJava, jint NUMBER_OF_AVAILABLE_PROCESSORS)
{
    // initialize pointer to Java arrays so as to make JNI to 'pin' down the array
    jfloat *stepResults = (*env) -> GetPrimitiveArrayCritical(env, stepResultsJava, 0);

    // initialize pointer to Java arrays so as to make JNI to 'pin' down the array
    jint *cachedRowsResultsIndicesToSkip = (*env) -> GetPrimitiveArrayCritical(env, cachedRowsResultsIndicesToSkipJava, 0);

    // initialize pointer to Java arrays so as to make JNI to 'pin' down the array
    jfloat *sumColumns_X2_cacheArray = (*env) -> GetPrimitiveArrayCritical(env, sumColumns_X2_cacheArrayJava, 0);

    // initialize pointer to Java arrays so as to make JNI to 'pin' down the array
    jfloat *sumX_sumX2_cacheArray = (*env) -> GetPrimitiveArrayCritical(env, sumX_sumX2_cacheArrayJava, 0);

    // initialize pointer to Java arrays so as to make JNI to 'pin' down the array
    jfloat *sumX_cacheArray = (*env) -> GetPrimitiveArrayCritical(env, sumX_cacheArrayJava, 0);

    // initialize pointer to Java arrays so as to make JNI to 'pin' down the array
    jfloat *expressionDataArray = (*env) -> GetPrimitiveArrayCritical(env, expressionDataArrayJava, 0);

    jclass correlationCalculationClass = (*env) -> GetObjectClass(env, thisObject);
    jmethodID methodID = (*env) -> GetMethodID(env, correlationCalculationClass, NAME_OF_UPDATE_GUI_JAVA_FUNCTION, RETURN_TYPE_OF_UPDATE_GUI_JAVA_FUNCTION);

    // main common correlation calculation code

    jint rowResultIndex = 0;
    jint i, j;

    if (isPowerOfTwo)
    {
        for (i = startRow; i <= endRow; i++)
        {
            if ( ( i & (NUMBER_OF_AVAILABLE_PROCESSORS - 1) ) == threadId )
            {
                (*env ) -> CallVoidMethod(env, thisObject, methodID);

                for (j = (i + 1); j < totalRows; j++)
                    stepResults[rowResultIndex++] = calculateCorrelation(i, j, expressionDataArray, sumColumns_X2_cacheArray, sumX_sumX2_cacheArray, sumX_cacheArray, totalColumns);                
            }
            else
            {
                rowResultIndex += cachedRowsResultsIndicesToSkip[i];
            }
        }
    }
    else
    {
        for (i = startRow; i <= endRow; i++)
        {
            if ( (i % NUMBER_OF_AVAILABLE_PROCESSORS) == threadId )
            {
                (*env ) -> CallVoidMethod(env, thisObject, methodID);

                for (j = (i + 1); j < totalRows; j++)
                    stepResults[rowResultIndex++] = calculateCorrelation(i, j, expressionDataArray, sumColumns_X2_cacheArray, sumX_sumX2_cacheArray, sumX_cacheArray, totalColumns);
            }
            else
            {
                rowResultIndex += cachedRowsResultsIndicesToSkip[i];
            }
        }
    }

    // end of common correlation calculation code

    (*env) -> DeleteLocalRef(env, correlationCalculationClass);

    // de-init ('un-pin') all Java arrays from their pinning C pointers
    (*env) -> ReleasePrimitiveArrayCritical(env, stepResultsJava, stepResults, 0);

    // de-init ('un-pin') all Java arrays from their pinning C pointers
    (*env) -> ReleasePrimitiveArrayCritical(env, cachedRowsResultsIndicesToSkipJava, cachedRowsResultsIndicesToSkip, 0);

    // de-init ('un-pin') all Java arrays from their pinning C pointers
    (*env) -> ReleasePrimitiveArrayCritical(env, sumColumns_X2_cacheArrayJava, sumColumns_X2_cacheArray, 0);

    // de-init ('un-pin') all Java arrays from their pinning C pointers
    (*env) -> ReleasePrimitiveArrayCritical(env, sumX_sumX2_cacheArrayJava, sumX_sumX2_cacheArray, 0);

    // de-init ('un-pin') all Java arrays from their pinning C pointers
    (*env) -> ReleasePrimitiveArrayCritical(env, sumX_cacheArrayJava, sumX_cacheArray, 0);

    // de-init ('un-pin') all Java arrays from their pinning C pointers    
    (*env) -> ReleasePrimitiveArrayCritical(env, expressionDataArrayJava, expressionDataArray, 0);
}

JNIEXPORT void JNICALL Java_org_BioLayoutExpress3D_Expression_ExpressionData_allCorrelationCalculationsForExpressionDataComputingSingleCoreNative__ZI_3I_3F_3F_3F_3F_3FI
(JNIEnv *env, jobject thisObject,
 jboolean usePairIndices, jint totalColumns, jintArray indexXYArrayJava, jfloatArray sumColumns_X2_cacheArrayJava, jfloatArray sumX_sumX2_cacheArrayJava,
 jfloatArray sumX_cacheArrayJava, jfloatArray expressionDataArrayJava, jfloatArray dataResultsCPUJava, jint N)
{
    // initialize pointer to Java arrays so as to make JNI to 'pin' down the array
    jint *indexXYArray = (*env) -> GetPrimitiveArrayCritical(env, indexXYArrayJava, 0);

    // initialize pointer to Java arrays so as to make JNI to 'pin' down the array
    jfloat *sumColumns_X2_cacheArray = (*env) -> GetPrimitiveArrayCritical(env, sumColumns_X2_cacheArrayJava, 0);

    // initialize pointer to Java arrays so as to make JNI to 'pin' down the array
    jfloat *sumX_sumX2_cacheArray = (*env) -> GetPrimitiveArrayCritical(env, sumX_sumX2_cacheArrayJava, 0);

    // initialize pointer to Java arrays so as to make JNI to 'pin' down the array
    jfloat *sumX_cacheArray = (*env) -> GetPrimitiveArrayCritical(env, sumX_cacheArrayJava, 0);

    // initialize pointer to Java arrays so as to make JNI to 'pin' down the array
    jfloat *expressionDataArray = (*env) -> GetPrimitiveArrayCritical(env, expressionDataArrayJava, 0);

    // initialize pointer to Java arrays so as to make JNI to 'pin' down the array
    jfloat *dataResultsCPU = (*env) -> GetPrimitiveArrayCritical(env, dataResultsCPUJava, 0);
    int i;

    // main common correlation calculation code

    if (!usePairIndices)
    {
        int index = 0;
        for (i = 0; i < N; i++)
        {
            index = indexXYArray[i];
            dataResultsCPU[i] = calculateCorrelation( ( (index >> 16) & 0xFFFF), (index & 0xFFFF), expressionDataArray, sumColumns_X2_cacheArray, sumX_sumX2_cacheArray, sumX_cacheArray, totalColumns );
        }
    }
    else // use Java code
    {
        int index = 0;
        for (i = 0; i < N; i++)
        {
            index = i + i;
            dataResultsCPU[i] = calculateCorrelation(indexXYArray[index], indexXYArray[index + 1], expressionDataArray, sumColumns_X2_cacheArray, sumX_sumX2_cacheArray, sumX_cacheArray, totalColumns);
        }
    }

    // end of common correlation calculation code

    // de-init ('un-pin') all Java arrays from their pinning C pointers
    (*env) -> ReleasePrimitiveArrayCritical(env, indexXYArrayJava, indexXYArray, 0);

    // de-init ('un-pin') all Java arrays from their pinning C pointers
    (*env) -> ReleasePrimitiveArrayCritical(env, sumColumns_X2_cacheArrayJava, sumColumns_X2_cacheArray, 0);

    // de-init ('un-pin') all Java arrays from their pinning C pointers
    (*env) -> ReleasePrimitiveArrayCritical(env, sumX_sumX2_cacheArrayJava, sumX_sumX2_cacheArray, 0);

    // de-init ('un-pin') all Java arrays from their pinning C pointers
    (*env) -> ReleasePrimitiveArrayCritical(env, sumX_cacheArrayJava, sumX_cacheArray, 0);

    // de-init ('un-pin') all Java arrays from their pinning C pointers
    (*env) -> ReleasePrimitiveArrayCritical(env, expressionDataArrayJava, expressionDataArray, 0);

    // de-init ('un-pin') all Java arrays from their pinning C pointers
    (*env) -> ReleasePrimitiveArrayCritical(env, dataResultsCPUJava, dataResultsCPU, 0);
}

JNIEXPORT void JNICALL Java_org_BioLayoutExpress3D_Expression_ExpressionData_allCorrelationCalculationsForExpressionDataComputingSingleCoreNative__I_3F_3F_3F_3F_3F_3F_3FI
(JNIEnv *env, jobject thisObject,
 jint totalColumns, jfloatArray indexXArrayJava, jfloatArray indexYArrayJava, jfloatArray sumColumns_X2_cacheArrayJava, jfloatArray sumX_sumX2_cacheArrayJava,
 jfloatArray sumX_cacheArrayJava, jfloatArray expressionDataArrayJava, jfloatArray dataResultsCPUJava, jint N)
{
    // initialize pointer to Java arrays so as to make JNI to 'pin' down the array
    jfloat *indexXArray = (*env) -> GetPrimitiveArrayCritical(env, indexXArrayJava, 0);

    // initialize pointer to Java arrays so as to make JNI to 'pin' down the array
    jfloat *indexYArray = (*env) -> GetPrimitiveArrayCritical(env, indexYArrayJava, 0);

    // initialize pointer to Java arrays so as to make JNI to 'pin' down the array
    jfloat *sumColumns_X2_cacheArray = (*env) -> GetPrimitiveArrayCritical(env, sumColumns_X2_cacheArrayJava, 0);

    // initialize pointer to Java arrays so as to make JNI to 'pin' down the array
    jfloat *sumX_sumX2_cacheArray = (*env) -> GetPrimitiveArrayCritical(env, sumX_sumX2_cacheArrayJava, 0);

    // initialize pointer to Java arrays so as to make JNI to 'pin' down the array
    jfloat *sumX_cacheArray = (*env) -> GetPrimitiveArrayCritical(env, sumX_cacheArrayJava, 0);

    // initialize pointer to Java arrays so as to make JNI to 'pin' down the array
    jfloat *expressionDataArray = (*env) -> GetPrimitiveArrayCritical(env, expressionDataArrayJava, 0);

    // initialize pointer to Java arrays so as to make JNI to 'pin' down the array
    jfloat *dataResultsCPU = (*env) -> GetPrimitiveArrayCritical(env, dataResultsCPUJava, 0);

    int i;

    // main common correlation calculation code

    for (i = 0; i < N; i++)
        dataResultsCPU[i] = calculateCorrelation( (int)indexXArray[i], (int)indexYArray[i], expressionDataArray, sumColumns_X2_cacheArray, sumX_sumX2_cacheArray, sumX_cacheArray, totalColumns );

    // end of common correlation calculation code

    // de-init ('un-pin') all Java arrays from their pinning C pointers
    (*env) -> ReleasePrimitiveArrayCritical(env, indexXArrayJava, indexXArray, 0);

    // de-init ('un-pin') all Java arrays from their pinning C pointers
    (*env) -> ReleasePrimitiveArrayCritical(env, indexYArrayJava, indexYArray, 0);

    // de-init ('un-pin') all Java arrays from their pinning C pointers
    (*env) -> ReleasePrimitiveArrayCritical(env, sumColumns_X2_cacheArrayJava, sumColumns_X2_cacheArray, 0);

    // de-init ('un-pin') all Java arrays from their pinning C pointers
    (*env) -> ReleasePrimitiveArrayCritical(env, sumX_sumX2_cacheArrayJava, sumX_sumX2_cacheArray, 0);

    // de-init ('un-pin') all Java arrays from their pinning C pointers
    (*env) -> ReleasePrimitiveArrayCritical(env, sumX_cacheArrayJava, sumX_cacheArray, 0);

    // de-init ('un-pin') all Java arrays from their pinning C pointers
    (*env) -> ReleasePrimitiveArrayCritical(env, expressionDataArrayJava, expressionDataArray, 0);

    // de-init ('un-pin') all Java arrays from their pinning C pointers
    (*env) -> ReleasePrimitiveArrayCritical(env, dataResultsCPUJava, dataResultsCPU, 0);
}

JNIEXPORT void JNICALL Java_org_BioLayoutExpress3D_Expression_ExpressionData_allCorrelationCalculationsForExpressionDataComputingNCPNative__IIZI_3I_3F_3F_3F_3F_3FII
(JNIEnv *env, jobject thisObject,
 jint threadId, jint totalLoopsPerProcess, jboolean usePairIndices, jint totalColumns, jintArray indexXYArrayJava, jfloatArray sumColumns_X2_cacheArrayJava, jfloatArray sumX_sumX2_cacheArrayJava,
 jfloatArray sumX_cacheArrayJava, jfloatArray expressionDataArrayJava, jfloatArray dataResultsCPUJava, jint N, jint NUMBER_OF_AVAILABLE_PROCESSORS)
{
    // initialize pointer to Java arrays so as to make JNI to 'pin' down the array
    jint *indexXYArray = (*env) -> GetPrimitiveArrayCritical(env, indexXYArrayJava, 0);

    // initialize pointer to Java arrays so as to make JNI to 'pin' down the array
    jfloat *sumColumns_X2_cacheArray = (*env) -> GetPrimitiveArrayCritical(env, sumColumns_X2_cacheArrayJava, 0);

    // initialize pointer to Java arrays so as to make JNI to 'pin' down the array
    jfloat *sumX_sumX2_cacheArray = (*env) -> GetPrimitiveArrayCritical(env, sumX_sumX2_cacheArrayJava, 0);

    // initialize pointer to Java arrays so as to make JNI to 'pin' down the array
    jfloat *sumX_cacheArray = (*env) -> GetPrimitiveArrayCritical(env, sumX_cacheArrayJava, 0);

    // initialize pointer to Java arrays so as to make JNI to 'pin' down the array
    jfloat *expressionDataArray = (*env) -> GetPrimitiveArrayCritical(env, expressionDataArrayJava, 0);

    // initialize pointer to Java arrays so as to make JNI to 'pin' down the array
    jfloat *dataResultsCPU = (*env) -> GetPrimitiveArrayCritical(env, dataResultsCPUJava, 0);

    // main common correlation calculation code

    int extraLoops = ( threadId == (NUMBER_OF_AVAILABLE_PROCESSORS - 1) ) ? (N % NUMBER_OF_AVAILABLE_PROCESSORS) : 0;
    int index = 0;
    int i;

    if (!usePairIndices)
    {
        for (i = threadId * totalLoopsPerProcess; i < (threadId + 1) * totalLoopsPerProcess + extraLoops; i++)
        {
            index = indexXYArray[i];
            dataResultsCPU[i] = calculateCorrelation( ( (index >> 16) & 0xFFFF), (index & 0xFFFF), expressionDataArray, sumColumns_X2_cacheArray, sumX_sumX2_cacheArray, sumX_cacheArray, totalColumns);
        }
    }
    else
    {
        for (i = threadId * totalLoopsPerProcess; i < (threadId + 1) * totalLoopsPerProcess + extraLoops; i++)
        {
            index = i + i;
            dataResultsCPU[i] = calculateCorrelation(indexXYArray[index], indexXYArray[index + 1], expressionDataArray, sumColumns_X2_cacheArray, sumX_sumX2_cacheArray, sumX_cacheArray, totalColumns);
        }
    }

    // end of common correlation calculation code

    // de-init ('un-pin') all Java arrays from their pinning C pointers
    (*env) -> ReleasePrimitiveArrayCritical(env, indexXYArrayJava, indexXYArray, 0);

    // de-init ('un-pin') all Java arrays from their pinning C pointers
    (*env) -> ReleasePrimitiveArrayCritical(env, sumColumns_X2_cacheArrayJava, sumColumns_X2_cacheArray, 0);

    // de-init ('un-pin') all Java arrays from their pinning C pointers
    (*env) -> ReleasePrimitiveArrayCritical(env, sumX_sumX2_cacheArrayJava, sumX_sumX2_cacheArray, 0);

    // de-init ('un-pin') all Java arrays from their pinning C pointers
    (*env) -> ReleasePrimitiveArrayCritical(env, sumX_cacheArrayJava, sumX_cacheArray, 0);

    // de-init ('un-pin') all Java arrays from their pinning C pointers
    (*env) -> ReleasePrimitiveArrayCritical(env, expressionDataArrayJava, expressionDataArray, 0);

    // de-init ('un-pin') all Java arrays from their pinning C pointers
    (*env) -> ReleasePrimitiveArrayCritical(env, dataResultsCPUJava, dataResultsCPU, 0);
}

JNIEXPORT void JNICALL Java_org_BioLayoutExpress3D_Expression_ExpressionData_allCorrelationCalculationsForExpressionDataComputingNCPNative__III_3F_3F_3F_3F_3F_3F_3FII
(JNIEnv *env, jobject thisObject,
 jint threadId, jint totalLoopsPerProcess, jint totalColumns, jfloatArray indexXArrayJava, jfloatArray indexYArrayJava, jfloatArray sumColumns_X2_cacheArrayJava, jfloatArray sumX_sumX2_cacheArrayJava,
 jfloatArray sumX_cacheArrayJava, jfloatArray expressionDataArrayJava, jfloatArray dataResultsCPUJava, jint N, jint NUMBER_OF_AVAILABLE_PROCESSORS)
{
    // initialize pointer to Java arrays so as to make JNI to 'pin' down the array
    jfloat *indexXArray = (*env) -> GetPrimitiveArrayCritical(env, indexXArrayJava, 0);

    // initialize pointer to Java arrays so as to make JNI to 'pin' down the array
    jfloat *indexYArray = (*env) -> GetPrimitiveArrayCritical(env, indexYArrayJava, 0);

    // initialize pointer to Java arrays so as to make JNI to 'pin' down the array
    jfloat *sumColumns_X2_cacheArray = (*env) -> GetPrimitiveArrayCritical(env, sumColumns_X2_cacheArrayJava, 0);

    // initialize pointer to Java arrays so as to make JNI to 'pin' down the array
    jfloat *sumX_sumX2_cacheArray = (*env) -> GetPrimitiveArrayCritical(env, sumX_sumX2_cacheArrayJava, 0);

    // initialize pointer to Java arrays so as to make JNI to 'pin' down the array
    jfloat *sumX_cacheArray = (*env) -> GetPrimitiveArrayCritical(env, sumX_cacheArrayJava, 0);

    // initialize pointer to Java arrays so as to make JNI to 'pin' down the array
    jfloat *expressionDataArray = (*env) -> GetPrimitiveArrayCritical(env, expressionDataArrayJava, 0);

    // initialize pointer to Java arrays so as to make JNI to 'pin' down the array
    jfloat *dataResultsCPU = (*env) -> GetPrimitiveArrayCritical(env, dataResultsCPUJava, 0);

    // main common correlation calculation code

    int extraLoops = ( threadId == (NUMBER_OF_AVAILABLE_PROCESSORS - 1) ) ? (N % NUMBER_OF_AVAILABLE_PROCESSORS) : 0;
    int i;
    for (i = threadId * totalLoopsPerProcess; i < (threadId + 1) * totalLoopsPerProcess + extraLoops; i++)
        dataResultsCPU[i] = calculateCorrelation( (int)indexXArray[i], (int)indexYArray[i], expressionDataArray, sumColumns_X2_cacheArray, sumX_sumX2_cacheArray, sumX_cacheArray, totalColumns );

    // end of common correlation calculation code

    // de-init ('un-pin') all Java arrays from their pinning C pointers
    (*env) -> ReleasePrimitiveArrayCritical(env, indexXArrayJava, indexXArray, 0);

    // de-init ('un-pin') all Java arrays from their pinning C pointers
    (*env) -> ReleasePrimitiveArrayCritical(env, indexYArrayJava, indexYArray, 0);

    // de-init ('un-pin') all Java arrays from their pinning C pointers
    (*env) -> ReleasePrimitiveArrayCritical(env, sumColumns_X2_cacheArrayJava, sumColumns_X2_cacheArray, 0);

    // de-init ('un-pin') all Java arrays from their pinning C pointers
    (*env) -> ReleasePrimitiveArrayCritical(env, sumX_sumX2_cacheArrayJava, sumX_sumX2_cacheArray, 0);

    // de-init ('un-pin') all Java arrays from their pinning C pointers
    (*env) -> ReleasePrimitiveArrayCritical(env, sumX_cacheArrayJava, sumX_cacheArray, 0);

    // de-init ('un-pin') all Java arrays from their pinning C pointers
    (*env) -> ReleasePrimitiveArrayCritical(env, expressionDataArrayJava, expressionDataArray, 0);

    // de-init ('un-pin') all Java arrays from their pinning C pointers
    (*env) -> ReleasePrimitiveArrayCritical(env, dataResultsCPUJava, dataResultsCPU, 0);
}
