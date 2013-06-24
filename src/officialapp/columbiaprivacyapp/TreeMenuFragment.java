package officialapp.columbiaprivacyapp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TreeSet;

import officialapp.columbiaprivacyapp.R;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;

public class TreeMenuFragment extends SherlockFragment{
	protected View view; 
	protected LinkedHashMap<Item,ArrayList<Item>> groupList;
	protected ExpandableListView expandableListView;
	protected ExpandableAdapter adapter; 


	private LinkedHashMap<Item, ArrayList<Item>> myHistory;

	//All strings within string, and whether they are in array 
	//(Key: TextEntry, Value: String Table) 
	private HashMap<String, String> allStrings = new HashMap<String, String>();
	private HashMap<String, Integer> groupPositions = new HashMap<String, Integer>();
	private HashMap<String, HashMap<String, Integer>> groupChildPosition = new HashMap<String, HashMap<String, Integer>>(); //(Key: 

	private HashMap<String, Integer>  alcMap = new HashMap<String, Integer>();
	private HashMap<String, Integer>  adultMap = new HashMap<String, Integer>();
	private HashMap<String, Integer>  actLifeMap = new HashMap<String, Integer>(); 
	private HashMap<String, Integer>  artsMap= new HashMap<String, Integer>();
	private HashMap<String, Integer>  autoMap= new HashMap<String, Integer>();
	private HashMap<String, Integer>  beautyMap= new HashMap<String, Integer>();
	private HashMap<String, Integer>  bicylMap= new HashMap<String, Integer>();
	private HashMap<String, Integer>  educMap= new HashMap<String, Integer>();
	private HashMap<String, Integer>  eventMap= new HashMap<String, Integer>();
	private HashMap<String, Integer>  finaMap= new HashMap<String, Integer>();
	private HashMap<String, Integer>  foodMap= new HashMap<String, Integer>();
	private HashMap<String, Integer>  healthMap= new HashMap<String, Integer>();
	private HashMap<String, Integer>  homeMap= new HashMap<String, Integer>();
	private HashMap<String, Integer>  hotelMap= new HashMap<String, Integer>();
	private HashMap<String, Integer>  flavorMap= new HashMap<String, Integer>();
	private HashMap<String, Integer>  localMap= new HashMap<String, Integer>();
	private HashMap<String, Integer>  massMap= new HashMap<String, Integer>();
	private HashMap<String, Integer>  sensMap= new HashMap<String, Integer>();
	private HashMap<String, Integer>  petsMap= new HashMap<String, Integer>();
	private HashMap<String, Integer>  professionalMap= new HashMap<String, Integer>();
	private HashMap<String, Integer>  publicMap= new HashMap<String, Integer>();
	private HashMap<String, Integer>  realMap= new HashMap<String, Integer>();
	private HashMap<String, Integer>  relMap= new HashMap<String, Integer>();
	private HashMap<String, Integer>  restMap= new HashMap<String, Integer>();
	private HashMap<String, Integer>  shopMap= new HashMap<String, Integer>();

	private final int ALC_SIZE = 10; 
	private final int ADULT_SIZE = 5;
	private final int HEALTH_SIZE = 73;
	private final int FINA_SIZE = 13;
	private final int SENS_SIZE = 28; 
	private final int REL_SIZE = 6;  

	private final int ACT_SIZE =  78; 
	private final int ART_SIZE = 28; 
	private final int AUTO_SIZE = 22; 
	private final int BEAUT_SIZE = 21; 
	private final int BIKE_SIZE = 5; 
	private final int EDUC_SIZE = 22; 
	private final int EVENT_SIZE = 19; 
	private final int FOOD_SIZE = 43; 
	private final int HOME_SIZE = 29; 
	private final int HOTEL_SIZE = 27; 
	private final int FLAVOR_SIZE = 1; 
	private final int LOCAL_SIZE = 32; 
	private final int MASS_SIZE = 4; 
	private final int PETS_SIZE = 10; 
	private final int PROF_SIZE = 32; 
	private final int PUB_SIZE = 11; 
	private final int REAL_SIZE = 18; 
	private final int REST_SIZE = 208; 
	private final int SHOP_SIZE = 94; 

	private final int NUMBER_OF_GROUPS = 25; 
	//For setting the parent checkbox to true, hardcoded it in
	private Integer[] actualGroupSize = {ALC_SIZE, ADULT_SIZE, HEALTH_SIZE, FINA_SIZE, SENS_SIZE, REL_SIZE, ACT_SIZE, ART_SIZE, AUTO_SIZE, BEAUT_SIZE, BIKE_SIZE, EDUC_SIZE, EVENT_SIZE, FOOD_SIZE, HOME_SIZE, HOTEL_SIZE, FLAVOR_SIZE, LOCAL_SIZE, MASS_SIZE, PETS_SIZE, PROF_SIZE, PUB_SIZE, REAL_SIZE, REST_SIZE, SHOP_SIZE};


	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		MainActivity.getInstance().invalidateOptionsMenu();
		view = inflater.inflate(R.layout.treemenu, container, false);

		//Creating AutoCompleteTextView, adding adapter, and notifying to update view
		AutoCompleteTextView autoView = (AutoCompleteTextView) view.findViewById(R.id.edit_message);
		String[] itemOptions = getResources().getStringArray(R.array.edit_message);

		ArrayAdapter<String> theAdapter = 
				new ArrayAdapter<String>(
						getActivity(), android.R.layout.simple_dropdown_item_1line, itemOptions);
		autoView.setAdapter(theAdapter);
		autoView.setTextColor(Color.BLACK);
		((BaseAdapter) autoView.getAdapter()).notifyDataSetChanged();

		//Creating Button and Setting Listener
		Button b = (Button) view.findViewById(R.id.post_blacklist_button);
		b.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				AutoCompleteTextView editText=(AutoCompleteTextView) view.findViewById(R.id.edit_message);
				String blackListItem = editText.getText().toString();
				editText.setText("");

				//First need to check if this value exists within the entire List. If it does, then check/uncheck the box. 
				if(allStrings.containsKey(blackListItem.toLowerCase())) {
					//Get which group position 
					String theGroupName = allStrings.get(blackListItem.toLowerCase());
					int theGroupPosition = groupPositions.get(theGroupName);

					//Get which child position
					int childPosition = groupChildPosition.get(theGroupName).get(blackListItem.toLowerCase());
					Item theItem = adapter.getChild(theGroupPosition, childPosition);
					//If Checked, then uncheck
					if(theItem.isChecked) {
						theItem.isChecked = false; 
					}
					//If unchecked, then check
					else{
						theItem.isChecked = true;
						//need to make sure group box is UNCHECKED
					}
					adapter.notifyDataSetChanged();
				}

				MainActivity.getInstance().postBlackListItem(blackListItem);
				//refreshes the rest 
				MainActivity.getInstance().invalidateOptionsMenu();
			}
		});
		Toast.makeText(getSherlockActivity(), "To add items, enter them into the text field, or select them from the drop-down menus", Toast.LENGTH_SHORT).show();
		//Creating ExpandableListView Menu Below..
		initViews(view);
		if(view != null) { return view; }

		((ViewGroup) autoView.getParent()).removeView(autoView);
		container.addView(autoView);

		return view;
	}

	//Collapses all windows 
	public void collapseAll() {
		if(expandableListView!=null) {
			for(int i=0; i<expandableListView.getCount(); i++)
			{
				expandableListView.collapseGroup(i);
			}
		}
	}

	//Refreshes the TreeMenuFragment with Updated 
	public TreeMenuFragment refresh() {
		if(expandableListView!=null) {
			initContactList();

			updateExpandableListView();
		}
		else {

		}
		return this; 
	}

	private void incrementGroupSize(int groupSize, Integer[] eachGroupSize) {
		eachGroupSize[groupSize]++; 


		if(eachGroupSize[groupSize].equals(actualGroupSize[groupSize])) {
			adapter.getGroup(groupSize).isChecked = true; 
		}
	}

	//TODO: Abstract this out later
	public void deleteFromMenu(String blackListItem) {
		if(allStrings.containsKey(blackListItem.toLowerCase())) {
			//Get which group position 
			String theGroupName = allStrings.get(blackListItem.toLowerCase());
			int theGroupPosition = groupPositions.get(theGroupName);

			//Get which child position
			int childPosition = groupChildPosition.get(theGroupName).get(blackListItem.toLowerCase());

			Item theItem = adapter.getChild(theGroupPosition, childPosition);
			if(theItem==null) return; //TODO: Test if this will happen --> it should not!
			theItem.isChecked = false; 

			adapter.notifyDataSetChanged();
		}
	}

	private void updateExpandableListView() {
		expandableListView = (ExpandableListView) view.findViewById(R.id.expandableListView);
		adapter = new ExpandableAdapter(getActivity(), expandableListView, groupList);
		TreeSet<BlacklistWord> setOfWords =  MainActivity.getInstance().datasource.GetAllWords();
		Integer[] eachGroupSize = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

		expandableListView.setAdapter(adapter);
		for(BlacklistWord eachWord :setOfWords) {
			String toCheck = eachWord.getWord(); 
			if(allStrings.containsKey(eachWord.getWord())) {
				String theGroupName = allStrings.get(toCheck.toLowerCase());
				int theGroupPosition = groupPositions.get(theGroupName);
				incrementGroupSize(theGroupPosition, eachGroupSize);

				//Get which child position
				int childPosition = groupChildPosition.get(theGroupName).get(toCheck.toLowerCase());

				//TODO: Check if both 0...
				Item theItem = adapter.getChild(theGroupPosition, childPosition);
				if(theItem==null) break; 
				theItem.isChecked = true; 
			}
			((BaseAdapter) expandableListView.getAdapter()).notifyDataSetChanged();
		}
	}

	private void initViews(View theView){
		if(groupList==null) {
			initContactList();
		}
		updateExpandableListView();
	}
	private void initContactList(){
		groupList = new LinkedHashMap<Item,ArrayList<Item>>();

		ArrayList<Item> groupsList = fetchGroups();

		int whatGroup = 0; 
		for(Item item:groupsList){
			String[] ids = item.id.split(",");
			ArrayList<Item> groupMembers =new ArrayList<Item>();

			for(int i=0;i<ids.length;i++){
				String groupId = ids[i];
				groupMembers.addAll(fetchGroupMembers(groupId));
			}
			String shortName = item.name;
			item.name = item.name +" ("+groupMembers.size()+")";
			groupList.put(item,groupMembers);

			//Placing group Positions;
			//First Get Correct HashMap to Add 
			groupPositions.put(shortName.toLowerCase(), whatGroup++);
		}

	}

	private ArrayList<Item> fetchGroups(){
		ArrayList<Item> groupList = new ArrayList<Item>();
		//List each group 
		for(int i=0; i<NUMBER_OF_GROUPS; i++){
			Item item = new Item();
			String groupName; 

			if(i==0) {
				item.id = groupName = "Alcohol / Drugs";
			}
			else if (i==1) {
				item.id = groupName = "Adult Education";
			}
			else if (i==2) {
				item.id = groupName = "Health & Medical";
			}
			else if (i==3) {
				item.id = groupName = "Financial / Legal";
			}
			else if (i==4) {
				item.id = groupName = "Sensitive Activities";
			}
			else if (i==5) {
				item.id = groupName = "Religious Organizations";
			}
			else if(i==6) {
				item.id = groupName = "Active Life";
			}
			else if(i==7) {
				item.id = groupName = "Arts & Entertainment";
			}
			else if(i==8) {
				item.id = groupName = "Automotive";
			}
			else if(i==9) {
				item.id = groupName = "Beauty & Spas";
			}
			else if(i==10) {
				item.id =groupName = "Bicycles";
			}
			else if(i==11) {
				item.id =groupName = "Education";
			} 
			else if(i==12) {
				item.id = groupName = "Event Planning & Services";
			}
			else if(i==13) {
				item.id=groupName = "Food";
			}
			else if(i==14) {
				item.id=groupName = "Home Services";
			}
			else if(i==15) {
				item.id=groupName = "Hotels & Travel";
			}
			else if(i==16) {
				item.id=groupName = "Local Flavor";
			}
			else if(i==17) {
				item.id= groupName = "Local Services";
			}
			else if(i==18) {
				item.id=groupName = "Mass Media";
			}
			else if(i==19) {
				item.id=groupName = "Pets";
			}
			else if(i==20) {
				item.id=groupName = "Professional Services";
			}
			else if(i==21) {
				item.id=groupName = "Public Services & Government";
			}
			else if(i==22) {
				item.id=groupName = "Real Estate";
			}
			else if(i==23) {
				item.id=groupName = "Restaurants";
			}
			else {
				item.id=groupName = "Shopping";
			}
			item.name = groupName;
			groupList.add(item);	
		}
		//		Collections.sort(groupList,new Comparator<Item>() {
		//
		//			public int compare(Item item1, Item item2) {
		//
		//				return item2.name.compareTo(item1.name)<0
		//						?0:-1;
		//			}
		//		});
		return groupList;
	}

	private ArrayList<Item> fetchGroupMembers(String groupId){
		ArrayList<Item> groupMembers = new ArrayList<Item>(); 
		if (groupId.equals("Alcohol / Drugs")) {
			String[] toAdd = {"bars","beer bars","beer, wine & spirits","breweries","cannabis clinics","dive bars","hookah bars","sports bars","wine bars","wineries"}; 
			groupMembers = addMyMembers(toAdd, "alcohol / drugs", alcMap);
		}
		else if(groupId.equals("Adult Education")) {
			String[] toAdd = {"adult education","private tutors","specialty schools","test preparation","tutoring centers"}; 
			groupMembers = addMyMembers(toAdd, "adult education",adultMap);
		}
		//down one 
		else if(groupId.equals("Health & Medical")) {
			String[] toAdd = {"acupuncture","allergists","anesthesiologists","audiologist","bulk billing","cardiologists","chiropractors","cosmetic dentists","cosmetic surgeons","counseling & mental health","dental hygienists","dentists","dermatologists","diagnostic imaging","diagnostic services","doctors","ear nose & throat","endodontists","fertility","gastroenterologist","general dentistry","gerontologists","health & medical","hearing aid providers","hearing aids","home health care","hospice","hospitals","internal medicine","laboratory testing","lactation services","laser eye surgery/lasik","massage therapy","medical centers","medical transportation","midwives","mobile clinics","naturopathic/holistic","neurologist","nutritionists","obstetricians & gynecologists","occupational therapy","oncologist","ophthalmologists","optometrists","oral surgeons","orthodontists","orthopedists","osteopathic physicians","osteopaths","pediatric dentists","pediatricians","periodontists","pharmacy","physical therapy","podiatrists","proctologists","psychiatrists","pulmonologist","reflexology","rehabilitation center","retirement homes","saunas","speech therapists","sports medicine","storefront clinics","surgeons","tattoo removal","traditional chinese medicine","urgent care","urologists","walk-in clinics","weight loss centers"}; 
			groupMembers = addMyMembers(toAdd, "health & medical", healthMap);
		}
		else if(groupId.equals("Financial / Legal")) {
			String[] toAdd = {"banks & credit unions","check cashing/pay-day loans","departments of motor vehicle","family practice","financial advising","financial services","general litigation","insurance","investing","lawyers","pawn shops","police departments","tax services"};  
			groupMembers = addMyMembers(toAdd, "financial / legal", finaMap);
		}
		else if(groupId.equals("Sensitive Activities")) {
			String[] toAdd = {"adult entertainment","beach bars","beer gardens","bridal","casinos","champagne bars","cocktail bars","coffeeshops","comedy clubs","country dance halls","dance clubs","dance restaurants","fasil music","gay bars","gun/rifle ranges","hotel bar","irish pub","karaoke","lingerie","lounges","maternity wear","nightlife","piano bars","piercing","pool halls","security systems","tattoo","wedding planning"}; 
			groupMembers = addMyMembers(toAdd, "sensitive activities", sensMap);
		}
		else if(groupId.equals("Religious Organizations")) {
			String[] toAdd = {"buddhist temples","churches","hindu temples","mosques","religious organizations","synagogues"};
			groupMembers = addMyMembers(toAdd, "religious organizations", relMap);
		}
		//down one
		else if(groupId.equals("Active Life")) {
			String[] toAdd = {"active life","amateur sports teams","amusement parks","aquariums","archery","badminton","barre classes","bathing area","beaches","bicycle paths","bike rentals","boating","boot camps","bowling","boxing","bungee jumping","climbing","dance studios","disc golf","diving","dog parks","experiences","fishing","fitness & instruction","free diving","gliding","go karts","golf","gymnastics","gyms","hang gliding","hiking","horse racing","horseback riding","hot air balloons","indoor playcentre","kids activities","kiteboarding","lakes","laser tag","lawn bowling","leisure centers","martial arts","mini golf","mountain biking","nudist","paddleboarding","paintball","parks","pilates","playgrounds","public plazas","rafting/kayaking","recreation centers","rock climbing","sailing","scuba diving","skate parks","skating rinks","skiing","skydiving","soccer","spin classes","sport equipment hire","sports clubs","squash","summer camps","surfing","swimming lessons/schools","swimming pools","tai chi","tennis","trainers","trampoline parks","tubing","yoga","zoos","zorbing"}; 
			groupMembers = addMyMembers(toAdd, "active life", actLifeMap);
		}
		else if(groupId.equals("Arts & Entertainment")) {
			String[] toAdd = {"arcades","art galleries","arts & entertainment","betting centers","botanical gardens","castles","choirs","christmas markets","cinema","cultural center","festivals","fun fair","general festivals","jazz & blues","marching bands","museums","music venues","opera & ballet","performing arts","professional sports teams","psychics & astrologers","race tracks","social clubs","stadiums & arenas","street art","tablao flamenco","ticket sales","trade fairs"};
			groupMembers = addMyMembers(toAdd, "arts & entertainment", artsMap);
		}
		else if(groupId.equals("Automotive")) {
			String[] toAdd ={"auto detailing","auto glass services","auto loan providers","auto parts & supplies","auto repair","automotive","boat dealers","body shops","car dealers","car stereo installation","car wash","gas & service stations","motorcycle dealers","motorcycle repair","oil change stations","parking","rv dealers","smog check stations","tires","towing","truck rental","windshield installation & repair"}; 
			groupMembers = addMyMembers(toAdd, "automotive", autoMap);
		}
		else if(groupId.equals("Beauty & Spas")) {
			String[] toAdd = {"barbers","beauty & spas","blow dry/out services","cosmetics & beauty supply","day spas","eyelash service","hair extensions","hair removal","hair salons","hair stylists","laser hair removal","makeup artists","massage","medical spas","men's hair salons","nail salons","perfume","permanent makeup","rolfing","skin care","tanning"}; 
			groupMembers = addMyMembers(toAdd, "beauty & spas", beautyMap);
		}
		else if(groupId.equals("Bicycles")) {
			String[] toAdd = {"bicycles","bike associations","bike repair","bike shop","special bikes"}; 
			groupMembers = addMyMembers(toAdd, "bicycles", bicylMap);
		}
		//education down 5
		else if(groupId.equals("Education")) {
			String[] toAdd = {"art schools","circus schools","college counseling","colleges & universities","cooking schools","cosmetology schools","cpr classes","dance schools","driving schools","education","educational services","elementary schools","first aid classes","flight instruction","language schools","massage schools","middle schools & high schools","preschools","private schools","religious schools","special education","vocational & technical school"}; 
			groupMembers = addMyMembers(toAdd, "education", educMap);
		}
		//down 1
		else if(groupId.equals("Event Planning & Services")) {
			String[] toAdd = {"bartenders","boat charters","caterers","clowns","djs","event photography","event planning & services","magicians","musicians","officiants","party & event planning","party bus rentals","party equipment rentals","party supplies","personal chefs","photographers","session photography","venues & event spaces","videographers"}; 
			groupMembers = addMyMembers(toAdd,"event planning & services", eventMap);
		}
		//food down 3 
		else if(groupId.equals("Food")) {
			String[] toAdd = {"bagels","bakeries","beverage store","bubble tea","butcher","candy stores","cheese shops","chocolatiers & shops","churros","coffee & tea","convenience stores","csa","delicatessen","desserts","do-it-yourself food","donairs","donuts","ethic grocery","ethnic food","farmers market","food","food delivery services","food trucks","friterie","fruits & veggies","gelato","grocery","health markets","herbs & spices","ice cream & frozen yogurt","internet cafes","juice bars & smoothies","meat shops","mulled wine","organic stores","patisserie/cake shop","pretzels","seafood markets","shaved ice","specialty food","street vendors","tea rooms","zapiekanka"}; 
			groupMembers = addMyMembers(toAdd, "food", foodMap);
		}
		else if(groupId.equals("Home Services")) {
			String[] toAdd = {"building supplies","carpenters","carpet installation","carpeting","contractors","electricians","flooring","garage door services","gardeners","handyman","heating & air conditioning/hvac","home cleaning","home inspectors","home organization","home services","home theatre installation","home window tinting","interior design","internet service providers","irrigation","keys & locksmiths","landscape architects","landscaping","lighting fixtures & equipment","masonry/concrete","movers","painters","plumbing","pool cleaners"}; 
			groupMembers = addMyMembers(toAdd, "home services", homeMap);
		}
		else if(groupId.equals("Hotels & Travel")) {
			String[] toAdd = {"airlines","airport shuttles","airports","bed & breakfast","campgrounds","car rental","dolmus station","ferries","guest houses","hostels","hotels","hotels & travel","limos","motorcycle rental","public transportation","resorts","rv parks","rv rental","ski resorts","taxis","tours","train stations","transportation","travel services","vacation rental agents","vacation rentals","water taxis"}; 
			groupMembers = addMyMembers(toAdd, "hotels & travel", hotelMap);
		}
		else if(groupId.equals("Local Flavor")) {
			String[] toAdd = {"local flavor"};
			groupMembers = addMyMembers(toAdd, "local flavor", flavorMap);
		}
		else if(groupId.equals("Local Services")) {
			String[] toAdd = {"appliances & repair","bail bondsmen","bike repair/maintenance","carpet cleaning","child care & day care","community service/non-profit","couriers & delivery services","data recovery","dry cleaning & laundry","electronics repair","funeral services & cemeteries","furniture reupholstery","it services & computer repair","jewelry repair","junk removal & hauling","local services","mobile phone repair","notaries","pest control","printing services","record labels","recording & rehearsal studios","recycling center","screen printing","screen printing/t-shirt printing","self storage","sewing & alterations","shipping centers","shoe repair","snow removal","watch repair","youth club"}; 
			groupMembers = addMyMembers(toAdd, "local services", localMap);
		}
		else if(groupId.equals("Mass Media")) {
			String[] toAdd = {"mass media","print media","radio stations","television stations"}; 
			groupMembers = addMyMembers(toAdd, "mass media", massMap);
		}
		else if(groupId.equals("Pets")) {
			String[] toAdd = {"animal shelters","dog walkers","horse boarding","pet boarding/pet sitting","pet groomers","pet services","pet stores","pet training","pets","veterinarians"};
			groupMembers = addMyMembers(toAdd, "pets", petsMap);
		}
		else if(groupId.equals("Professional Services")) {
			String[] toAdd = {"accountants","advertising","architects","bankruptcy law","boat repair","business law","career counseling","criminal defense law","divorce & family law","dui law","editorial services","employment agencies","employment law","estate planning law","graphic design","immigration law","life coach","marketing","matchmakers","office cleaning","personal assistants","personal injury law","private investigation","professional services","public relations","real estate law","security services","talent agencies","taxidermy","translation services","video/film production","web design"}; 
			groupMembers = addMyMembers(toAdd, "professional services", professionalMap);
		}
		//down 2
		else if(groupId.equals("Public Services & Government")) {
			String[] toAdd = {"authorized postal representative","community centers","courthouses","embassy","fire departments","landmarks & historical buildings","libraries","post offices","public services & government","registry office","tax office"}; 
			groupMembers = addMyMembers(toAdd, "public services & government", publicMap);
		}
		//down 1
		else if(groupId.equals("Real Estate")) {
			String[] toAdd = {"apartments","commercial real estate","home staging","mortgage brokers","property management","real estate","real estate agents","real estate services","roofing","shades & blinds","shared office spaces","solar installation","television service providers","tree services","university housing","utilities","window washing","windows installation"}; 
			groupMembers = addMyMembers(toAdd, "real estate", realMap);
		}
		else if(groupId.equals("Restaurants")) {
			String[] toAdd = {"afghan","african","altoatesine","american","apulian","arabian","argentine","armenian","arroceria / paella","asian fusion","asturian","australian","austrian","baden","baguettes","bangladeshi","barbeque","basque","bavarian","beer garden","beer hall","belgian","bistros","black sea","brasseries","brazilian","breakfast & brunch","british","buffets","bulgarian","burgers","burmese","cafes","cafeteria","cajun/creole","calabrian","cambodian","canadian","canteen","cantonese","caribbean","catalan","chech","chee kufta","cheesesteaks","chicken shop","chicken wings","chinese","colombian","comfort food","corsican","creperies","cuban","cucina campana","curry sausage","cypriot","czech/slovakian","danish","delis","dim sum","diners","dominican","eastern european","eastern german","egyptian","emilian","ethiopian","fast food","filipino","fish & chips","fondue","food court","food stands","french","french southwest","friulan","fuzhou","galician","gastropubs","georgian","german","giblets","gluten-free","gozleme","greek","haitian","hakka","halal","hawaiian","henghwa","hessian","himalayan/nepalese","hokkien","hot dogs","hot pot","hungarian","iberian","indian","indonesian","international","irish","island pub","israeli","italian","izakaya","japanese","jewish","kebab","korean","kosher","kurdish","laos","laotian","latin american","lebanese","ligurian","live/raw food","lumbard","lyonnais","malaysian","mamak","meatballs","mediterranean","mexican","middle eastern","milk bars","modern australian","modern european","mongolian","moroccan","new zealand","night food","northern german","nyonya","open sandwiches","oriental","pakistani","palatine","parent cafes","parma","persian/iranian","peruvian","pierogis","pita","pizza","polish","portuguese","potatoes","poutineries","pub food","puerto rican","ramen","restaurants","rhinelandian","rice","roman","romanian","rotisserie chicken","rumanian","russian","salad","salvadoran","sandwiches","sardinian","scandinavian","scottish","seafood","senegalese","serbo croatian","shanghainese","sicilian","signature cuisine","singaporean","soul food","soup","south african","southern","spanish","steakhouses","sushi bars","swabian","swedish","swiss food","szechuan","tabernas","taiwanese","tapas bars","tapas/small plates","teochew","teppanyaki","tex-mex","thai","traditional norwegian","traditional swedish","trinidadian","turkish","turkish ravioli","tuscan","ukrainian","vegan","vegetarian","venetian","venezuelan","venison","vietnamese","wok","wraps","yugoslav"}; 
			groupMembers = addMyMembers(toAdd, "restaurants", restMap);
		}
		//down 3
		else {
			String[] toAdd = {"accessories","antiques","appliances","art supplies","arts & crafts","auction houses","baby gear & furniture","bespoke clothing","bikes","books, mags, music & video","bookstores","cards & stationery","children's clothing","chinese bazaar","comic books","computers","concept shops","costumes","department stores","discount store","drugstores","electronics","embroidery & crochet","eyewear & opticians","fabric stores","fashion","fireworks","flea markets","florists","flowers","flowers & gifts","formal wear","framing","furniture stores","gift shops","golf equipment","golf equipment shops","guns & ammo","hardware stores","hats","hobby shops","home & garden","home decor","hot tub & pool","jewelry","kiosk","kitchen & bath","knitting supplies","leather goods","linens","luggage","market stalls","mattresses","medical supplies","men's clothing","mobile phones","motorcycle gear","music & dvds","musical instruments & teachers","newspapers & magazines","nurseries & gardening","office equipment","outdoor gear","outlet stores","personal shopping","photography stores & services","plus size fashion","pop-up shops","scandinavian design","shoe stores","shopping","shopping centers","sleepwear","souvenir shops","spiritual shop","sporting goods","sports wear","surf shop","swimwear","tableware","thrift stores","tickets","tobacco shops","toy stores","trophy shops","uniforms","used bookstore","used, vintage & consignment","videos & video game rental","vinyl records","watches","wholesale stores","wigs","women's clothing"}; 
			groupMembers = addMyMembers(toAdd, "shopping", shopMap);
		}
		return groupMembers;
	}

	private ArrayList<Item> addMyMembers(String[] childrenArray, String groupName, HashMap<String, Integer> groupMap) {
		ArrayList<Item> groupMembers = new ArrayList<Item>();
		for(int i=0; i<childrenArray.length; i++) {
			Item item = new Item();
			item.name = childrenArray[i];

			allStrings.put(childrenArray[i], groupName);
			//Build inner HashMap
			groupMap.put(childrenArray[i], i);

			item.id = new Integer(i).toString();
			groupMembers.add(item);

			//Add all elements to groupChildPositionMap
			groupChildPosition.put(groupName, groupMap);
		}
		return groupMembers; 
	}
}
