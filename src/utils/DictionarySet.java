package utils;

import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;

import java.io.Serializable;

public class DictionarySet implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public enum DictionaryTypes 
	{
		POS,
		WORD,
		DEPLABEL,
		WORDVEC,
		TYPE_END
	}
	
	private int tot;
	private Dictionary[] dicts;
	
	private boolean isCounting;
	private TIntIntMap[] counters;
	
	
	public DictionarySet() 
	{	
		isCounting = false;
		int indexDictionaryTypes = DictionaryTypes.TYPE_END.ordinal();
		dicts = new Dictionary[indexDictionaryTypes];
		tot = dicts.length;
		for (int i = 0; i < tot; ++i) {
			dicts[i] = new Dictionary();
		}
	}

	public int lookupIndex(DictionaryTypes tag, String item) 
	{
		int id = dicts[tag.ordinal()].lookupIndex(item);
		
		if (isCounting && id > 0) {
			counters[tag.ordinal()].putIfAbsent(id, 0);
			counters[tag.ordinal()].increment(id);
		}
		
		return id <= 0 ? 1 : id;
	}
	
	public int size(DictionaryTypes tag)
	{
		int indexTag = tag.ordinal();
		return dicts[indexTag].size();
	}
	
	public void stopGrowth(DictionaryTypes tag)
	{
		dicts[tag.ordinal()].stopGrowth();
	}
	
	public Dictionary get(DictionaryTypes tag)
	{
		return dicts[tag.ordinal()];
	}
	
	public void setCounters()
	{
		isCounting = true;
		counters = new TIntIntHashMap[tot];
		for (int i = 0; i < tot; ++i)
			counters[i] = new TIntIntHashMap();
	}
	
	public void closeCounters()
	{
		isCounting = false;
		counters = null;
	}

}

