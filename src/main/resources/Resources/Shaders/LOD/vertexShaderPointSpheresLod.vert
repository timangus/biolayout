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

 @ author, GLSL & OpenGL code author Thanos Theo, 2012

*/

out  vec3 vsCenter;
out float vsRadius;
out  vec4 vsSceneColor;

// animation related GPU Computing variables
uniform bool AnimationGPUComputingMode;
float animationNodeSizeRatio = 1.0;
vec4 color;

vec4 applyAnimationGPUComputing(in vec4);

void main()
{    
    vsCenter = gl_Vertex.xyz;
    vsRadius = gl_Vertex.w;
    vsSceneColor = gl_Color;

    if (AnimationGPUComputingMode)
    {
        applyAnimationGPUComputing(gl_Color); // no return value needed to be stored here
        vsRadius *= animationNodeSizeRatio;
        vsSceneColor.rgb = color.rgb;
    }

    gl_Position = vec4(0.0, 0.0, 0.0, 1.0);
}