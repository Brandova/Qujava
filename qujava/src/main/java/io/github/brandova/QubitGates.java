package io.github.brandova;

/*
 * Gates will always choose the first index in input as the output if there is a singular output
 * they always return all of the input because some gates will measure the qubits, collapsing their states
 */
public class QubitGates 
{
    // ------------------------------------------------------
    // SECTION A : Gates that change Probability Amplitude(s)
    // ------------------------------------------------------

    /*
     * Swaps u and v of each Qubit
     * |0> becomes |1> and vice versa
     */
    public static Qubit[] not(Qubit[] bits) 
    {
        for (int i = 0; i < bits.length; i++) 
        {
            bits[i] = bits[i].not();  
        }
        return bits;  
    }

    /*
     * pauli-Y gate is defined with this matrix multiplication
     * | 0  -i | | u |
     * | i   0 | | v |
     * 
     * result: 
     * | -i * v |
     * |  i * u |
     */
    public static Qubit[] y(Qubit[] bits)
    {
        final ComplexNumber positivei = new ComplexNumber(0, 1);
        final ComplexNumber negativei = new ComplexNumber(0, -1);

        for (int i = 0; i < bits.length; i++) 
        {
            bits[i] = new Qubit(negativei.multiply(bits[i].getV()), positivei.multiply(bits[i].getU()));
        }
        return bits; 
    }

    /*
     * pauli-Z gate is defined with this matrix multiplication
     * | 1   0 | | u |
     * | 0  -1 | | v |
     * 
     * result: 
     * |  u  |
     * | -v  |
     */
    public static Qubit[] z(Qubit[] bits)
    {
        for (int i = 0; i < bits.length; i++) 
        {
            bits[i] = new Qubit(bits[i].getU(), bits[i].getV().multiply(-1.0));
        }
        return bits; 
    }

    /*
     * hadamard gate is defined with this matrix multiplication
     * (1 / sqrt(2)) *  | 1  1 | | u |
     *                  | 1 -1 | | v |
     * result: 
     * (1 / sqrt(2)) *  | u + v |
     *                  | u - v | 
     */
    public static Qubit[] hadamard(Qubit[] bits)
    {
        final double invroot2 = 1 / Math.sqrt(2);

        for (int i = 0 ; i<bits.length ; i++)
        {
            ComplexNumber top    = bits[i].getU().add(bits[i].getV());
            ComplexNumber bottom = bits[i].getU().subtract(bits[i].getV());
            
            top    = top.multiply(invroot2);
            bottom = bottom.multiply(invroot2);

            bits[i] = new Qubit(top, bottom);
        }

        return bits;
    }
    
    /*
     * multiplies v by e^(i * (phase angle))
     * this corresponds to a rotation about the z-axis in the bloch sphere
     * a phase shift does not effect the measurement of the qubit
     */
    public static Qubit[] phaseShift(Qubit[] bits, double phi)
    {
        final ComplexNumber shiftingFactor = new ComplexNumber(Math.cos(phi), Math.sin(phi));

        for (int i = 0 ; i<bits.length ; i++)
        {
            ComplexNumber shiftv = bits[i].getV().multiply(shiftingFactor);
            bits[i] = new Qubit(bits[i].getU(), shiftv);
        }
        return bits;
    }
    
    // ---------------------------------------------------------
    // SECTION B : Gates that Measure Qubits. Kept in Qubit form
    // ---------------------------------------------------------

    // TODO change gate behavior to allow choosing a result register
    /*
     * Measure the Qubits, converting them to classical bits
     */
    public static boolean[] measure(Qubit[] bits)
    {
        boolean[] result = new boolean[bits.length];

        for (int i = 0 ; i<bits.length ; i++)
        {
            result[i] = bits[i].measure();
        }
        return result;
    }

    /*
     * Measure, but keep in Qubit form, collapsing the state
     */
    public static Qubit[] collapse(Qubit[] bits)
    {
        for (int i = 0 ; i<bits.length ; i++)
        {
            bits[i] = bits[i].collapse();
        }
        return bits;
    }

    /*
     * Measure each chosen Qubit. If any are false (aka zero), return false.
     */
    public static Qubit[] and(Qubit[] bits)
    {
        // since the Qubits are collapsed, they will have no floating-point error
        bits = collapse(bits);

        for (int i = 0 ; i<bits.length ; i++)
        {
            if (bits[i].isZero()) // checks if any are |0>
            {
                bits[0] = new Qubit(1, 0, 0, 0); // returns |0>
                return bits;
            }
        }
        bits[0] = new Qubit(0, 0, 1, 0); // returns |1>
        return bits;
    }

    /*
     * Measure each chosen Qubit. If any are true, return true.
     */
    public static Qubit[] or(Qubit[] bits)
    {
        bits = collapse(bits);

        for (int i = 0 ; i<bits.length ; i++)
        {
            if (bits[i].isOne()) // checks if any are |1>
            {
                bits[0] = new Qubit(0, 0, 1, 0); // returns |1>
                return bits;
            }
        }
        bits[0] = new Qubit(1, 0, 0, 0); // returns |0>
        return bits;
    }

    /*
     * Measure each chosen Qubit. If an odd number of them are true, return true.
     */
    public static Qubit[] xor(Qubit[] bits)
    {
        bits = collapse(bits);
        int countTrue = 0;

        for (int i = 0 ; i<bits.length ; i++)
        {
            if (bits[i].isOne()) countTrue++;
        }

        if (countTrue % 2 == 1)
        {
            bits[0] = new Qubit(0, 0, 1, 0); // returns |1>
            return bits;
        }
        else
        {
            bits[0] = new Qubit(1, 0, 0, 0); // returns |0>
            return bits;
        }
    }

    /*
     * Measure each chosen Qubit. If an even number of them are true, return true.
     * Simplified by the fact that it is equivalent to !xor
     */
    public static Qubit[] xnor(Qubit[] bits)
    {
        bits = collapse(bits);
        int countTrue = 0;

        for (int i = 0 ; i<bits.length ; i++)
        {
            if (bits[i].isOne()) countTrue++;
        }
        
        if (countTrue % 2 == 0)
        {
            bits[0] = new Qubit(0, 0, 1, 0); // returns |1>
            return bits;
        }
        else
        {
            bits[0] = new Qubit(1, 0, 0, 0); // returns |0>
            return bits;
        }
    }

    /*
     * Measure each chosen Qubit. If any are true, return false.
     */
    public static Qubit[] nor(Qubit[] bits)
    {
        bits = collapse(bits);
        
        for (int i = 0 ; i<bits.length ; i++)
        {
            if (bits[i].isOne()) 
            {
                bits[0] = new Qubit(1, 0, 0, 0); // returns |0>
                return bits;
            }
        }
        
        bits[0] = new Qubit(0, 0, 1, 0); // returns |1>
        return bits;
    }
}
