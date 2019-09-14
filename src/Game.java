import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;

/**
 * A class for the main aspects of the game.
 * @author Peta Douglas
 * @author Marianne Trye
 *
 */
public class Game {

	List<Player> players = new ArrayList<Player>();	
	List<Weapon> weaponCards;
	List<Suspect> suspectCards;
	List<Room> roomCards;
	List<Room> rooms;
	HashMap<String,Room> mapped=new HashMap<String,Room> ();
	HashMap<Room,Integer> numMapped=new HashMap<Room,Integer> ();
	HashMap<Integer,Room> roomMapped=new HashMap<Integer,Room> ();
	HashMap<Integer, Suspect> murderMapped=new HashMap<Integer, Suspect> ();
	HashMap<Integer, Weapon> weaponMapped=new HashMap<Integer, Weapon> ();
	HashMap<Player, Room> previousRoom=new HashMap<Player, Room> ();
	Stack<Card> deck;
	Card envelope[] = new Card[3];
	Board board = new Board();
	boolean gameWon = false;
	String murderer;
	String weapon;

	/**
	 * Sets everything up and calls starts the game
	 */
	public void setup() {

		getDeck();
		getPlayers();
		deal();
		board.board(players);
		play();
	}
	/**
	 * The actual gameplay. 
	 * Each player has a turn unless they have made an incorrect accusation had therefore lost.
	 * Player chooses where to move based on dice result.
	 * If They're in a room, they can make a suggestion. Other players will show them 1 card inluding players who have already lost.
	 * Player can choose whether they then want to make an accusation.
	 * If they are right they win, if they're wrong they  and it becomes the next player's turn.
	 * Stops when game is won.lose
	 */
	public void play() {
		Scanner reader = new Scanner(System.in);

		while (gameWon == false) {
			int i = 0;
			while(i<players.size()) {	//each player's turn
				Player player = players.get(i);
				if(player.hasLost == true) {
					i++;
				}
				else {
					System.out.println(player.name +"'s turn:");
					System.out.println("Pass to "+player.name+" and press 1" );
					int o;
					while(true) {
						o = reader.nextInt();
						if(o==1)
							break;
					}
					board.printBoard(players);
					System.out.println("Your hand:");
					for (Card c : player.getHand()) {
						System.out.print(c.name + " / ");
					}
					System.out.println("");
					int roll = rollDice();
					System.out.println("You rolled a "+ roll);
					move(player, roll);
					if(getRoom(player.posX, player.posY) == null) {	//not in room, therefore next player's turn
						i++;
					}else {	//inside a room, gets to make a suggestion
						makeSuggestion(player, getRoom(player.posX, player.posY));
						System.out.println();
						System.out.println("Make your final accusation? Y/N");
						String ans = reader.next();
						if (ans.equalsIgnoreCase("Y")) {	//choose which suspect, weapon, and room to accuse
							int n;
							while(true) {
								System.out.println("Press 1: Mrs White, 2: Miss Scarlett, 3: Professor Plum, 4: Colonel Mustard, 5: Mr Green, 6: Mrs. Peacock"); 	
								n=reader.nextInt();
								if(n>0&&n<7)
									break;
							}
							Suspect suspect=murderMapped.get(n);
							while(true) {
								System.out.println("Press 1: CandleStick, 2: Lead Pipe, 3: Revolver, 4: Knife, 5: Wrench, 6: Rope");
								n=reader.nextInt();
								if(n>0&&n<7)
									break;
							}
							Weapon weap=weaponMapped.get(n);
							while(true) {
								System.out.println("Press 1: lounge, 2: hall, 3: study, 4: library, 5: billards room, 6: conservatory,  7: ballroom, 8: kitchen, 9: dining room");
								n=reader.nextInt();
								if(n>0&&n<10)
									break;
							}
							Room room=roomMapped.get(n);
							accuse(player, suspect.name, weap.name,room.name);
							if(gameWon) {
								System.out.println("Game is won!");
								break;
							}
							else {
								System.out.println("\n");
								i++;
							}
						}
						else {
							i++;
						}
					}

					System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
				}
			}
		}
	}
	/**
	 * User inputs where they want to go. If they try and move too far, 
	 * they have to choose again unill they've chosen a position they can reach.
	 * 
	 * @param player 	The current player whose move it is
	 * @param roll 		The dice result, max number of spaces they can move
	 */
	private void move(Player player, int roll) {
		int row=player.posX;
		int column=player.posY;
		Scanner reader = new Scanner(System.in);
		Room currentRoom=getRoom(player.posX, player.posY);
		while(true) {
			System.out.println("Would you like to go in a room? (Y or N)");
			String yorn = reader.next();
			if(yorn.equalsIgnoreCase("Y")||yorn.equalsIgnoreCase("Yes")) {
				int roomNum;
				while(true) {
					System.out.println("Press 1: lounge, 2: hall, 3: study, 4: library, 5: billards room, 6: conservatory,  7: ballroom, 8: kitchen, 9: dining room");
					roomNum=reader.nextInt();
					if(roomNum>0&&roomNum<10)
						break;
				}
				Room goToR=roomMapped.get(roomNum);
				if(previousRoom.get(player)!=null) {

					if(roomMapped.get(roomNum).name==previousRoom.get(player).name) {
						System.out.println("You tried to visit the room you were just in");
					}
				}
				else {	//put player in the room
					List<QItem> doors=getDoors(roomNum);
					boolean vm=false;
					if(currentRoom==null) {
						for(QItem d: doors) {
							int validMove = board.minDistance(player.posX, player.posY, d.row, d.col);
							if(validMove < roll && validMove!=-1) {
								previousRoom.put(player, goToR);
								vm=true;
								QItem a=placeinRoom(roomNum,player);
								row=a.row;
								column=a.col;
								break;
							}
						}
					}
					else {
						//player is already in the room
						Integer cRoom=numMapped.get(currentRoom);
						List<QItem> cDoors=getDoors(cRoom);
						for(QItem c: cDoors) {
							for(QItem d: doors) {
								int validMove = board.minDistance(c.row, c.col, d.row, d.col);

								if(validMove!=-1 && validMove < roll-1) {
									previousRoom.put(player, goToR);
									vm=true;
									QItem a=placeinRoom(roomNum,player);
									row=a.row;
									column=a.col;
									break;
								}
							}
							if(vm)
								break;
						}
					}
					if(vm)
						break;
					else
						System.out.println("Sorry, you could not get to that room");
				}

			}
			else {
				//Player must stay in corridor (not in any room). Chooses what position they want to move to
				while(true) {
					System.out.println("Where would you like to go? E.g G *press enter* 8 *press enter*");
					String col = reader.next();
					row = reader.nextInt();
					if(isInteger(col)) {
						column=Integer.parseInt(col);
					}
					else {
						column = colToInt(col);
					}
					if(row<26 && column<25 && column>0 && column>0) {
						break;
					}
				}
				if(currentRoom==null) {
					//boolean validMove = roll>board.minDistance(board.board, player.posX, player.posY, row, column);
					int validMove = board.minDistance(player.posX, player.posY, row, column);
					//System.out.println(row+" "+ column);
					if(validMove > roll) {
						System.out.println("Move is greater than dice roll. Try again.");
					}
					else if(!(board.board[row][column]==(null)||board.board[row][column]==("_"))){
						System.out.println("Move was not to a valid square.");
					}
					else if(!(getRoom(row,column)==null)) {
						System.out.println("You tried to go to the "+getRoom(row,column).name);
					}

					else {
						break;
					}
				}
				else {

					Integer cRoom=numMapped.get(currentRoom);
					List<QItem> cDoors=getDoors(cRoom);
					boolean vm=false;
					for(QItem c: cDoors) {
						//boolean validMove = roll>board.minDistance(board.board, player.posX, player.posY, row, column);
						int validMove = board.minDistance(c.row, c.col, row, column);

						if((validMove > roll) || (!(board.board[row][column]==(null)||board.board[row][column]==("_"))) || (!(getRoom(row,column)==null))) {
						}
						else {
							vm=true;
							break;
						}
					}
					if(vm)
						break;
					else
						System.out.println("Invalid move from "+currentRoom.name);
				}
			}
		}
		board.board[player.posX][player.posY]="_";
		player.posX = row;
		player.posY = column;

	}

	/**
	 * Checks if input is an integer 
	 * @param s		The string input that we are checking is an int
	 * @return		Boolean stating if it's an int or not
	 */
	public static boolean isInteger(String s) {
		boolean vi=false;
		try{
			Integer.parseInt(s);
			vi = true;
		}
		catch (NumberFormatException ex)
		{
			// s is not an integer
		}

		return vi;
	}

	/**
	 * Finds which squares make up each room. If null, they character isn't in a room.
	 * @param row 	X position for 2D array
	 * @param col 	Y position for 2D array
	 * @return Room that player is in
	 */
	private Room getRoom(int row, int col) {
		//Hall
		for(int i=1; i<7; i++){
			for(int j=11; j<15; j++){
				if(row==i&&col==j) {
					return mapped.get("Hall");
				}
			}
		}

		//Lounge
		for(int i=1; i<6; i++){
			for(int j=1; j<7; j++){
				if(row==i&&col==j) {
					return mapped.get("Lounge");
				}
			}
		}
		//Study
		for(int i=1; i<4; i++){
			for(int j=19; j<24; j++){
				if(row==i&&col==j) {
					return mapped.get("Study");
				}
			}
		}


		//Library
		for(int i=8; i<11; i++){
			for(int j=19; j<24; j++){
				if(row==i&&col==j) {
					return mapped.get("Library");
				}
			}
		}

		//Billards Room
		for(int i=14; i<17; i++){
			for(int j=20; j<24; j++){
				if(row==i&&col==j) {
					return mapped.get("Billiards Room");
				}
			}
		}

		//conservatory room
		for(int i=21; i<24; i++){
			for(int j=20; j<24; j++){
				if(row==i&&col==j) {
					return mapped.get("Conservatory");
				}
			}
		}
		//ballroom
		for(int i=19; i<24; i++){
			for(int j=10; j<16; j++){
				if(row==i&&col==j) {
					return mapped.get("Ballroom");
				}
			}
		}

		//kitchen
		for(int i=20; i<24; i++){
			for(int j=1; j<6; j++){
				if(row==i&&col==j) {
					return mapped.get("Kitchen");
				}
			}
		}

		//Dining Room
		for(int i=11; i<16; i++){
			for(int j=1; j<8; j++){
				if(row==i&&col==j) {
					return mapped.get("Dining Room");
				}
			}
		}
		return null;

	}
	//Press 1: lounge, 2: hall, 3: study, 4: library, 5: billards room, 6: conservatory,  7: ballroom, 8: kitchen, 9: dining room");
	private QItem placeinRoom(Integer room,Player player) {
		int x=0;
		int y=0;
		if(room==1) {//Lounge
			for(int i=1; i<6; i++){
				for(int j=1; j<7; j++){
					if(board.board[i][j]==null||board.board[i][j]=="_") {
						x=i;
						y=j;
					}
				}
			}
		}
		else if(room==2) {//Hall
			for(int i=1; i<7; i++){
				for(int j=11; j<15; j++){
					if(board.board[i][j]==null||board.board[i][j]=="_") {
						x=i;
						y=j;
					}
				}
			}
		}
		else if(room==3) {//Study
			for(int i=1; i<4; i++){
				for(int j=19; j<24; j++){
					if(board.board[i][j]==null||board.board[i][j]=="_") {
						x=i;
						y=j;
					}
				}
			}
		}
		else if(room==4) {//Library
			for(int i=8; i<11; i++){
				for(int j=19; j<24; j++){
					if(board.board[i][j]==null||board.board[i][j]=="_") {
						x=i;
						y=j;
					}
				}
			}
		}
		else if(room==5) {//Billards Room
			for(int i=14; i<17; i++){
				for(int j=20; j<24; j++){
					if(board.board[i][j]==null||board.board[i][j]=="_") {
						x=i;
						y=j;
					}
				}
			}
		}
		else if(room==6) {//Conservatory
			for(int i=21; i<24; i++){
				for(int j=20; j<24; j++){
					if(board.board[i][j]==null||board.board[i][j]=="_") {
						x=i;
						y=j;
					}
				}
			}
		}
		else if(room==7) {//Ball Room
			for(int i=19; i<24; i++){
				for(int j=10; j<16; j++){
					if(board.board[i][j]==null||board.board[i][j]=="_") {
						x=i;
						y=j;
					}
				}
			}
		}
		else if(room==8) {//Kitchen
			for(int i=20; i<24; i++){
				for(int j=1; j<6; j++){
					if(board.board[i][j]==null||board.board[i][j]=="_") {
						x=i;
						y=j;
					}
				}
			}
		}
		else if(room==9) {//Dining Room
			for(int i=11; i<16; i++){
				for(int j=1; j<8; j++){
					if(board.board[i][j]==null||board.board[i][j]=="_") {
						x=i;
						y=j;
					}
				}
			}
		}


		return new QItem(x,y);
	}


	/**
	 * Finds all the doors x and y locations in a room
	 * @param room 	The room that we want to find the doors for
	 * @return List of the doors in the room
	 */
	public List<QItem> getDoors(int room) {
		//this will return all the doors square before
		List<QItem> a=new ArrayList<QItem>();
		if(room==1) {//Lounge
			QItem door=new QItem(7,7);//67
			a.add(door);
			return a;
		}
		else if(room==2) {//Hall

			QItem door=new QItem(8,12);//712
			a.add(door);
			door=new QItem(8,13);//713
			a.add(door);
			door=new QItem(5,16);//515
			a.add(door);
			return a;
		}
		else if(room==3) {//Study
			QItem door=new QItem(5,18);//418
			a.add(door);
			return a;
		}
		else if(room==4) {//Library
			QItem door=new QItem(9,17);//918
			a.add(door);
			door=new QItem(12,21);//1121
			a.add(door);
			return a;
		}
		else if(room==5) {//Billards Room
			QItem door=new QItem(12,23);//1323
			a.add(door);
			door=new QItem(16,18);//1619
			a.add(door);
			return a;
		}
		else if(room==6) {//Conservatory
			QItem door=new QItem(20,19);//2119
			a.add(door);
			return a;
		}
		else if(room==7) {//Ball Room
			QItem door=new QItem(17,10);//1810
			a.add(door);
			door=new QItem(17,15);//1815
			a.add(door);
			door=new QItem(20,8);//209
			a.add(door);
			door=new QItem(20,17);//2016
			a.add(door);
			return a;
		}
		else if(room==8) {//Kitchen
			QItem door=new QItem(18,5);//195
			a.add(door);
			return a;
		}
		else if(room==9) {//Dining Room
			QItem door=new QItem(9,7);//107
			a.add(door);
			door=new QItem(13,9);//138
			a.add(door);
			return a;
		}
		return null;
	}

	/**
	 * Fills each list with it's respective cards.
	 * Selects the murderer, crime scene location and murder weapon.
	 * Creates the deck.
	 */
	public void getDeck() {
		weaponCards = new ArrayList<Weapon>();
		suspectCards = new ArrayList<Suspect>();
		roomCards = new ArrayList<Room>();
		deck = new Stack<Card>();

		//add weapons
		weaponCards.add(new Weapon("Candlestick"));
		weaponCards.add(new Weapon("Lead Pipe"));
		weaponCards.add(new Weapon("Revolver"));
		weaponCards.add(new Weapon("Knife"));
		weaponCards.add(new Weapon("Wrench"));
		weaponCards.add(new Weapon("Rope"));

		//Candle
		weaponMapped.put(1,weaponCards.get(0));
		//Pipe
		weaponMapped.put(2,weaponCards.get(1));
		//Revolver
		weaponMapped.put(3,weaponCards.get(2));
		//Knife
		weaponMapped.put(4,weaponCards.get(3));
		//Wrench
		weaponMapped.put(5,weaponCards.get(4));
		//Rope
		weaponMapped.put(6,weaponCards.get(5));


		//add suspects
		suspectCards.add(new Suspect("Mrs. White"));
		suspectCards.add(new Suspect("Miss Scarlett"));
		suspectCards.add(new Suspect("Professor Plum"));
		suspectCards.add(new Suspect("Colonel Mustard"));
		suspectCards.add(new Suspect("Mr. Green"));
		suspectCards.add(new Suspect("Mrs. Peacock"));


		//Mrs W
		murderMapped.put(1,suspectCards.get(0));
		// Ms S
		murderMapped.put(2,suspectCards.get(1));
		//Prof P
		murderMapped.put(3,suspectCards.get(2));
		//Col M
		murderMapped.put(4,suspectCards.get(3));
		//Mr G
		murderMapped.put(5,suspectCards.get(4));
		//Mrs P
		murderMapped.put(6,suspectCards.get(5));

		//add rooms
		roomCards.add(new Room("Kitchen"));
		roomCards.add(new Room("Conservatory"));
		roomCards.add(new Room("Study"));
		roomCards.add(new Room("Billiards Room"));
		roomCards.add(new Room("Hall"));
		roomCards.add(new Room("Dining Room"));
		roomCards.add(new Room("Ballroom"));
		roomCards.add(new Room("Lounge"));
		roomCards.add(new Room("Library"));

		mapped.put("Kitchen",roomCards.get(0));
		mapped.put("Conservatory",roomCards.get(1));
		mapped.put("Study",roomCards.get(2));
		mapped.put("Billiards Room",roomCards.get(3));
		mapped.put("Hall",roomCards.get(4));
		mapped.put("Dining Room",roomCards.get(5));
		mapped.put("Ballroom",roomCards.get(6));
		mapped.put("Lounge",roomCards.get(7));
		mapped.put("Library",roomCards.get(8));



		rooms = new ArrayList<>(roomCards);

		//(room==1) {//Lounge
		//(room==2) {//Hall
		//(room==3) {//Study
		//(room==4) {//Library
		//(room==5) {//Billards Room
		//(room==6) {//Conservatory
		//(room==7) {//Ball Room
		//(room==8) {//Kitchen
		//(room==9) {//Dining Room
		numMapped.put(roomCards.get(0),8);
		numMapped.put(roomCards.get(1),6);
		numMapped.put(roomCards.get(2),3);
		numMapped.put(roomCards.get(3),5);
		numMapped.put(roomCards.get(4),2);
		numMapped.put(roomCards.get(5),9);
		numMapped.put(roomCards.get(6),7);
		numMapped.put(roomCards.get(7),1);
		numMapped.put(roomCards.get(8),4);


		roomMapped.put(8,roomCards.get(0));
		roomMapped.put(6,roomCards.get(1));
		roomMapped.put(3,roomCards.get(2));
		roomMapped.put(5,roomCards.get(3));
		roomMapped.put(2,roomCards.get(4));
		roomMapped.put(9,roomCards.get(5));
		roomMapped.put(7,roomCards.get(6));
		roomMapped.put(1,roomCards.get(7));
		roomMapped.put(4,roomCards.get(8));

		//shuffle cards
		Collections.shuffle(weaponCards);
		Collections.shuffle(suspectCards);
		Collections.shuffle(roomCards);

		//assign finals
		Room crimeScene = roomCards.get(roomCards.size()-1);
		Weapon murderWeapon = weaponCards.get(weaponCards.size()-1);
		Suspect murderer = suspectCards.get(suspectCards.size()-1);

		//remove finals from cards
		weaponCards.remove(weaponCards.size()-1);
		suspectCards.remove(suspectCards.size()-1);
		roomCards.remove(roomCards.size()-1);

		//add finals to envelope
		envelope = new Card[] {murderer, murderWeapon, crimeScene};

		//create the deck
		for(Suspect s : suspectCards) {
			deck.push(s);
		}
		for(Weapon w : weaponCards) {
			deck.push(w);
		}
		for(Room r : roomCards) {
			deck.push(r);
		}

		//shuffle the deck
		Collections.shuffle(deck);
	}

	/**
	 * Deals the cards to each player.
	 */
	private void deal() {
		boolean a=false;
		while (!deck.isEmpty()) {
			for(Player player : players) {
				player.getHand().add(deck.pop());
				if(deck.isEmpty())
					a=true;
				if(a)
					break;
			}
		}
	}


	/**
	 * Finds the number of players.
	 * Players choose what character they want to be.
	 * Player is added to the list of Players.
	 */
	private void getPlayers() {
		String character = null;
		String token = null;
		int posX =0;
		int posY =0;
		Scanner reader = new Scanner(System.in);
		int n;

		while(true) {	//number of players
			System.out.println("How many players (3 - 6)");
			n = reader.nextInt();
			if( n<3 || n>6){
				System.out.println("Invalid # of players");
			}
			else {
				break;
			}
		}
		String[] characters = {null, "1: Mrs. White", "2: Miss Scarlett", "3: Colonel Mustard", 
				"4: Professor Plum", "5: Mrs. Peacock", "6: Mr. Green"};

		for (int i = 1; i<=n; i++) {	//print available characters
			System.out.println();
			System.out.print("Characters: ");
			for(String c : characters) {
				if(c != null) {
					System.out.print(c+"  ");
				}
			}
			int c;

			while(true){	//chooses their character
				System.out.println();
				System.out.println("Player "+ i + ", choose a character(#)");
				c = reader.nextInt();
				if(c<1 || c>6) {
					System.out.println("Not a valid character");
				}
				else if((c>=1 || c<=6) && characters[c] == null) {
					System.out.println("Not a valid character");
				}
				else {
					break;
				}
			}
			//assign character and first positions
			if( c == 1) {
				character = "Mrs. White";
				characters[1] = null;
				token = "W";
				posX = 25;
				posY = 10;
			}else if(c == 2) {
				character = "Miss Scarlett";
				characters[2] = null;
				token = "S";
				posX = 1;
				posY = 8;
			}else if(c == 3) {
				character = "Colonel Mustard";
				characters[3] = null;
				token = "M";
				posX = 8;
				posY = 1;
			}else if(c == 4) {
				character = "Professor Plum";
				characters[4] = null;
				token = "P";
				posX = 6;
				posY = 24;
			}else if(c == 5) {
				character = "Mrs. Peacock";
				characters[5] = null;
				token = "C";
				posX = 19;
				posY = 24;
			}else if(c == 6) {
				character = "Mr. Green";
				characters[6] = null;
				token = "G";
				posX = 25;
				posY = 16;
			}
			System.out.println("Player "+ i + " is "+ character+". Your token is "+token+".");
			players.add(new Player(character, token, posX, posY));
		}
		//the room player was in last
		for(Player p:players) {
			previousRoom.put(p,null);
		}

	}

	/**
	 * Rolls the dice.
	 * @return result	A number between 2 and 12 (Possibilities from 2 dice).
	 */
	public int rollDice() {
		int result = 2 + (int)(Math.random() * 11);
		return result;
	}

	/**
	 * Converts column letter to a usable number.
	 * @param col	The letter of the column
	 * @return 		The actual number of the column
	 */
	private int colToInt(String col) {
		if(col.equalsIgnoreCase("A")) {
			return 1;
		}else if(col.equalsIgnoreCase("B")) {
			return 2;
		}else if(col.equalsIgnoreCase("C")) {
			return 3;
		}else if(col.equalsIgnoreCase("D")) {
			return 4;
		}else if(col.equalsIgnoreCase("E")) {
			return 5;
		}else if(col.equalsIgnoreCase("F")) {
			return 6;
		}else if(col.equalsIgnoreCase("G")) {
			return 7;
		}else if(col.equalsIgnoreCase("H")) {
			return 8;
		}else if(col.equalsIgnoreCase("I")) {
			return 9;
		}else if(col.equalsIgnoreCase("J")) {
			return 10;
		}else if(col.equalsIgnoreCase("K")) {
			return 11;
		}else if(col.equalsIgnoreCase("L")) {
			return 12;
		}else if(col.equalsIgnoreCase("M")) {
			return 13;
		}else if(col.equalsIgnoreCase("N")) {
			return 14;
		}else if(col.equalsIgnoreCase("O")) {
			return 15;
		}else if(col.equalsIgnoreCase("P")) {
			return 16;
		}else if(col.equalsIgnoreCase("Q")) {
			return 17;
		}else if(col.equalsIgnoreCase("R")) {
			return 18;
		}else if(col.equalsIgnoreCase("S")) {
			return 19;
		}else if(col.equalsIgnoreCase("T")) {
			return 20;
		}else if(col.equalsIgnoreCase("U")) {
			return 21;
		}else if(col.equalsIgnoreCase("V")) {
			return 22;
		}else if(col.equalsIgnoreCase("W")) {
			return 23;
		}else if(col.equalsIgnoreCase("X")) {
			return 24;
		}
		return 0;
	}

	/**
	 * Player can make a suggestion on who, how, and where the victim was killed.
	 * @param player	The current player making the suggestion
	 * @param room		The room the player is in (as they can only suggest a room they are in)
	 */
	private void makeSuggestion(Player player, Room room) {
		board.printBoard(players);
		System.out.println();
		System.out.println("Make a suggestion");
		System.out.println("Your hand:");
		for (Card c : player.getHand()) {
			System.out.print(c.name + " / ");
		}
		Scanner reader = new Scanner(System.in);
		System.out.println();
		System.out.println();
		System.out.println("Suggest a Murderer");
		int n;
		while(true) {
			System.out.println("Press 1: Mrs White, 2: Miss Scarlett, 3: Professor Plum, 4: Colonel Mustard, 5: Mr Green, 6: Mrs. Peacock");
			n = reader.nextInt();
			if( n<1 || n>6){
				System.out.println("Invalid player");
			}
			else {
				break;
			}
		}
		murderer=murderMapped.get(n).name;
		for(Player p: players) {
			if(p.name==murderer) {
				QItem a=placeinRoom(numMapped.get(room), p);

				board.board[p.posX][p.posY]="_";
				p.posX = a.row;
				p.posY = a.col;
				break;

			}
		}
		System.out.println();
		System.out.println("Suggest a Weapon");

		while(true) {
			System.out.println("Press 1: CandleStick, 2: Lead Pipe, 3: Revolver, 4: Knife, 5: Wrench, 6: Rope");
			n = reader.nextInt();
			if( n<1 || n>6){
				System.out.println("Invalid weapon");
			}
			else {
				break;
			}
		}
		weapon=weaponMapped.get(n).name;
		System.out.println("I suggest it was "+murderer+", with the "+weapon+", in the "+room.name+".");
		int start=0;
		int count=0;
		for(Player p : players) {
			if(p==player){
				start=count;
			}
			count++;
		}
		int index=0;
		boolean showedIt=false;
		while(index<count-1) {
			start++;
			if(start==count)
				start=0;
			Player p=players.get(start);
			ArrayList<Card> cardsToShow=new ArrayList<Card>();
			Card toShow=null;
			if(!p.equals(player)) {
				System.out.println("\n\n\n\n\n\n\n");
				board.printBoard(players);
				System.out.println("Pass to "+p.name+" and press 1 when done\n");
				while(true) {
					n = reader.nextInt();
					if(n==1)
						break;
				}
				//passed to next player to show a card if they have it
				System.out.println(player.name+" suggested it was "+murderer+", with the "+weapon+", in the "+room.name+".");

				System.out.println("Your hand:");
				for (Card c : p.getHand()) {
					System.out.print(c.name + " / ");
				}
				System.out.println();
				for(Card c : p.getHand()) {
					if(c.name.equals(murderer) || c.name.equals(weapon) || c.name.equals(room.name)) {
						cardsToShow.add(c);
					}
				}
				if(cardsToShow.isEmpty()) {
					System.out.println("You have no cards to show");
				}
				else {
					showedIt=true;
					int i=0;
					for(Card c: cardsToShow) {
						i++;
						System.out.println("Press "+i+" to show "+c.name);
					}
					while(true) {
						n = reader.nextInt();
						if(n>0 && n<=i)
							break;
					}
					toShow=cardsToShow.get(n-1);
					System.out.println("\n\n\n\n\n\n\n");
					board.printBoard(players);
					System.out.println("Pass to "+player.name+" and press 1 when done\n");
					while(true) {
						n = reader.nextInt();
						if(n==1)
							break;
					}
					System.out.println(p.name+" showed you "+toShow.name);

					break;

				}

			}
			index++;
		}
		if(!showedIt) {
			System.out.println("\n\n\n\n\n\n\n");
			board.printBoard(players);
			System.out.println("Pass to "+player.name+" and press 1 when done\n");
			while(true) {
				n = reader.nextInt();
				if(n==1)
					break;
			}
		}

	}

	/**
	 * Make a final accusation for who, how and where the victim was killed. 
	 * If they are right, the player wins the game. If wrong, they lose.
	 * @param player		The current Player
	 * @param murderer		Suspect they are accusing
	 * @param murderWeapon	Weapon they are accusing
	 * @param crimeScene	Location they are accusing
	 */
	public void accuse(Player player, String murderer, String murderWeapon, String crimeScene) {
		if (murderer.equals(envelope[0].name) && murderWeapon.equals(envelope[1].name)
				&& crimeScene.equals(envelope[2].name)) {
			System.out.println("Correct! The murder was committed by "+murderer+", with the "
					+murderWeapon+", in the "+crimeScene+".");
			playerWins(player);
		}
		else {
			System.out.println("Incorrect. You Lose.");
			playerLoses(player);
		}
	}

	/**
	 * Sets gameWon to true. Prints the winner.
	 * @param player	Current player
	 */
	private void playerWins(Player player) {
		gameWon = true;
		System.out.println(player.name+" has won!");
	}

	/**
	 * Sets hasLost to true.
	 * @param player	Current player
	 */
	private void playerLoses(Player player) {
		player.hasLost = true;
	}

	public static void main(String[] args) {
		Game g = new Game();
		g.setup();
	}
}

