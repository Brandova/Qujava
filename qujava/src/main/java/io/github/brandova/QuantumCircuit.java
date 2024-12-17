package io.github.brandova;

import java.util.LinkedList;
import java.util.Queue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
 * Gates glossary 
 * M      -> Measure
 * N      -> Not
 * H      -> Hadamard
 * S      -> Phase shift
 * X      -> Pauli-X (equivalent to not)
 * Y      -> Pauli-Y
 * Z      -> Pauli-Z
 * C___   -> Controlled Gate
 * And
 * Or
 * Nor
 * Xor
 * Xnor
 */

 /*
  * this QuantumCircuit implementation treats circuits as functions rather than one-off operations
  * therefore, you can repeat the same circuit operation with different inputs, or the same input several times.
  */
public class QuantumCircuit 
{
    // ---------------------------------
    // SECTION A : Basic Implementations
    // ---------------------------------

    private int inputSize;
    private Queue<Gate> gateQueue;

    public QuantumCircuit(int numQubits)
    {
        inputSize = numQubits;
        gateQueue = new LinkedList<Gate>();
    }

    public int getInputSize()
    {
        return inputSize;
    }

    // ---------------------------------
    // SECTION B : Gate Queueing Methods
    // ---------------------------------

    /*
     * installs a QubitGate at the next time interval in the circuit runtime
     * String field indicates what gate (and, hadamard, etc)
     * T field indicates which qubits in the circuit are effected (starting from index 0)
     */

    // Most gates
    public <T> void queueGate(String gateType, T inputBits)
    {
        if (gateType.charAt(0) == 'C')
            throw new IllegalArgumentException("Controlled gate must be given controller Qubits. To queue a Controlled Gate, use queueGate(String, T, T)");
        if (gateType == "S")
            throw new IllegalArgumentException("Phase Shift gate must be given a phase angle. To queue a Phase Shift Gate, use queueGate(String, double, T)");

        // verify inputBits is an int, String or String[]
        verifyType(inputBits);

        int[] convertedInputBits = getGateIndices(inputBits);
    
        gateQueue.add(new Gate(gateType, convertedInputBits));
    }

    // Controlled Gates
    public <T> void queueGate(String gateType, T controlBits, T inputBits)
    {
        if (gateType.charAt(0) != 'C')
            throw new IllegalArgumentException("Method arguments for queueing " +gateType+ " were indicative of a Controlled gate");

        // verify targetBits and controlBits are an int, String or String[]
        verifyType(inputBits);
        verifyType(controlBits);

        int[] convertedInputBits = getGateIndices(inputBits);
        int[] convertedControlBits = getGateIndices(controlBits);
    
        gateQueue.add(new ControlledGate(gateType, convertedInputBits, convertedControlBits));
    }

    // Phase Shift Gates
    public <T> void queueGate(String gateType, double phaseAngle, T inputBits)
    {
        // If its meant to be a controlled gate, the second argument must be converted to a String
        // because this method takes priority for the args (String, int, T), which is what (String, T, T) is meant to handle
        if (gateType.charAt(0) == 'C') {     
            queueGate(gateType, Integer.toString((int) phaseAngle), inputBits);
            return;}
        if (gateType != "S")
            throw new IllegalArgumentException("Method arguments for queueing " +gateType+ " were indicative of a Phase Shift gate");

        // verify targetBits is an int, String or String[]
        verifyType(inputBits);

        int[] convertedInputBits = getGateIndices(inputBits);
    
        gateQueue.add(new PhaseShiftGate(gateType, convertedInputBits, phaseAngle));
    }

    // Controlled Phase Shift Gates
    public <T> void queueGate(String gateType, double phaseAngle, T controlBits, T inputBits)
    {
        if (gateType.charAt(0) != 'C' || gateType.charAt(1) != 'S') 
            throw new IllegalArgumentException("Method arguments for queueing " +gateType+ " were indicative of a Controlled Phase Shift gate");

        // verify targetBits and controlBits are an int, String or String[]
        verifyType(inputBits);
        verifyType(controlBits);

        int[] convertedInputBits = getGateIndices(inputBits);
        int[] convertedControlBits = getGateIndices(controlBits);
    
        gateQueue.add(new ControlledGate(gateType, phaseAngle, convertedInputBits, convertedControlBits));
    }

    // -------------------------------------
    // SECTION C : Circuit Execution Methods
    // -------------------------------------

    // if no input specified, execute with default qubit information (all set to |0>)
    public Qubit[] execute()
    {
        Qubit[] input = Qubit.generateDefaultQubits(inputSize);
        return execute(input);
    }

    // TODO ability to choose a register for the output
    public Qubit[] execute(Qubit[] input)
    {
        if (input.length != inputSize)
            throw new IllegalArgumentException("Circuit input did not match required size " +inputSize);

        // after executing, the circuit's gate queue needs to be reset. 
        Queue<Gate> backup = this.gateQueue;

        while(gateQueue.peek() != null)
        {
            Gate currGate = gateQueue.poll();

            // short circuit evaluation if control gate does not execute based on controls.
            if (currGate instanceof ControlledGate) { 
                if (controlFails(currGate, input))
                    continue;}

            // retrieve the relevant bits for this gate
            int[] gateInputs = currGate.getindexes();
            Qubit[] inputBits = new Qubit[gateInputs.length];

            for (int i = 0 ; i<gateInputs.length ; i++)
            {
                inputBits[i] = input[gateInputs[i]];
            }

            inputBits = currGate.execute(inputBits); // reusing inputBits after gate execution. It is now storing the gate output.

            // mapping output to respective input indices
            for (int i = 0 ; i<inputBits.length ; i++)
            {
                input[gateInputs[i]] = inputBits[i];
            }
        }

        // reset gate queue
        gateQueue = backup;
        return input;
    }

    // --------------------------
    // SECTION D : Helper Methods
    // --------------------------

    private <T> void verifyType(T targetBits) 
    {
        if (!(targetBits instanceof Integer || targetBits instanceof String || targetBits instanceof int[] || targetBits instanceof String[])) 
            throw new IllegalArgumentException("Gate queueing requires a String, int, String[] or int[] input as the last method argument");
    }

    private <T> int[] getGateIndices(T targetBits)
    {
        // simplest case is its already an int[], which is what Gate's implementation is built around
        if (targetBits instanceof int[])
            return (int[]) targetBits;

        else if (targetBits instanceof Integer)
            return new int[]{(int) targetBits};
        
        else if (targetBits instanceof String)
            return convertAndSort((String) targetBits);

        else // if targetBits is a String[], concatenate it's contents
        {
            String[] contents = (String[]) targetBits; // avoid further typecasting
            String concatenated = "";

            for (int i = 0 ; i<contents.length ; i++)
            { 
                concatenated += contents[i] + ","; // extra comma at string end is safe, we use .split(",") in convertAndSort
            }             
            return convertAndSort(concatenated);
        }
    }

    private static int[] convertAndSort(String input) 
    {
        List<Integer> numbers = new ArrayList<>();

        String[] parts = input.split(",");
        for (String part : parts) 
        {
            part = part.trim(); // Remove extra spaces

            if (part.contains("-")) // range handling
            { 
                String[] range = part.split("-");
                int start = Integer.parseInt(range[0].trim());
                int end = Integer.parseInt(range[1].trim());

                for (int i = start; i <= end; i++) 
                {
                    numbers.add(i);
                }
            } 
            else // Handle single numbers like "5"
                numbers.add(Integer.parseInt(part));
        }

        // Convert List<Integer> to int[]
        int[] result = numbers.stream().mapToInt(Integer::intValue).toArray();

        // Check if the array is sorted
        if (!isSorted(result))
            Arrays.sort(result);

        return result;
    }

    private static boolean isSorted(int[] array) 
    {
        for (int i = 1; i < array.length; i++) 
        {
            if (array[i] < array[i - 1])
                return false;
        }
        return true;
    }

    // failing control means this gate will not execute
    private static boolean controlFails(Gate currGate, Qubit[] input)
    {
        ControlledGate cGate = (ControlledGate) currGate;
        return cGate.controlCheck(input);
    }

}
