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

#extension GL_EXT_geometry_shader4 : enable

in vec3 TS_GS_POSITION[];
in vec3 TS_GS_NORMAL[];
in vec4 TS_GS_SCENE_COLOR[];
in vec2 TS_GS_TEX_COORDS[];
#if APPLY_NORMALS_GEOMETRY_CONDITION
    in vec4 TS_GS_VERTEX_NORMAL_POSITION[];
#endif

out vec3 fsPosition;
out vec3 fsNormal;
out vec4 fsSceneColor;
out vec2 fsTexCoords;
out vec3 fsTriangleDistances;

uniform  bool pointNormalTrianglesLodTexturing;
uniform float pointNormalTrianglesLodTimer;
uniform  bool pointNormalTrianglesLodShrinkTriangles;
uniform bool pointNormalTrianglesLodNormals;

const vec3 ZERO_VECTOR = vec3(0.0);
#if APPLY_NORMALS_GEOMETRY_CONDITION
    const vec4 UNIT_NORMAL_COLOR_RED = vec4(1.0, 0.0, 0.0, 1.0);
#endif

void applyTriangleGeometry(in bool, in float, in bool);
#if APPLY_NORMALS_GEOMETRY_CONDITION
    void applyNormalsGeometry();
#endif

void main() 
{
    bool useShrinkFactor;
    float shrinkFactor;
    if (pointNormalTrianglesLodShrinkTriangles)
    {
        useShrinkFactor = true;
        shrinkFactor = 0.9 + 0.05 * abs( sin(pointNormalTrianglesLodTimer) );
    }
    else
    {
        useShrinkFactor = false;
        shrinkFactor = 0.0;
    }
    applyTriangleGeometry(useShrinkFactor, shrinkFactor, pointNormalTrianglesLodTexturing);

    #if APPLY_NORMALS_GEOMETRY_CONDITION
        if (pointNormalTrianglesLodNormals)
            applyNormalsGeometry();
    #endif
}

void applyTriangleGeometry(in bool useShrinkFactor, in float shrinkFactor, in bool useTexturing)
{
    vec3 triangleCentroid = (useShrinkFactor) ? (gl_PositionIn[0].xyz + gl_PositionIn[1].xyz + gl_PositionIn[2].xyz) / 3.0 : vec3(0.0);
    for (int i = 0; i < gl_VerticesIn; i++)
    {
        fsPosition   = TS_GS_POSITION[i];
        fsNormal     = TS_GS_NORMAL[i];
        fsSceneColor = TS_GS_SCENE_COLOR[i];
        fsTriangleDistances    = ZERO_VECTOR;
        fsTriangleDistances[i] = 1.0;

        gl_Position = gl_ModelViewProjectionMatrix * ( (useShrinkFactor) ? vec4(triangleCentroid + shrinkFactor * (gl_PositionIn[i].xyz - triangleCentroid), gl_PositionIn[i].w)
                                                                         : gl_PositionIn[i] );
        if (useTexturing)
            fsTexCoords = TS_GS_TEX_COORDS[i];
        EmitVertex();
    }
    EndPrimitive(); 
}

#if APPLY_NORMALS_GEOMETRY_CONDITION
    void applyNormalsGeometry()
    {
        vec4 position;
        for (int i = 0; i < gl_VerticesIn; i++)
        {
            fsSceneColor = UNIT_NORMAL_COLOR_RED;

            position = gl_ModelViewProjectionMatrix * TS_GS_VERTEX_NORMAL_POSITION[i];

            for (int j = 0; j < gl_VerticesIn; j++)
            {
                gl_Position = (j == 0) ? gl_ModelViewProjectionMatrix * gl_PositionIn[i] : position;
                EmitVertex();
            }
            EndPrimitive();
        }
    }
#endif