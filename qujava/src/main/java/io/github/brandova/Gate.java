package io.github.brandova;

public class Gate 
{
    protected String type;     
    protected int[] indexes;  

    public Gate(String type, int[] indexes) 
    {
        this.type = type;
        this.indexes = indexes;
    }

    public String getType() 
    {
        return type;
    }

    public void setType(String type) 
    {
        this.type = type;
    }

    public int[] getindexes() 
    {
        return indexes;
    }

    public void setIndexes(int[] indexes) 
    {
        this.indexes = indexes;
    }

    public Qubit[] execute(Qubit[] input)
    {
        switch (type) 
        {
            case "M":
                return QubitGates.collapse(input);

            case "H":
                return QubitGates.hadamard(input);
        
            case "X":
                return QubitGates.not(input);

            case "N":
                return QubitGates.not(input);

            case "Y":
                return QubitGates.y(input);

            case "Z":
                return QubitGates.z(input);

            case "AND":
                return QubitGates.and(input);

            case "OR":
                return QubitGates.or(input);

            case "XOR":
                return QubitGates.xor(input);

            case "NOR":
                return QubitGates.nor(input);

            case "XNOR":
                return QubitGates.xnor(input);
            
            default:
                throw new IllegalStateException("Cannot resolve gate type: " + type);
        }
    
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Gate Type: ").append(type).append(", indexes: [");
        if (indexes != null) 
        {
            for (int i = 0; i < indexes.length; i++) 
            {
                sb.append(indexes[i]);
                if (i < indexes.length - 1) 
                {
                    sb.append(", ");
                }
            }
        }
        sb.append("]");
        return sb.toString();
    }
}
