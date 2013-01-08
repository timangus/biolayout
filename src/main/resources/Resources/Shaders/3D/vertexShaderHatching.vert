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

VS_VARYING vec3 VS_POSITION;
VS_VARYING vec3 VS_MC_POSITION;
VS_VARYING vec3 VS_NORMAL;
VS_VARYING vec4 VS_SCENE_COLOR;
VS_VARYING vec2 VS_TEX_COORD;
VS_VARYING float VS_V;

uniform bool hatchingTexturing;
uniform bool hatchingSphericalMapping;
uniform float hatchingMorphing;
uniform bool hatchingUserClipping;
uniform float hatchingTimer;

const float ScaleStandard = 0.015;
const float ScaleSpherical = 0.3;
const float pointSizeFactor = 100.0;

// animation related GPU Computing variables
uniform bool AnimationGPUComputingMode;
float animationNodeSizeRatio = 1.0;
vec4 color;

vec4 applyMorphing(in float, in float);
vec2 applySphericalCoordinates(in vec3, in vec3);
vec4 applyAnimationGPUComputing(in vec4);

void main()
{
    vec4 vertex;
    VS_SCENE_COLOR = gl_Color;
    // apply morphing if appropriate
    if (AnimationGPUComputingMode)
    {
        vertex = applyAnimationGPUComputing(VS_SCENE_COLOR);
        VS_SCENE_COLOR.rgb = color.rgb;
    }
    else if (hatchingMorphing != 0.0)
    {
        vertex = applyMorphing(hatchingMorphing, hatchingTimer);
    }
    else
    {
        vertex = gl_Vertex;
    }
    
    VS_POSITION = vec3(gl_ModelViewMatrix * vertex);
    VS_NORMAL = gl_NormalMatrix * gl_Normal;
    VS_MC_POSITION = vec3(vertex) / (150.0 * animationNodeSizeRatio)  +  ( (hatchingSphericalMapping) ? ScaleSpherical : ScaleStandard ) * vec3(0.0, 0.0, hatchingTimer);

    #if GPU_GEOMETRY_SHADER4_COMPATIBILITY_CONDITION
        gl_Position = vertex;
    #else    
        gl_Position = gl_ModelViewProjectionMatrix * vertex;
    #endif

    if (hatchingUserClipping)
    {
        // used with the glClipPlane() command for user defined clipping planes mimicking OpenGL's fixed functionality
        gl_ClipVertex = gl_ModelViewMatrix * vertex;
    }

    // calculate a distance attenuated point size from the original point size
    gl_PointSize = pointSizeFactor / -VS_POSITION.z;

    // apply texturing if appropriate
    if (hatchingTexturing)
    {
        gl_TexCoord[0] = (hatchingSphericalMapping) ? vec4(applySphericalCoordinates(VS_POSITION, VS_NORMAL), (gl_TextureMatrix[0] * gl_MultiTexCoord0).pq)  : gl_TextureMatrix[0] * gl_MultiTexCoord0;
        VS_V = gl_TexCoord[0].s; // try .s for vertical stripes, .t for horizontal stripes
    }
    else
    {
        // try .s for vertical stripes, .t for horizontal stripes
        VS_V = (hatchingSphericalMapping) ? applySphericalCoordinates(VS_POSITION, VS_NORMAL).s : (gl_TextureMatrix[0] * gl_MultiTexCoord0).s;
    }
}