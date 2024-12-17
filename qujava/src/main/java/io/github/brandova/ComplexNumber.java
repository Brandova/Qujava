package io.github.brandova;

public class ComplexNumber {

    private final double Re;      // Real part
    private final double Im;      // Imaginary part

    public ComplexNumber(double Re, double Im) 
    {
        this.Re = Re;
        this.Im = Im;
    }

    public double getRe() 
    {
        return Re;
    }

    public double getIm() 
    {
        return Im;
    }

    public ComplexNumber add(ComplexNumber other) 
    {
        return new ComplexNumber(this.Re + other.Re, this.Im + other.Im);
    }

    public ComplexNumber subtract(ComplexNumber other) 
    {
        return new ComplexNumber(this.Re - other.Re, this.Im - other.Im);
    }

    public ComplexNumber multiply(ComplexNumber other) 
    {
        double RePart = this.Re * other.Re - this.Im * other.Im;
        double ImPart = this.Re * other.Im + this.Im * other.Re;
        return new ComplexNumber(RePart, ImPart);
    }

    public ComplexNumber multiply(double other) 
    {
        double RePart = this.Re * other;
        double ImPart = this.Im * other;
        return new ComplexNumber(RePart, ImPart);
    }

    // Exponentiation
    /*
     * Using De Moivre's theorem
     * 
     * 1. conv to polar: 
     *    z = r * (cos(t) + isin(t))
     *    t = atan(Im / Re)
     *    r = magnitude
     * 2. exponentiate: 
     *    z^n = r^n * (cos(nt) + isin(nt))
     * 3. conv to cartesian:
     *    Re = r^n * cos(nt)
     *    Im = r^n * sin(nt)
     */
    public ComplexNumber pow(int n)
    {
        double t = Math.atan2(Im, Re);
        double r = magnitude();
        t *= n;
        r = Math.pow(r, n);
        return new ComplexNumber(r * Math.cos(t), r * Math.sin(t));
    }

    public ComplexNumber divide(ComplexNumber other) {
        double denominator = other.Re * other.Re + other.Im * other.Im;
        if (denominator == 0) 
            throw new ArithmeticException("Division by zero");
        
        double RePart = (this.Re * other.Re + this.Im * other.Im) / denominator;
        double ImPart = (this.Im * other.Re - this.Re * other.Im) / denominator;
        return new ComplexNumber(RePart, ImPart);
    }

    // Magnitude |a + bi| = sqrt(a^2 + b^2)
    public double magnitude() 
    {
        return Math.sqrt(Re * Re + Im * Im);
    }

    // Conjugate (a + bi) -> (a - bi)
    public ComplexNumber conjugate() 
    {
        return new ComplexNumber(Re, -Im);
    }

    public boolean isEmpty()
    {
        return (Re == 0 && Im == 0);
    }

    @Override
    public String toString() 
    {
        return toString(3);
    }
    
    // Custom precision
    public String toString(int p) 
    { 
        if (Im >= 0) 
            return String.format("%."+p+"f + %."+p+"fi", Re, Im);
        else 
            return String.format("%."+p+"f - %."+p+"fi", Re, Im);
    }
}

