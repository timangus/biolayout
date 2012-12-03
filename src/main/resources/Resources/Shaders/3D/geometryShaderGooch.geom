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

uniform bool goochTexturing;
uniform float goochMorphing;
uniform bool goochUserClipping;
uniform float goochTimer;
uniform bool goochShrinkTriangles;
uniform bool goochSolidWireFrame;
uniform bool goochNormals;

const float PI = 3.14159;

void applyTriangleGeometry(in bool, in float, in bool, in bool);
void applyNormalsGeometry();

void main() 
{
    bool useShrinkFactor;
    float shrinkFactor;
    if (goochShrinkTriangles)
    {
        useShrinkFactor = true;
        shrinkFactor = 0.9 + 0.05 * abs( sin(goochTimer) );
    }
    else if (goochMorphing != 0.0)
    {
        useShrinkFactor = true;
        shrinkFactor = 0.25 + 0.75 * abs( sin(PI / 2.0 + goochTimer) );
    }
    else
    {
        useShrinkFactor = false;
        shrinkFactor = 0.0;
    }
    applyTriangleGeometry(useShrinkFactor, shrinkFactor, goochUserClipping, goochTexturing);

    if (goochSolidWireFrame && goochNormals)
        applyNormalsGeometry();
}