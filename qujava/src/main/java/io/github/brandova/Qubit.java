package io.github.brandova;

public class Qubit 
{
    // --------------------------------
    // SECTION A : Qubit Implementation
    // --------------------------------

    protected final ComplexNumber u;  // probability amplitude of 0
    protected final ComplexNumber v;  // probability amplitude of 1

    public Qubit()
    {
        u = new ComplexNumber(1, 0);
        v = new ComplexNumber(0, 0);
    }

    public Qubit(ComplexNumber u, ComplexNumber v)
    {
        this.u = u;
        this.v = v;
    }

    public Qubit(double uRe, double uIm, double vRe, double vIm)
    {
        u = new ComplexNumber(uRe, uIm);
        v = new ComplexNumber(vRe, vIm);
    }

    // collapses the state of the Qubit based on the probability amplitudes
    // returns true if the measured state is 1 and false if 0
    public boolean measure()
    {
        double ampU = Math.pow(u.magnitude(), 2);
        return Math.random() > ampU;
    }

    // collapses the state of the Qubit based on the probability amplitudes, but keep in Qubit form
    public Qubit collapse()
    {
        if (measure()) return new Qubit(1, 0, 0, 0);
        else return new Qubit(0, 0, 1, 0);
    }

    public double getWeight()
    {
        double alpha = u.magnitude(); 
        double beta = v.magnitude();
        alpha *= alpha;
        beta *= beta;
        return alpha+beta;
    }

    public ComplexNumber getU()
    {
        return u;
    }

    public ComplexNumber getV()
    {
        return v;
    }

    // Multiplication
    public Qubit multiply(ComplexNumber other) 
    {
        return new Qubit(u.multiply(other), v.multiply(other));
    }

    public Qubit multiply(double other) 
    {
        return new Qubit(u.multiply(other), v.multiply(other));
    }

    // u and v must be constrained by the second axiom of probability theory
    // the sum of their square magnitudes must be equal to 1
    // in other words, the sum of each Real and Imaginary component squared must equal 1
    public boolean isUnit()
    {
        return Math.abs(getWeight() - 1.0) < 1e-9;
    }

    // converts u and v such that they combine to a unit vector
    public Qubit makeUnit()
    {
        // find current qubit magnitude
        double newWeight = getWeight();

        // no need to make a new qubit, this is already unit in magnitude
        if (Math.abs(newWeight - 1.0) < 1e-9)
        return this;

        // Normalize the qubit: scale each component by 1 / sqrt(newWeight)
        final double normalizationFactor = 1 / Math.sqrt(newWeight);

        // Return a new Qubit with normalized coefficients
        return new Qubit(u.multiply(normalizationFactor), v.multiply(normalizationFactor));
    }

    // defined similar to this > other
    // in other words, if this instance of Qubit is most likely to measure to be 1, return true
    public boolean compare(Qubit other)
    {
        return v.magnitude() > other.v.magnitude();
    }

    public Qubit not()
    {
        return new Qubit(v, u);
    }

    // returns true if this Qubit is equivalent to |1>. otherwise, returns false
    public boolean isOne()
    {
        return this.getV().getRe() == 1;
    }

    // returns true if this Qubit is equivalent to |0>. otherwise, returns false
    public boolean isZero()
    {
        return this.getU().getRe() == 1;
    }

    // --------------------------
    // SECTION B : String Methods
    // --------------------------

    @Override
    public String toString() 
    {
        return toString(3);
    }
    
    // Custom precision
    public String toString(int p) 
    {
        return "(" + u.toString(p)+ ") |0> + (" + v.toString(p) + ") |1>";
    }

    // Represent qubits individually
    public static String qubitsToString(Qubit[] input) 
    {
        return qubitsToString(input, 3);
    }
    
    // Custom precision
    public static String qubitsToString(Qubit[] input, int p) 
    {
        String inputTable = "";

        for (int i = 0 ; i<input.length ; i++)
        {
            if (i % 2 == 1)
            {
                inputTable += " ||| " + input[i].toString(p) + "\n";
                inputTable += "--------------------------------------------------------------------------------------------\n";
            }
            else 
            inputTable += input[i].toString(p); 
        }

        if (input.length % 2 == 1)
        inputTable += "\n--------------------------------------------------------------------------------------------\n";

        return inputTable;
    }

    // Measure and show as binary representation
    public static String qubitsToBinaryString(Qubit[] input)
    {
        boolean[] binary = QubitGates.measure(input);

        String rep = "";
        for (int i = 0 ; i<input.length ; i++)
        {
            if (binary[i]) rep += "1";
            else rep += "0";
        }
        return rep;
    }

    // --------------------------
    // SECTION C : Bit Generators
    // --------------------------

    public static Qubit[] generateDefaultQubits(int numQubits)
    {
        Qubit[] bits = new Qubit[numQubits];
        for (int i = 0 ; i < numQubits ; i++)
        {
            bits[i] = new Qubit();
        }
        return bits;
    }

    // for testing
    public static Qubit[] generateRandomQubits(int numQubits) 
    {
        Qubit[] bits = new Qubit[numQubits];
        for (int i = 0; i < numQubits; i++) 
        {
            // Generate random real and imaginary parts for the u and v complex numbers
            double realU = Math.random() * 2 - 1;  // Random between -1 and 1
            double imagU = Math.random() * 2 - 1;  // Random between -1 and 1
            double realV = Math.random() * 2 - 1;  // Random between -1 and 1
            double imagV = Math.random() * 2 - 1;  // Random between -1 and 1
            
            // Create complex numbers for u and v
            ComplexNumber u = new ComplexNumber(realU, imagU);
            ComplexNumber v = new ComplexNumber(realV, imagV);
    
            // Create the qubit and normalize it
            Qubit rand = new Qubit(u, v);
            bits[i] = rand.makeUnit();  // Ensure the qubit is normalized
        }
        return bits;
    }

    // -------------------------------------
    // SECTION D : Binary Conversion Methods
    // -------------------------------------

    public static int qubitsToInt(Qubit[] register)
    {
        int result = 0;
        for (int i = 0 ; i<register.length ; i++)
        {
            if (register[i].isOne())
                result += Math.pow(2, i);
        }
        return result;
    }
}
