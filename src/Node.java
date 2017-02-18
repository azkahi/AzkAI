import java.util.Deque;
import java.util.LinkedList;
import java.util.Random;

import simulator.Simulator;
import structs.CharacterData;
import structs.FrameData;
import structs.GameData;

import commandcenter.CommandCenter;

import enumerate.Action;

/**
 * Node data structure for Djikstra Algorithm
 * 
 * @author Azka Hanif Imtiyaz
 */
public class Node {

	/** Execution time (Time for 1 frame */
	public static final int EXEC_TIME = 165 * 100000;

	/** Depth for heuristic search */
	public static final int DJIKSTRA_TREE_DEPTH = 2;

	/** Threshold for generating a node */
	public static final int CREATE_NODE_THRESHOULD = 10;

	/** Time for performing simulation */
	public static final int SIMULATION_TIME = 60;
	
	/** Use when in need of random numbers */
	private Random rnd;

	/** Parent node */
	private Node parent;

	/** Child node */
	private Node[] children;

	/** Node depth */
	private int depth;

	/** Evaluation value */
	private double val;

	private double visit;

	/** All selectable actions of self AI */
	private LinkedList<Action> myActions;

	/**  All selectable actions of the opponent */
	private LinkedList<Action> oppActions;

	/** Use in simulation */
	private Simulator simulator;

	/** Selected action by self AI during search */
	private LinkedList<Action> selectedMyActions;

	/** Self HP before simulation */
	private int myOriginalHp;

	/** Opponent HP before simulation */
	private int oppOriginalHp;

	/** Frame data for simulation */
	private FrameData frameData;
	
	/** Player number */
	private boolean playerNumber;
	
	private CommandCenter commandCenter;
	private GameData gameData;

	/** My Action */
	Deque<Action> mAction;
	
	/** Opponent action */
	Deque<Action> oppAction;

	/** Constructor with selected action as parameter */
	public Node(FrameData frameData, Node parent, LinkedList<Action> myActions,
			LinkedList<Action> oppActions, GameData gameData, boolean playerNumber,
			CommandCenter commandCenter, LinkedList<Action> selectedMyActions) {
		this(frameData, parent, myActions, oppActions, gameData, playerNumber, commandCenter);

		this.selectedMyActions = selectedMyActions;
	}

	/** Constructor without selected action as parameter */
	public Node(FrameData frameData, Node parent, LinkedList<Action> myActions,
			LinkedList<Action> oppActions, GameData gameData, boolean playerNumber,
			CommandCenter commandCenter) {
		this.frameData = frameData;
		this.parent = parent;
		this.myActions = myActions;
		this.oppActions = oppActions;
		this.gameData = gameData;
		this.simulator = new Simulator(gameData);
		this.playerNumber = playerNumber;
		this.commandCenter = commandCenter;

		this.selectedMyActions = new LinkedList<Action>();

		this.rnd = new Random();
		this.mAction = new LinkedList<Action>();
		this.oppAction = new LinkedList<Action>();

		CharacterData myCharacter = playerNumber ? frameData.getP1() : frameData.getP2();
		CharacterData oppCharacter = playerNumber ? frameData.getP2() : frameData.getP1();
		myOriginalHp = myCharacter.getHp();
		oppOriginalHp = oppCharacter.getHp();

		if (this.parent != null) {
			this.depth = this.parent.depth + 1;
		} else {
			this.depth = 0;
		}
	}

	/** 
	 * Methods to be called at processing() Loop until 1 frame has passed 
	 * 
	 * @return best action to be executed
	 */
	public Action execute(){
		long start = System.nanoTime();
		while(System.nanoTime() - start <= EXEC_TIME){
			djikstra();
		}

		return getBestVisitAction();
	}

	/**
	 * Perform a simulation
	 *
	 * @return the evaluation value of the playout
	 */
	public double playout() {
		mAction.clear();
		oppAction.clear();

		for (int i = 0; i < selectedMyActions.size(); i++) {
			mAction.add(selectedMyActions.get(i));
		}

		for (int i = 0; i < 5 - selectedMyActions.size(); i++) {
			mAction.add(myActions.get(rnd.nextInt(myActions.size())));
		}

		for (int i = 0; i < 5; i++) {
			oppAction.add(oppActions.get(rnd.nextInt(oppActions.size())));
		}

		FrameData nFrameData =
				simulator.simulate(frameData, playerNumber, mAction, oppAction, SIMULATION_TIME); // Perform simulation

		return getScore(nFrameData);
	}

	
	/**
	 * Main algorithm, processing each node based on data acquired before
	 * Node is selected based on score acquired from simulation and data  
	 * 
	 * @return score of this node + selected child
	 */
	public double djikstra(){
		Node selectedNode = null;

		double bestVal = -9999;

		for (Node child : this.children) {
			if (child.visit == 0){
				child.val = 0;
				for (Act a : AzkAI.listAction){
					for (Action act : child.selectedMyActions){
						if (a.getAction() == act && a.getKey().equals(AzkAI.keyToString(AzkAI.oppKey))){
							child.val += a.getScore();
						}
					}
				}
			}

			if (bestVal < child.val) {
				selectedNode = child;
				bestVal = child.val;
			}
		}

		double score = 0;
		if (selectedNode.visit == 0) {
			score = selectedNode.playout();
		} else {
			if (selectedNode.children == null) {
				if (selectedNode.depth < DJIKSTRA_TREE_DEPTH) {
					if (CREATE_NODE_THRESHOULD <= selectedNode.visit) {
						selectedNode.createNode();
						score = selectedNode.djikstra();
					} else {
						score = selectedNode.playout();
					}
				} else {
					score = selectedNode.playout();
				}
			} else {
				if (selectedNode.depth < DJIKSTRA_TREE_DEPTH) {
					score = selectedNode.djikstra();
				} else {
					selectedNode.playout();
				}
			}

		}

		selectedNode.visit++;
		selectedNode.val += score;

		if (depth == 0) {
			visit++;
		}

		return score;
	}

	/**
	 * Creating node based on possible and selected action to be performed
	 */
	public void createNode() {

		this.children = new Node[myActions.size()];

		for (int i = 0; i < children.length; i++) {

			LinkedList<Action> my = new LinkedList<Action>();
			for (Action act : selectedMyActions) {
				my.add(act);
			}

			my.add(myActions.get(i));

			children[i] =
					new Node(frameData, this, myActions, oppActions, gameData, playerNumber, commandCenter, my);
		}	  
	}

	/**
	 * Get the best action to be performed based on the score of each children node
	 * 
	 * @return best action to be performed
	 */
	public Action getBestVisitAction() {

		int selected = -1;
		double bestGames = -9999;

		for (int i = 0; i < children.length; i++) {

			if (bestGames < children[i].val) {
				bestGames = children[i].val;
				selected = i;
			}
		}

		return this.myActions.get(selected);
	}

	/**
	 * Return the evaluation value
	 *
	 * @param fd frame data (including information such as hp)
	 * @return the score
	 */
	public int getScore(FrameData fd) {
		return playerNumber ? (fd.getP1().hp - myOriginalHp) - (fd.getP2().hp - oppOriginalHp) : (fd.getP2().hp - myOriginalHp) - (fd.getP1().hp - oppOriginalHp);
	}


}
