package io.github.brandova;

public class Algorithms 
{
    // Example: Grover's Algorithm
    public static Qubit[] Grovers(String target)
    {
        int lastIndex = target.length()-1;

        // repeatable operation for increased accuracy
        QuantumCircuit groversAlg = new QuantumCircuit(lastIndex+1);

        // oracle     
        for (int i = 0 ; i < lastIndex+1 ; i++)
        {                
            if (target.charAt(i) == '0')     
            groversAlg.queueGate("X", i); // mark the target qubit
        }
        groversAlg.queueGate("CZ", "0-"+(lastIndex-1), "0-"+lastIndex);
        groversAlg.queueGate("X", "0-"+lastIndex);                        

        // diffusion
        groversAlg.queueGate("X", "0-"+lastIndex);
        groversAlg.queueGate("CX", "0-"+(lastIndex-1), "0-"+lastIndex);  
        groversAlg.queueGate("X", "0-"+lastIndex);

        Qubit[] qbs = groversAlg.execute(); // setup the Qubits for repeated diffusion

        // execute 3 times. Passing qbs as the argument effectively repeats the operation.
        qbs = groversAlg.execute(qbs);
        qbs = groversAlg.execute(qbs);
        qbs = groversAlg.execute(qbs);

        return qbs;
    }

    public static void testQFT(int numQubits)
    {
        Qubit[] register = Algorithms.Grovers(genRandomBinary(numQubits));
        System.out.print("QFT input: ");
        System.out.println(Qubit.qubitsToBinaryString(register));

        // Measure the execution time
        long startTime = System.nanoTime(); // Start time

        register = Algorithms.QFT(register);

        long endTime = System.nanoTime(); // End time

        // Calculate elapsed time in milliseconds
        double elapsedTimeInMs = (endTime - startTime) / 1_000_000.0;

        System.out.println("QFT output: ");
        System.out.println(Qubit.qubitsToString(register));

        // Output results
        System.out.println("Quantum Fourier Transform Execution Time: " + elapsedTimeInMs + " ms");
    }

    private static String genRandomBinary(int numBits)
    {
        String r = "";
        for (int i = 0 ; i<numBits ; i++)
        {
            if (Math.random() > 0.5)
            r += "1";
            else r += "0";
        }
        return r;
    }
    
    /**
    * Performs the Quantum Fourier Transform (QFT) on the input qubits.
    * The QFT maps computational basis states to the Fourier basis and is a fundamental operation in quantum computing.
    *
    * Steps:
    * 1. Apply a Hadamard gate to each qubit to create superposition.
    * 2. For each qubit, apply controlled phase shift gates (R_k) to introduce phase changes 
    *    that depend on the relative position of the qubits.
    * 3. Execute the quantum circuit to produce the transformed qubits.
    *
    * This implementation is mostly precise, however large Qubit registers will be error prone
    * primarily because of the method phiOfRsubK(int). 
    * For registers of over 1024 Qubits, phiOfRsubK will return 0 when k > 1024
    *
    * @param input An array of Qubits representing the quantum state to be transformed.
    * @return Qubit[] The transformed quantum state after applying the QFT.
    */
    public static Qubit[] QFT(Qubit[] input)
    {
        // If any Qubits are null, initialize to |0> state
        input = initialize(input);

        int numQubits = input.length; 
        QuantumCircuit qft = new QuantumCircuit(numQubits); 

        // Loop over each qubit in the input register
        for (int i = 0; i < numQubits; i++)
        {
            // Step 1: Apply a Hadamard gate to the current qubit
            qft.queueGate("H", i);

            // Step 2: Apply the controlled phase shift gates (R_k)
            // R_k applies a phase shift phi = pi / 2^(k-1) to qubit i controlled by  higher-index qubits
            for (int k = 2; k < numQubits - i + 1; k++)
            {
                /*
                 * - "CS" indicates a Controlled Phase Shift gate.
                 * - phiOfRsubK(k): Calculates the phase shift value for R_k (phi = π / 2^(k-1)).
                 * - listHigherNums(i, numQubits): Returns a list of qubits with indices higher than i
                 *   that will serve as control for the phase shift.
                 * - i: The current qubit that recieves the phase shift operation.
                 */
                qft.queueGate("CS", phiOfRsubK(k), k+i-1, i);
            }
        }
        return qft.execute(input);
    }

    // Find the phase angle for R_k, denoted phi
    private static double phiOfRsubK(int k)
    {
        return Math.PI / (Math.pow(2, k-1));
    }

    private static Qubit[] initialize(Qubit[] inp)
    {
        for (int i = 0 ; i < inp.length ; i++)
        {
            if (inp[i] == null)
            inp[i] = new Qubit(1, 0, 0, 0);
        }
        return inp;
    }
}
