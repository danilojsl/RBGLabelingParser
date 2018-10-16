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
		DEP_LABEL,
		WORD_VEC,
		TYPE_END
	}
	
	private Dictionary[] dicts;
	
	private boolean isCounting;
	private TIntIntMap[] counters;
	
	
	public DictionarySet() 
	{	
		isCounting = false;
		int indexDictionaryTypes = DictionaryTypes.TYPE_END.ordinal();
		dicts = new Dictionary[indexDictionaryTypes];
		for (int i = 0; i < dicts.length; ++i) {
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
	
	public int getDictionarySize(DictionaryTypes tag)
	{
		int indexTag = tag.ordinal();
		return dicts[indexTag].dictionarySize();
	}
	
	public void stopGrowth(DictionaryTypes tag)
	{
		dicts[tag.ordinal()].stopGrowth();
	}
	
	public Dictionary getDictionary(DictionaryTypes tag)
	{
		return dicts[tag.ordinal()];
	}
	
	public void setCounters()
	{
		isCounting = true;
		counters = new TIntIntHashMap[dicts.length];
		for (int i = 0; i < dicts.length; ++i)
			counters[i] = new TIntIntHashMap();
	}
	
	public void closeCounters()
	{
		isCounting = false;
		counters = null;
	}

}

