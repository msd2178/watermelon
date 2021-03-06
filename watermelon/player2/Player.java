package watermelon.player2;

import java.util.ArrayList;
import java.util.Random;

import watermelon.sim.Pair;
import watermelon.sim.Point;
import watermelon.sim.seed;

public class Player extends watermelon.sim.Player {

	static double distowall = 1.00;
	static double distotree = 2.00;
	static double distoseed = 2.00;
	static double epsilon = .0000001;

	double width;
	double length;
	double s;
	public static ArrayList<Pair> treelist;
	public static ArrayList<seed> seedlist;
	Random random;

	public Player() {
		init();
	}

	public void init() {
		seedlist = new ArrayList<seed>();
		random = new Random();
	}

	// distance between seed and pair
	static double distance(seed tmp, Pair pair) {
		return Math.sqrt((tmp.x - pair.x) * (tmp.x - pair.x) + (tmp.y - pair.y)
				* (tmp.y - pair.y));
	}

	// distance between seed and point
	static double distance(seed a, Point b) {
		return Math.sqrt((a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y));
	}

	// distance between seed and seed
	static double distance(seed a, seed b) {
		return Math.sqrt((a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y));
	}

	// get the hexagonal x offset
	static double getHexagonalOffsetX() {
		return distoseed * Math.sin(Math.PI / 6.0);
	}

	// get the hexagonal y offset
	static double getHexagonalOffsetY() {
		return distoseed * Math.cos(Math.PI / 6.0);
	}

	@Override
	public ArrayList<seed> move(ArrayList<Pair> initTreelist, double initWidth,
			double initLength, double initS) {
		treelist = initTreelist;
		width = initWidth;
		length = initLength;
		s = initS;

		seedlist = getHexagonalAlternatingBoard();
		recolorLowProducingSeeds(seedlist);

		System.out.printf("seedlist size is %d\n", seedlist.size());
		System.out.printf("score is %f\n", calculateScore(seedlist));
		return seedlist;
	}

	private void recolorLowProducingSeeds(ArrayList<seed> seedlist) {
		SeedGraph seedgraph = new SeedGraph(seedlist);
		for (int i = 0; i < seedlist.size(); i++)
			if (seedgraph.isLowProducer(i))
				seedlist.get(i).tetraploid = !seedlist.get(i).tetraploid;
	}

	private ArrayList<seed> getHexagonalAlternatingBoard() {
		ArrayList<seed> tmplist = new ArrayList<seed>();
		int seedType = 1;
		boolean shift = false;
		boolean alternateRow = true;
		for (double j = distowall; j <= length - distowall; j = j
				+ getHexagonalOffsetY() + epsilon) {
			if (!alternateRow)
				seedType *= -1;
			alternateRow = !alternateRow;
			for (double i = distowall; i <= width - distowall; i = i
					+ distoseed) {
				seed tmp = new seed(i, j, false);
				if (shift) {
					tmp.x += getHexagonalOffsetX();
				}
				if (seedType == 1) {
					tmp.tetraploid = true;
				}
				seedType *= -1;
				if (validateSeed(tmp))
					tmplist.add(tmp);
			}
			seedType = 1;
			shift = !shift;
		}
		return tmplist;
	}

	private ArrayList<seed> getHexagonalBoard() {
		int seedType = 1;
		ArrayList<seed> tmplist = new ArrayList<seed>();
		boolean shift = false;
		for (double j = distowall; j < length - distowall; j = j
				+ getHexagonalOffsetY() + epsilon) {
			for (double i = distowall; i < width - distowall; i = i + distoseed) {
				seed tmp = new seed(i, j, false);
				if (shift) {
					tmp.x += getHexagonalOffsetX();
				}
				if (seedType == 1) {
					tmp.tetraploid = true;
				}
				seedType *= -1;
				if (validateSeed(tmp))
					tmplist.add(tmp);
			}
			seedType = 1;
			shift = !shift;
		}
		return tmplist;
	}

	private ArrayList<seed> getAlternatingBoard() {
		int seedType = 1;
		ArrayList<seed> tmplist = new ArrayList<seed>();
		for (double i = distowall; i < width - distowall; i = i + distoseed) {

			// alternate initial seed type per row
			if (i % 2 == 0)
				seedType = seedType * -1;

			for (double j = distowall; j < length - distowall; j = j
					+ distoseed) {
				seed tmp;
				// alternate seed type
				if (seedType == 1) {
					tmp = new seed(i, j, false);
					seedType *= -1;
				} else {
					tmp = new seed(i, j, true);
					seedType *= -1;
				}
				if (validateSeed(tmp))
					tmplist.add(tmp);
			}
		}
		return tmplist;
	}

	private ArrayList<seed> getRandomBoard() {
		ArrayList<seed> tmplist = new ArrayList<seed>();
		for (double i = distowall; i < width - distowall; i = i + distoseed) {
			for (double j = distowall; j < length - distowall; j = j
					+ distoseed) {
				seed tmp;
				if (random.nextInt(2) == 0)
					tmp = new seed(i, j, false);
				else
					tmp = new seed(i, j, true);
				if (validateSeed(tmp))
					tmplist.add(tmp);
			}
		}
		return tmplist;
	}

	private boolean validateSeed(seed tmpSeed) {
		for (Pair p : treelist) {
			if (distance(tmpSeed, p) < distotree) {
				return false;
			}
			if (tmpSeed.x + 1.00 > width || tmpSeed.x - 1.00 < 0) {
				return false;
			}
			if (tmpSeed.y + 1.00 > length || tmpSeed.y - 1.00 < 0) {
				return false;
			}
		}
		return true;
	}

	private double calculateScore(ArrayList<seed> seedlist) {
		double total = 0.0;
		for (int i = 0; i < seedlist.size(); i++) {
			double score = 0.0;
			double chance = 0.0;
			double totaldis = 0.0;
			double difdis = 0.0;
			for (int j = 0; j < seedlist.size(); j++) {
				if (j != i) {
					totaldis = totaldis
							+ Math.pow(
									distance(seedlist.get(i), seedlist.get(j)),
									-2);
				}
			}
			for (int j = 0; j < seedlist.size(); j++) {
				if (j != i
						&& ((seedlist.get(i).tetraploid && !seedlist.get(j).tetraploid) || (!seedlist
								.get(i).tetraploid && seedlist.get(j).tetraploid))) {
					difdis = difdis
							+ Math.pow(
									distance(seedlist.get(i), seedlist.get(j)),
									-2);
				}
			}
			chance = difdis / totaldis;
			score = chance + (1 - chance) * s;
			total = total + score;
		}
		return total;
	}

}
