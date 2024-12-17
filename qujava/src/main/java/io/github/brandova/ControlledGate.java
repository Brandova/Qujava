package io.github.brandova;

/*
 * Before calling execute on a ControlledGate, the method calling it should check its control first
 * See QuantumCircuit 151-154 for an example
 * This allows the ability to execute without checking controls if desired
 */
public class ControlledGate extends Gate
{
    private int[] controllers;
    private final double phi; // Special case: controlled phase shift gate

    public ControlledGate(String type, int[] indexes, int[] controllers) 
    {
        super(type, indexes);
        this.controllers = controllers;
        phi = 0;
    }

    // Controlled Phase Shift Gate constructor
    public ControlledGate(String type, double phi, int[] indexes, int[] controllers) 
    {
        super(type, indexes);
        this.controllers = controllers;
        this.phi = phi;
    }

    public boolean controlCheck(Qubit[] input)
    {
        for (int i = 0 ; i < controllers.length ; i++)
        {
            // check each bit contributing to the controller. If any return false this gate will not execute
            if (!input[controllers[i]].measure()) 
                return false;
        }
        
        return true;
    }

    // This method is assuming control has already been checked
    // To handle the String storing gate type having a leading C, temporarily remove the C from type using the substring method
    @Override
    public Qubit[] execute(Qubit[] input)
    {
        // if this is a controlled phase shift gate
        if (type.charAt(1) == 'S')
        return QubitGates.phaseShift(input, phi);

        String backup = this.type;
        this.type = backup.substring(1);

        Qubit[] output = super.execute(input);

        this.type = backup;
        return output;
    }
}
