package myapp.columbiaprivacyapp;

public class BlacklistWord implements Comparable<BlacklistWord>{

	private String word;
	private long id;

	public BlacklistWord () {
		this.word = "";		
	}
	public BlacklistWord (String theWord) {
		this.word = theWord;		
	}
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	// Will be used by the ArrayAdapter in the ListView
	@Override
	public String toString() {
		return word;
	}
	//For comparator
	@Override 
	public int compareTo(BlacklistWord o2) {
		if(word.compareTo(o2.getWord())>0) return 1;
		if(word.compareTo(o2.getWord())==0) return 0;
		return -1;
	}	
}
