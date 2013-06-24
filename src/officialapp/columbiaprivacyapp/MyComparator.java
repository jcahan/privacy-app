package officialapp.columbiaprivacyapp;

import java.util.Comparator;

public class MyComparator implements Comparator<BlacklistWord>{
	@Override
	public int compare(BlacklistWord o1, BlacklistWord o2) {
		if(o1.compareTo(o2)==0) return 0; 
		else if(o1.compareTo(o2)<0){
			return -1; 
		}
		else return 1;
	}
}