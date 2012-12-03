/*

 BioLayoutExpress3D - A tool for visualisation
 and analysis of biological networks

 Copyright (c) 2006-2012 Genome Research Ltd.
 Authors: Thanos Theo, Anton Enright, Leon Goldovsky, Ildefonso Cases, Markus Brosch, Stijn van Dongen, Michael Kargas, Benjamin Boyer and Tom Freeman
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

 @ author, GLSL & OpenGL code author Thanos Theo, 2009-2010-2011-2012

*/

in vec3 fsPosition;
in vec3 fsNormal;
in vec4 fsSceneColor;
in vec2 fsTexCoords;

uniform bool phongTessellationLodFog;
uniform bool phongTessellationLodTexturing;
uniform bool phongTessellationLodState;
uniform bool phongTessellationLodOldLCDStyleTransparency;
uniform bool phongTessellationLodSolidWireFrame;

const float intensityLevel = 0.5;
const float intensityTransparencyLevel = 1.8 * intensityLevel;

// animation related GPU Computing variables
uniform bool AnimationGPUComputingMode;
uniform bool ANIMATION_USE_COLOR_PALETTE_SPECTRUM_TRANSITION;

void applyOldStyleTransparency();
vec4 applyAnimationGPUComputing(in vec4);
vec4 applyADSLightingModel(in bool, in bool, in vec3, in vec3, in vec4);
void applyTexture(inout vec4, in vec2);
#if GPU_GEOMETRY_SHADER4_COMPATIBILITY_CONDITION
    vec4 applySolidWireFrame(in vec4, in float);
#endif
vec4 applyFog(in vec4);

void main()
{
    if (phongTessellationLodOldLCDStyleTransparency)
        applyOldStyleTransparency();

    vec4 sceneColorLocal = (AnimationGPUComputingMode && ANIMATION_USE_COLOR_PALETTE_SPECTRUM_TRANSITION) ? applyAnimationGPUComputing(fsSceneColor) : fsSceneColor;
    float alpha = sceneColorLocal.a;
    sceneColorLocal.rgb *= intensityLevel;
    vec4 finalColor = applyADSLightingModel(phongTessellationLodState, true, fsNormal, fsPosition, sceneColorLocal);    
    if (alpha < 1.0)
        finalColor.a *= (alpha / intensityTransparencyLevel);

    // apply texturing if appropriate
    if (phongTessellationLodTexturing)
        applyTexture( finalColor, vec2(1.0 - fsTexCoords.x, fsTexCoords.y) );    
   
    #if GPU_GEOMETRY_SHADER4_COMPATIBILITY_CONDITION    
        if (phongTessellationLodSolidWireFrame)
            finalColor = applySolidWireFrame(finalColor, 1.5);    
    #endif 
    // apply per-pixel fog if appriopriate
    gl_FragColor = (phongTessellationLodFog) ? applyFog(finalColor) : finalColor;
}