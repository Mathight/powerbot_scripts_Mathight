import org.powerbot.concurrent.Task;
import org.powerbot.concurrent.strategy.Condition;
import org.powerbot.concurrent.strategy.Strategy;
import org.powerbot.game.api.ActiveScript;
import org.powerbot.game.api.Manifest;
import org.powerbot.game.api.methods.Calculations;
import org.powerbot.game.api.methods.Tabs;
import org.powerbot.game.api.methods.Walking;
import org.powerbot.game.api.methods.Widgets;
import org.powerbot.game.api.methods.interactive.Players;
import org.powerbot.game.api.methods.node.SceneEntities;
import org.powerbot.game.api.methods.tab.Inventory;
import org.powerbot.game.api.methods.widget.Bank;
import org.powerbot.game.api.util.Random;
import org.powerbot.game.api.util.Time;
import org.powerbot.game.api.wrappers.Tile;
import org.powerbot.game.api.wrappers.node.Item;
import org.powerbot.game.api.wrappers.node.SceneObject;
import java.lang.String;

@Manifest(authors = { "TWCrap" }, name = "TWCrap's Smither - Alfa", description = "Smelts ore's in Falador", version = 0.1)
public class TWCrap_Smithing extends ActiveScript {

	// ID's and Animations and Widgets:
	public final int smithingAnimation = 3243;
	public final int doorIDOpen = 11708;
	public final int doorIDClose = 11707;
	public final int furnaceID = 11666;
	public final int widgetMain = 905;
	public final int widgetBar = 14;
	public final int smithingAnimationID = 3243;

	// Path
	public final Tile pathToOven = new Tile(2974, 3369, 0);
	public final Tile atDoor = new Tile(2971, 3379, 0);
	public final Tile atFurnace = new Tile(2974, 3369, 0);
	public final Tile[] tileToCheck = { new Tile(2971, 3378, 0) };
	public final Tile[] pathToOven2 = { new Tile(2955, 3380, 0),
			new Tile(2971, 3378, 0), new Tile(2971, 3378, 0) };
	public final Tile[] pathToFurnace = { new Tile(2974, 3369, 0) };
	public final Tile[] pathToBank = { new Tile(2961, 3378, 0),
			new Tile(2950, 3375, 0), new Tile(2946, 3369, 0) };
	public final Tile atBank = new Tile(2946, 3369, 0);

	// Choose and Needed Ores...
	public final int keuze = 1;
	public int oreFinalChoose1 = 0;
	public int oreFinalChoose2 = 0;
	public String oreFinalChoose1Name = "";
	public String oreFinalChoose2Name = "";
	public final int keuze_1[] = { 436, 438 }; // Copper and Tin for Bronze
	public final int keuze_1_bars = 2349;

	// Other useful declarations....
	public final String welkomMessage = "Welkom to the RS Smelther of TWCrap -Alfa";
	public int actionDoing = 0; // 0 is being busy with bank, 1 is walking to furnace,
								// 2 is smiting and 3 is walking back to bank and 4 is resetting everything
	public int actionDoingBank = 0; // 0 is dumping at bank, 1 is withdrawing....
	public int smithingStart = 0;
	public int isSmithing = 0;

	@Override
	protected void setup() {
		log.info(welkomMessage);
		Tabs.INVENTORY.open();

		final actionBank actionBank = new actionBank();
		final Strategy actionBank_S = new Strategy(actionBank, actionBank);

		final walking Walking = new walking();
		final Strategy walking = new Strategy(Walking, Walking);

		final smithing smithing = new smithing();
		final Strategy Smithing = new Strategy(smithing, smithing);

		final resetVariables resetvariables = new resetVariables();
		final Strategy resetVariables = new Strategy(resetvariables, resetvariables);

		provide(actionBank_S);
		provide(walking);
		provide(Smithing);
		provide(resetVariables);
	}

	public class actionBank extends Strategy implements Task, Condition {
		public boolean validate() {
			return (actionDoing == 1);
		}

		public void run() {
			if (!Bank.isOpen()) {
				Bank.open();
				Time.sleep(1000, 3000);
			}
			if (Bank.isOpen()) {
				if (Inventory.getCount() == 0) {
					actionDoingBank = 1;
				}
				if (Inventory.getCount() > 0 && actionDoingBank == 0) {
					for (int i = 0; i < Inventory.getItems().length; i++) {
						Bank.deposit(Inventory.getItems()[i].getId(), 100);
					}
				}
				if (actionDoingBank == 1) {
					int oreID[] = { 0, 0 };
					switch (keuze) {
						case 1:
							oreID[0] = keuze_1[0];
							oreID[1] = keuze_1[1];
							break;
						}

					if (oreID[0] != 0) {
						if (oreID[1] != 0) {
							Bank.withdraw(oreID[0], 14);
							Time.sleep(Random.nextInt(1000, 3000));
							Bank.withdraw(oreID[1], 14);
							oreFinalChoose1 = oreID[0];
							oreFinalChoose2 = oreID[1];
							actionDoingBank = 2;
						} else {
							Bank.withdraw(oreID[0], 28);
							oreFinalChoose1 = oreID[0];
							actionDoingBank = 2;
						}
					}

				}
				if (actionDoingBank == 2) {
					Time.sleep(Random.nextInt(500, 1500));
					int isInInventory1 = 0;
					int isInInventory2 = 0;
					boolean returnStatement = false;
					for (Item i : Inventory.getItems()) {
						if (i.getId() == oreFinalChoose1) {
							isInInventory1++;
						}
						if (i.getId() == oreFinalChoose2) {
							isInInventory2++;
						}
					}
					if (oreFinalChoose1 != 0 && isInInventory1 != 0) { // if oreFinalChoose1 isset to the ore id and that oreid is found in the inventory
						if (oreFinalChoose2 == 0) { 	// And if there's no second ore needed...
							returnStatement = true;		// Then we have all the ore's we needed and we can go on...
						} else if (oreFinalChoose2 != 0 && isInInventory2 != 0) { 	//if oreFinalChoose2 isset to the ore id of the second ore and whe have that ore in the inventory
							returnStatement = true;		//The we can continue
						} else {
							returnStatement = false;	//Else we can not continue
						}
					}
					if (returnStatement == true) {		//If whe have the ore's, we continue
						actionDoing = 1;
					} else {							//else we start all over again...
						actionDoing = 0;
						actionDoingBank = 0;
					}

				}

			} else {
				log.info("The bank is not open yet...");
			}
		}

	}

	public class walking extends Strategy implements Task, Condition {
		public boolean validate() {
			if (actionDoing == 1 || actionDoing == 3) {		//If we are walking run this
				return true;
			} else {
				return false;
			}
		}

		@Override
		public void run() {
			if (actionDoing == 1) {		//if we are going to walk to the furnace...
				if (Players.getLocal().isMoving() && Calculations.distanceTo(Walking.getDestination())>7) {	//If we are moving, and the destination is more than 7 tiles away 
					Time.sleep(400, 600);				//Wait....
				} else if (Walking.newTilePath(pathToOven2).traverse()) {	//if we can still walk...
					Time.sleep(Random.nextInt(0, 900));
				} else {			//if we are at destination, continue to the next part.
					actionDoing = 2;
				}
				Time.sleep(Random.nextInt(1000, 3000));

			} else if (actionDoing == 3) {		//If we are going to walk back to the bank
				if (Players.getLocal().isMoving() && Calculations.distanceTo(Walking.getDestination())>7) {	//if we are moving we wait a bit...
					Time.sleep(400, 600);
				} else if (Walking.newTilePath(pathToBank).traverse()) {	//If we can still walk, we will walk
					Time.sleep(Random.nextInt(1000, 3000));
				} else {			//Else we will continue to the next part...
					actionDoing = 4;
				}
			}
		}
	}

	public class smithing extends Strategy implements Task, Condition {
		public boolean validate() {
			boolean returnStatement = false;	//We will return this.
			if (actionDoing == 2) {		//If we are going to smith...
				if (oreFinalChoose1 != 0 && Inventory.getCount(oreFinalChoose1) > 0) { 
					if (oreFinalChoose2 == 0) { 		//if no second ore is needed....
						returnStatement = true;
					} else if (oreFinalChoose2 != 0 && Inventory.getCount(oreFinalChoose2) > 0) { 	
						returnStatement = true;
					} else {
						returnStatement = false;
					}
				}
				if (returnStatement == false) {
					actionDoing = 3;
				}
			} else {
				returnStatement = false;
			}
			return returnStatement;
		}

		@Override
		public void run() {
			if (smithingStart == 0) {	//We start at the front of the door. First we check to make sure the door is open. if not, open it and then we walk to the furnace
				final SceneObject door = SceneEntities.getNearest(doorIDClose);
				if (door != null && door.isOnScreen()) {
					Time.sleep(Random.nextInt(500, 1500));
					door.interact("Open");
					Time.sleep(Random.nextInt(1000, 3000));
				}
				Walking.newTilePath(pathToFurnace).traverse();
				smithingStart = 1;
				Time.sleep(Random.nextInt(1000, 4000));
			}
			if (Calculations.distanceTo(atFurnace) < 3 && isSmithing == 0 && smithingStart == 1 && !Players.getLocal().isMoving()) { //if we are close to the furnace, whe are not moving and whe just start smithing...
				if (Players.getLocal().getAnimation() != smithingAnimationID) { 	//if our animation id doesn't equals the smithinganimation id...
					final SceneObject furnace = SceneEntities.getNearest(furnaceID);
					if (furnace.isOnScreen()
							&& Players.getLocal().getAnimation() != smithingAnimationID
							&& isSmithing == 0
							&& !Players.getLocal().isMoving()) {
						furnace.click(true);			//We click on the furnace
						Time.sleep(Random.nextInt(1000, 3000));
						Widgets.get(widgetMain).getChild(widgetBar).click(true);	//and we click on the bar at the bottom...
						isSmithing = 1;
					}
				}
			} else if (isSmithing == 1) {		//if we are already bussy with smithing...
				int x = 0;
				for (int i = 0; i < 10; i++) {
					x += (Players.getLocal().getAnimation() == -1) ? -1 : 1;	//if we stand still (animation id = -1) count +1 at x
					Time.sleep(50);												//And wait a little bit...
				}
				boolean isBussy = x > 0;		//true if we are smithing, false if we are doing nothing...
				if (isBussy == false) {		//if we wheren't doing anything...
					log.info("We werent doing anything, so we start with smithing again.");
					final SceneObject furnace = SceneEntities
							.getNearest(furnaceID);
					if (furnace.isOnScreen()
							&& Players.getLocal().getAnimation() != smithingAnimationID
							&& !Players.getLocal().isMoving()) {
						furnace.click(true);
						Time.sleep(Random.nextInt(1000, 3000));
						Widgets.get(widgetMain).getChild(widgetBar).click(true);
						isSmithing = 1;
					}
				}
			}
			Time.sleep(Random.nextInt(1000, 4000));
		}

	}

	public class resetVariables extends Strategy implements Task, Condition {
		public boolean validate() {
			if (actionDoing == 4) {
				return true;
			} else {
				return false;
			}
		}

		@Override
		public void run() {		//Reset al the variables and we start from the beginning again :P
			actionDoing = 0;
			actionDoingBank = 0;
			smithingStart = 0;
			isSmithing = 0;
			log.info("Everything has been resetted and we start all over again.");
		}

	}

}