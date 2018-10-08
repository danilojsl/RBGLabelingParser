/* Copyright (C) 2002 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */




/** 
    @author Andrew McCallum <a href="mailto:mccallum@cs.umass.edu">mccallum@cs.umass.edu</a>
*/

package utils;

import gnu.trove.map.hash.TObjectIntHashMap;

import java.io.*;

public class Dictionary implements Serializable
{

	// Serialization
	private static final long serialVersionUID = 1;

	private TObjectIntHashMap map;
	
    private int numEntries;
    private boolean growthStopped = false;

    private Dictionary (int capacity)
    {
    	this.map = new TObjectIntHashMap(capacity);
		numEntries = 0;
    }

    Dictionary ()
    {
    	this (10000);
    }

    /** Return -1 (in old trove version) or 0 (in trove current verion) if entry isn't present. */
    public int lookupIndex (Object entry)
    {
		if (entry == null)
		    throw new IllegalArgumentException ("Can't lookup \"null\" in an Alphabet.");
		int ret = map.get(entry);
		if (ret <= 0 && !growthStopped) {
			numEntries++;
			ret = numEntries;
		    map.put(entry, ret);
		}
		return ret;
    }

    public Object[] toArray () {
    	return map.keys();
    }

    public int size ()
    {
    	return numEntries;
    }

    void stopGrowth ()
    {
    	growthStopped = true;
    }

}
