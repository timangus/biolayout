package org.biolayout.Textures;

import static java.lang.Math.*;

/**
*
*  This class includes the improved Simplex Perlin Noise static methods. Copyright 2002 Ken Perlin.
*  Java reference implementation of Simplex Noise, Stefan Gustavson, 2005.
*
* @author Ken Perlin, 2002, Stefan Gustavson, 2005
* @version 3.0.0.0
*
*/

final class SimplexNoise // package access
{

    /**
    *  Standard permutation order.
    */
    private static final int P[] = { 151,160,137,91,90,15,
    131,13,201,95,96,53,194,233,7,225,140,36,103,30,69,142,8,99,37,240,21,10,23,
    190, 6,148,247,120,234,75,0,26,197,62,94,252,219,203,117,35,11,32,57,177,33,
    88,237,149,56,87,174,20,125,136,171,168, 68,175,74,165,71,134,139,48,27,166,
    77,146,158,231,83,111,229,122,60,211,133,230,220,105,92,41,55,46,245,40,244,
    102,143,54, 65,25,63,161, 1,216,80,73,209,76,132,187,208, 89,18,169,200,196,
    135,130,116,188,159,86,164,100,109,198,173,186, 3,64,52,217,226,250,124,123,
    5,202,38,147,118,126,255,82,85,212,207,206,59,227,47,16,58,17,182,189,28,42,
    223,183,170,213,119,248,152, 2,44,154,163, 70,221,153,101,155,167, 43,172,9,
    129,22,39,253, 19,98,108,110,79,113,224,232,178,185, 112,104,218,246,97,228,
    251,34,242,193,238,210,144,12,191,179,162,241, 81,51,145,235,249,14,239,107,
    49,192,214, 31,181,199,106,157,184, 84,204,176,115,121,50,45,127, 4,150,254,
    138,236,205,93,222,114,67,29,24,72,243,141,128,195,78,66,215,61,156,180 };

    /**
    *  To remove the need for index wrapping, double the permutation table length.
    */
    private static final int PERM[] = new int[512];

    /**
    *  Gradient 3D array.
    */
    private static final int GRAD3[][] = {
                                           {1,1,0}, {-1,1,0}, {1,-1,0}, {-1,-1,0},
                                           {1,0,1}, {-1,0,1}, {1,0,-1}, {-1,0,-1},
                                           {0,1,1}, {0,-1,1}, {0,1,-1}, {0,-1,-1}
                                         };

    /**
    *  Gradient 4D array.
    */
    private static final int GRAD4[][]= {
                                          {0,1,1,1},  {0,1,1,-1},  {0,1,-1,1},  {0,1,-1,-1},
                                          {0,-1,1,1}, {0,-1,1,-1}, {0,-1,-1,1}, {0,-1,-1,-1},
                                          {1,0,1,1},  {1,0,1,-1},  {1,0,-1,1},  {1,0,-1,-1},
                                          {-1,0,1,1}, {-1,0,1,-1}, {-1,0,-1,1}, {-1,0,-1,-1},
                                          {1,1,0,1},  {1,1,0,-1},  {1,-1,0,1},  {1,-1,0,-1},
                                          {-1,1,0,1}, {-1,1,0,-1}, {-1,-1,0,1}, {-1,-1,0,-1},
                                          {1,1,1,0},  {1,1,-1,0},  {1,-1,1,0},  {1,-1,-1,0},
                                          {-1,1,1,0}, {-1,1,-1,0}, {-1,-1,1,0}, {-1,-1,-1,0}
                                        };

    /**
    *  A lookup table to traverse the SIMPLEX around a given point in 4D.
    *  Details can be found where this table is used, in the 4D Perlin Noise method.
    */
    private static final int SIMPLEX[][] = {
                                            {0,1,2,3}, {0,1,3,2}, {0,0,0,0}, {0,2,3,1}, {0,0,0,0}, {0,0,0,0}, {0,0,0,0}, {1,2,3,0},
                                            {0,2,1,3}, {0,0,0,0}, {0,3,1,2}, {0,3,2,1}, {0,0,0,0}, {0,0,0,0}, {0,0,0,0}, {1,3,2,0},
                                            {0,0,0,0}, {0,0,0,0}, {0,0,0,0}, {0,0,0,0}, {0,0,0,0}, {0,0,0,0}, {0,0,0,0}, {0,0,0,0},
                                            {1,2,0,3}, {0,0,0,0}, {1,3,0,2}, {0,0,0,0}, {0,0,0,0}, {0,0,0,0}, {2,3,0,1}, {2,3,1,0},
                                            {1,0,2,3}, {1,0,3,2}, {0,0,0,0}, {0,0,0,0}, {0,0,0,0}, {2,0,3,1}, {0,0,0,0}, {2,1,3,0},
                                            {0,0,0,0}, {0,0,0,0}, {0,0,0,0}, {0,0,0,0}, {0,0,0,0}, {0,0,0,0}, {0,0,0,0}, {0,0,0,0},
                                            {2,0,1,3}, {0,0,0,0}, {0,0,0,0}, {0,0,0,0}, {3,0,1,2}, {3,0,2,1}, {0,0,0,0}, {3,1,2,0},
                                            {2,1,0,3}, {0,0,0,0}, {0,0,0,0}, {0,0,0,0}, {3,1,0,2}, {0,0,0,0}, {3,2,0,1}, {3,2,1,0}
                                            };

    static
    {
        for (int i = 0; i < 512; i++)
            PERM[i] = P[i & 255];
    }

    /**
    *  This method is a *lot* faster than using (int)Math.floor(x).
    */
    private static int fastfloor(double x)
    {
        return ( (x > 0) ? (int)x : (int)x - 1 );
    }

    /**
    *  Dot product method for 2D.
    */
    private static double dot(int g[], double x, double y)
    {
        return g[0] * x + g[1] * y;
    }

    /**
    *  Dot product method for 3D.
    */
    private static double dot(int g[], double x, double y, double z)
    {
        return g[0] * x + g[1] * y + g[2] * z;
    }

    /**
    *  Dot product method for 4D.
    */
    private static double dot(int g[], double x, double y, double z, double w)
    {
        return g[0] * x + g[1] * y + g[2] * z + g[3] * w;
    }

    /**
    *  2D Simplex Perlin Noise.
    */
    public static double perlinNoise2(double xIn, double yIn)
    {
        // Noise contributions from the three corners
        double n0 = 0.0, n1 = 0.0, n2 = 0.0;

        // Skew the input space to determine which SIMPLEX cell we're in
        final double F2 = 0.5 * (sqrt(3.0) - 1.0);

        // Hairy factor for 2D
        double s = (xIn + yIn) * F2;
        int i = fastfloor(xIn + s);
        int j = fastfloor(yIn + s);
        final double G2 = ( 3.0 - sqrt(3.0) ) / 6.0;
        double t = (i + j) * G2;

        // Unskew the cell origin back to (x,y) space
        double X0 = i - t;
        double Y0 = j - t;
        // The x,y distances from the cell origin
        double x0 = xIn - X0;
        double y0 = yIn - Y0;

        // For the 2D case, the SIMPLEX shape is an equilateral triangle.
        // Determine which SIMPLEX we are in.
        int i1 = 0, j1 = 0; // Offsets for second (middle) corner of SIMPLEX in (i,j) coords
        if (x0 > y0)
        {
            i1 = 1;
            j1 = 0;
        } // lower triangle, XY order: (0,0)->(1,0)->(1,1)
        else
        {
            i1 = 0;
            j1 = 1;
        } // upper triangle, YX order: (0,0)->(0,1)->(1,1)

        // A step of (1,0) in (i,j) means a step of (1-c,-c) in (x,y), and
        // a step of (0,1) in (i,j) means a step of (-c,1-c) in (x,y), where
        // c = ( 3 - sqrt(3) ) / 6
        double x1 = x0 - i1 + G2; // Offsets for middle corner in (x,y) unskewed coords
        double y1 = y0 - j1 + G2;
        double x2 = x0 - 1.0 + 2.0 * G2; // Offsets for last corner in (x,y) unskewed coords
        double y2 = y0 - 1.0 + 2.0 * G2;

        // Work out the hashed gradient indices of the three SIMPLEX corners
        int ii = i & 255;
        int jj = j & 255;
        int gi0 = PERM[ii + PERM[jj]] % 12;
        int gi1 = PERM[ii + i1 + PERM[jj + j1]] % 12;
        int gi2 = PERM[ii + 1 + PERM[jj + 1]] % 12;

        // Calculate the contribution from the three corners
        double t0 = 0.5 - x0 * x0 - y0 * y0;
        if (t0 < 0)
        {
            n0 = 0.0;
        }
        else
        {
            t0 *= t0;
            n0 = t0 * t0 * dot(GRAD3[gi0], x0, y0); // (x,y) of GRAD3 used for 2D gradient
        }

        double t1 = 0.5 - x1 * x1 - y1 * y1;
        if (t1 < 0)
        {
            n1 = 0.0;
        }
        else
        {
            t1 *= t1;
            n1 = t1 * t1 * dot(GRAD3[gi1], x1, y1);
        }

        double t2 = 0.5 - x2 * x2 - y2 * y2;
        if (t2 < 0)
        {
            n2 = 0.0;
        }
        else
        {
            t2 *= t2;
            n2 = t2 * t2 * dot(GRAD3[gi2], x2, y2);
        }

        // Add contributions from each corner to get the final Perlin Noise value.
        // The result is scaled to return values in the interval [-1,1].
        return 70.0 * (n0 + n1 + n2);
    }

    /**
    *  3D Simplex Perlin Noise.
    */
    public static double perlinNoise3(double xIn, double yIn, double zIn)
    {
        // Noise contributions from the four corners
        double n0 = 0.0, n1 = 0.0, n2 = 0.0, n3 = 0.0;

        // Skew the input space to determine which SIMPLEX cell we're in
        final double F3 = 1.0 / 3.0;

        // Very nice and simple skew factor for 3D
        double s = (xIn + yIn + zIn) * F3;
        int i = fastfloor(xIn + s);
        int j = fastfloor(yIn + s);
        int k = fastfloor(zIn + s);

        // Very nice and simple unskew factor, too
        final double G3 = 1.0 / 6.0;

        double t = (i + j + k) * G3;
        // Unskew the cell origin back to (x,y,z) space
        double X0 = i - t;
        double Y0 = j - t;
        double Z0 = k - t;
        // The x,y,z distances from the cell origin
        double x0 = xIn - X0;
        double y0 = yIn - Y0;
        double z0 = zIn - Z0;

        // For the 3D case, the SIMPLEX shape is a slightly irregular tetrahedron.
        // Determine which SIMPLEX we are in.
        // Offsets for second corner of SIMPLEX in (i,j,k) coords
        int i1 = 0, j1 = 0, k1 = 0;
        // Offsets for third corner of SIMPLEX in (i,j,k) coords
        int i2 = 0, j2 = 0, k2 = 0;
        if (x0 >= y0)
        {
            if (y0 >= z0)
            {
                i1 = 1;
                j1 = 0;
                k1 = 0;
                i2 = 1;
                j2 = 1;
                k2 = 0;
            } // X Y Z order
            else if (x0 >= z0)
            {
                i1 = 1;
                j1 = 0;
                k1 = 0;
                i2 = 1;
                j2 = 0;
                k2 = 1;
            } // X Z Y order
            else
            {
                i1 = 0;
                j1 = 0;
                k1 = 1;
                i2 = 1;
                j2 = 0;
                k2 = 1;
            } // Z X Y order
        }
        else
        { // x0<y0
            if (y0 < z0)
            {
                i1 = 0;
                j1 = 0;
                k1 = 1;
                i2 = 0;
                j2 = 1;
                k2 = 1;
            } // Z Y X order
            else if (x0 < z0)
            {
                i1 = 0;
                j1 = 1;
                k1 = 0;
                i2 = 0;
                j2 = 1;
                k2 = 1;
            } // Y Z X order
            else
            {
                i1 = 0;
                j1 = 1;
                k1 = 0;
                i2 = 1;
                j2 = 1;
                k2 = 0;
            } // Y X Z order
        }

        // A step of (1,0,0) in (i,j,k) means a step of (1-c,-c,-c) in (x,y,z),
        // a step of (0,1,0) in (i,j,k) means a step of (-c,1-c,-c) in (x,y,z), and
        // a step of (0,0,1) in (i,j,k) means a step of (-c,-c,1-c) in (x,y,z), where
        // c = 1 / 6.
        double x1 = x0 - i1 + G3; // Offsets for second corner in (x,y,z) coords
        double y1 = y0 - j1 + G3;
        double z1 = z0 - k1 + G3;
        double x2 = x0 - i2 + 2.0 * G3; // Offsets for third corner in (x,y,z) coords
        double y2 = y0 - j2 + 2.0 * G3;
        double z2 = z0 - k2 + 2.0 * G3;
        double x3 = x0 - 1.0 + 3.0 * G3; // Offsets for last corner in (x,y,z) coords
        double y3 = y0 - 1.0 + 3.0 * G3;
        double z3 = z0 - 1.0 + 3.0 * G3;

        // Work out the hashed gradient indices of the four SIMPLEX corners
        int ii = i & 255;
        int jj = j & 255;
        int kk = k & 255;
        int gi0 = PERM[ii + PERM[jj + PERM[kk]]] % 12;
        int gi1 = PERM[ii + i1 + PERM[jj + j1 + PERM[kk + k1]]] % 12;
        int gi2 = PERM[ii + i2 + PERM[jj + j2 + PERM[kk + k2]]] % 12;
        int gi3 = PERM[ii + 1 + PERM[jj + 1 + PERM[kk + 1]]] % 12;

        // Calculate the contribution from the four corners
        double t0 = 0.6 - x0 * x0 - y0 * y0 - z0 * z0;
        if (t0 < 0)
        {
            n0 = 0.0;
        }
        else
        {
            t0 *= t0;
            n0 = t0 * t0 * dot(GRAD3[gi0], x0, y0, z0);
        }

        double t1 = 0.6 - x1 * x1 - y1 * y1 - z1 * z1;
        if (t1 < 0)
        {
            n1 = 0.0;
        }
        else
        {
            t1 *= t1;
            n1 = t1 * t1 * dot(GRAD3[gi1], x1, y1, z1);
        }

        double t2 = 0.6 - x2 * x2 - y2 * y2 - z2 * z2;
        if (t2 < 0)
        {
            n2 = 0.0;
        }
        else
        {
            t2 *= t2;
            n2 = t2 * t2 * dot(GRAD3[gi2], x2, y2, z2);
        }

        double t3 = 0.6 - x3 * x3 - y3 * y3 - z3 * z3;
        if (t3 < 0)
        {
            n3 = 0.0;
        }
        else
        {
            t3 *= t3;
            n3 = t3 * t3 * dot(GRAD3[gi3], x3, y3, z3);
        }

        // Add contributions from each corner to get the final Perlin Noise value.
        // The result is scaled to stay just inside [-1,1]
        return 32.0 * (n0 + n1 + n2 + n3);
    }

    /**
    *  4D Simplex Perlin Noise.
    */
    public static double perlinNoise4(double xIn, double yIn, double zIn, double wIn)
    {
        // The skewing and unskewing factors are hairy again for the 4D case
        final double F4 = (sqrt(5.0) - 1.0) / 4.0;
        final double G4 = ( 5.0 - sqrt(5.0) ) / 20.0;

        // Noise contributions from the five corners
        double n0 = 0.0, n1 = 0.0, n2 = 0.0, n3 = 0.0, n4 = 0.0;

        // Skew the (xIn,yIn,zIn,wIn) space to determine which cell of 24 simplices we're in
        // Factor for 4D skewing
        double s = (xIn + yIn + zIn + wIn) * F4;
        int i = fastfloor(xIn + s);
        int j = fastfloor(yIn + s);
        int k = fastfloor(zIn + s);
        int l = fastfloor(wIn + s);

        // Factor for 4D unskewing
        double t = (i + j + k + l) * G4;

        // Unskew the cell origin back to (x,y,z,w) space
        double X0 = i - t;
        double Y0 = j - t;
        double Z0 = k - t;
        double W0 = l - t;

        // The x,y,z,w distances from the cell origin
        double x0 = xIn - X0;
        double y0 = yIn - Y0;
        double z0 = zIn - Z0;
        double w0 = wIn - W0;

        // For the 4D case, the SIMPLEX is a 4D shape I won't even try to describe.
        // To find out which of the 24 possible simplices we're in, we need to
        // determine the magnitude ordering of x0, y0, z0 and w0.
        // The method below is a good way of finding the ordering of x,y,z,w and
        // then find the correct traversal order for the SIMPLEX we’re in.
        // First, six pair-wise comparisons are performed between each possible pair
        // of the four coordinates, and the results are used to add up binary bits
        // for an integer index.
        int c1 = (x0 > y0) ? 32 : 0;
        int c2 = (x0 > z0) ? 16 : 0;
        int c3 = (y0 > z0) ? 8 : 0;
        int c4 = (x0 > w0) ? 4 : 0;
        int c5 = (y0 > w0) ? 2 : 0;
        int c6 = (z0 > w0) ? 1 : 0;
        int c = c1 + c2 + c3 + c4 + c5 + c6;
        int i1 = 0, j1 = 0, k1 = 0, l1 = 0; // The integer offsets for the second SIMPLEX corner
        int i2 = 0, j2 = 0, k2 = 0, l2 = 0; // The integer offsets for the third SIMPLEX corner
        int i3 = 0, j3 = 0, k3 = 0, l3 = 0; // The integer offsets for the fourth SIMPLEX corner

        // SIMPLEX[c] is a 4-vector with the numbers 0, 1, 2 and 3 in some order.
        // Many values of c will never occur, since e.g. x>y>z>w makes x<z, y<w and x<w
        // impossible. Only the 24 indices which have non-zero entries make any sense.
        // We use a thresholding to set the coordinates in turn from the largest magnitude.

        // The number 3 in the "SIMPLEX" array is at the position of the largest coordinate.
        i1 = (SIMPLEX[c][0] >= 3) ? 1 : 0;
        j1 = (SIMPLEX[c][1] >= 3) ? 1 : 0;
        k1 = (SIMPLEX[c][2] >= 3) ? 1 : 0;
        l1 = (SIMPLEX[c][3] >= 3) ? 1 : 0;

        // The number 2 in the "SIMPLEX" array is at the second largest coordinate.
        i2 = (SIMPLEX[c][0] >= 2) ? 1 : 0;
        j2 = (SIMPLEX[c][1] >= 2) ? 1 : 0;
        k2 = (SIMPLEX[c][2] >= 2) ? 1 : 0;
        l2 = (SIMPLEX[c][3] >= 2) ? 1 : 0;

        // The number 1 in the "SIMPLEX" array is at the second smallest coordinate.
        i3 = (SIMPLEX[c][0] >= 1) ? 1 : 0;
        j3 = (SIMPLEX[c][1] >= 1) ? 1 : 0;
        k3 = (SIMPLEX[c][2] >= 1) ? 1 : 0;
        l3 = (SIMPLEX[c][3] >= 1) ? 1 : 0;

        // The fifth corner has all coordinate offsets = 1, so no need to look that up.
        double x1 = x0 - i1 + G4; // Offsets for second corner in (x,y,z,w) coords
        double y1 = y0 - j1 + G4;
        double z1 = z0 - k1 + G4;
        double w1 = w0 - l1 + G4;
        double x2 = x0 - i2 + 2.0 * G4; // Offsets for third corner in (x,y,z,w) coords
        double y2 = y0 - j2 + 2.0 * G4;
        double z2 = z0 - k2 + 2.0 * G4;
        double w2 = w0 - l2 + 2.0 * G4;
        double x3 = x0 - i3 + 3.0 * G4; // Offsets for fourth corner in (x,y,z,w) coords
        double y3 = y0 - j3 + 3.0 * G4;
        double z3 = z0 - k3 + 3.0 * G4;
        double w3 = w0 - l3 + 3.0 * G4;
        double x4 = x0 - 1.0 + 4.0 * G4; // Offsets for last corner in (x,y,z,w) coords
        double y4 = y0 - 1.0 + 4.0 * G4;
        double z4 = z0 - 1.0 + 4.0 * G4;
        double w4 = w0 - 1.0 + 4.0 * G4;

        // Work out the hashed gradient indices of the five SIMPLEX corners
        int ii = i & 255;
        int jj = j & 255;
        int kk = k & 255;
        int ll = l & 255;
        int gi0 = PERM[ii + PERM[jj + PERM[kk + PERM[ll]]]] % 32;
        int gi1 = PERM[ii + i1 + PERM[jj + j1 + PERM[kk + k1 + PERM[ll + l1]]]] % 32;
        int gi2 = PERM[ii + i2 + PERM[jj + j2 + PERM[kk + k2 + PERM[ll + l2]]]] % 32;
        int gi3 = PERM[ii + i3 + PERM[jj + j3 + PERM[kk + k3 + PERM[ll + l3]]]] % 32;
        int gi4 = PERM[ii + 1 + PERM[jj + 1 + PERM[kk + 1 + PERM[ll + 1]]]] % 32;

        // Calculate the contribution from the five corners
        double t0 = 0.6 - x0 * x0 - y0 * y0 - z0 * z0 - w0 * w0;
        if (t0 < 0)
        {
            n0 = 0.0;
        }
        else
        {
            t0 *= t0;
            n0 = t0 * t0 * dot(GRAD4[gi0], x0, y0, z0, w0);
        }

        double t1 = 0.6 - x1 * x1 - y1 * y1 - z1 * z1 - w1 * w1;
        if (t1 < 0)
        {
            n1 = 0.0;
        }
        else
        {
            t1 *= t1;
            n1 = t1 * t1 * dot(GRAD4[gi1], x1, y1, z1, w1);
        }

        double t2 = 0.6 - x2 * x2 - y2 * y2 - z2 * z2 - w2 * w2;
        if (t2 < 0)
        {
            n2 = 0.0;
        }
        else
        {
            t2 *= t2;
            n2 = t2 * t2 * dot(GRAD4[gi2], x2, y2, z2, w2);
        }

        double t3 = 0.6 - x3 * x3 - y3 * y3 - z3 * z3 - w3 * w3;
        if (t3 < 0)
        {
            n3 = 0.0;
        }
        else
        {
            t3 *= t3;
            n3 = t3 * t3 * dot(GRAD4[gi3], x3, y3, z3, w3);
        }

        double t4 = 0.6 - x4 * x4 - y4 * y4 - z4 * z4 - w4 * w4;
        if (t4 < 0)
        {
            n4 = 0.0;
        }
        else
        {
            t4 *= t4;
            n4 = t4 * t4 * dot(GRAD4[gi4], x4, y4, z4, w4);
        }

        // Sum up and scale the result to cover the range [-1,1]
        return 27.0 * (n0 + n1 + n2 + n3 + n4);
    }


}