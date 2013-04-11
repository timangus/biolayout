package org.BioLayoutExpress3D.Utils;

/**
 *
 * @author Tim Angus <tim.angus@roslin.ed.ac.uk>
 */
final public class Complex
{
    private final double real;
    private final double imaginary;

    public Complex()
    {
        this(0.0, 0.0);
    }

    public Complex(double r)
    {
        this(r, 0.0);
    }

    public Complex(double r, double i)
    {
        this.real = r;
        this.imaginary = i;
    }

    public Complex(Complex other)
    {
        this.real = other.real;
        this.imaginary = other.imaginary;
    }

    public Complex add(Complex other)
    {
        return new Complex((this.real + other.real), (this.imaginary + other.imaginary));
    }
}
