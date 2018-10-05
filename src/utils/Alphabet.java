package utils;

import gnu.trove.map.hash.TLongIntHashMap;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;


public class Alphabet implements Serializable
{
	private TLongIntHashMap map;
    private int numEntries;
    private boolean growthStopped = false;

    private Alphabet (int capacity)
    {
    	this.map = new TLongIntHashMap(capacity);
		numEntries = 0;
    }
    
    public Alphabet ()
    {
    	this (10000);
    }

    /** Return -1 if entry isn't present. */
    public int lookupIndex (long entry, int value)
    {
		int ret = map.get(entry);
		if (ret <= 0 && !growthStopped) {
			numEntries++;
			ret = value + 1;
		    map.put (entry, ret);
		}
		return ret - 1;	// feature id should be 0-based
    }
    
    /** Return -1 if entry isn't present. */
    public int lookupIndex (long entry)
    {
		int ret = map.get(entry);
		if (ret <= 0 && !growthStopped) {
			numEntries++;
			ret = numEntries;
		    map.put (entry, ret);
		}
		return ret - 1;	// feature id should be 0-based
    }

    public void stopGrowth ()
    {
    	growthStopped = true;
    }

    // Serialization 
		
    private static final long serialVersionUID = 1;
    private static final int CURRENT_SERIAL_VERSION = 0;

    private void writeObject (ObjectOutputStream out) throws IOException {
		out.writeInt (CURRENT_SERIAL_VERSION);
		out.writeInt (numEntries);
		out.writeObject(map);
		out.writeBoolean (growthStopped);
	}
	
    private void readObject (ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.readInt();
		numEntries = in.readInt();
		map = (TLongIntHashMap)in.readObject();
		growthStopped = in.readBoolean();
    }
}
	