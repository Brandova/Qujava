package io.github.brandova;

public class PhaseShiftGate extends Gate
{
    private final double phi;

    public PhaseShiftGate(String type, int[] indexes, double phi) 
    {
        super(type, indexes);
        this.phi = phi;
    }

    @Override
    public Qubit[] execute(Qubit[] input)
    {
        return QubitGates.phaseShift(input, phi);
    }

    public double getPhi()
    {
        return phi;
    }
}
