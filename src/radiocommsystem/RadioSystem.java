package radiocommsystem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;

public class RadioSystem {
	public static final int VARIABLES = 4, MASK_DEPTH = 2;
	private final static int[] empty = new int[0];

	List<Node> B1 = new ArrayList<>();
	private int[] whitelist = empty, ignored = empty;

	public static class Node { // Abstract Node (B1 element)

		String name;

		V.StaticLoudness interferenceLoudness; // o1
		V.TransmissionLatency ioLatency; // o2
		V.NodeDistance testDistance; // o3
		V.MessageLength msgLength; // o4
		V.SignalQuality quality; // o5

		Node setName(String name) {
			this.name = name;
			return this;
		}

		Node setInterferenceLoudness(int loudness) {
			this.interferenceLoudness = V.StaticLoudness.o1(loudness);
			return this;
		}

		Node setIOLatency(int latency) {
			ioLatency = V.TransmissionLatency.o2(latency);
			return this;
		}

		Node setTestDistance(int dst) {
			testDistance = V.NodeDistance.o3(dst);
			return this;
		}

		Node setMsgLength(V.MessageLength len) {
			msgLength = V.MessageLength.o4(len);
			return this;
		}

		Node setQuality() {
			double interfPercent = 1 - ((double) (interferenceLoudness.ordinal()) / 5.0);
			double latencyPercent = 1 - ((double) (ioLatency.ordinal()) / 4.0);
			double lenPercent = (double) (msgLength.ordinal() - 2) / 3.0;
			double distancePercent = (double) (testDistance.ordinal() + 1) / 5.0;
			double percent = ((100 * (interfPercent + latencyPercent + lenPercent + distancePercent)) / 4.0);

			if (ioLatency.ordinal() > 1)
				percent -= 15;
			if (ioLatency.ordinal() > 2)
				percent -= 15;

			if (interferenceLoudness.ordinal() > 2)
				percent -= 15;

			if (interferenceLoudness.ordinal() > 3)
				percent = 0;

			// System.out.print("%: "+percent);

			if (percent >= -100)
				this.quality = V.SignalQuality.Uns;
			if (percent > 30)
				this.quality = V.SignalQuality.Sat;
			if (percent > 45)
				this.quality = V.SignalQuality.Med;
			if (percent > 66)
				this.quality = V.SignalQuality.Gd;
			if (percent > 75)
				this.quality = V.SignalQuality.Exc;

			return this;
		}

		/* Return Value of Vn */
		int getV(int n) {
			switch (n) {
			case 0:
				return interferenceLoudness.ordinal();

			case 1:
				return ioLatency.ordinal();

			case 2:
				return testDistance.ordinal();

			case 3:
				return msgLength.ordinal() - 3;

			case 4:
				return quality.ordinal() - 5;

			default:
				return -1;
			}
		}
	}

	int[][] getDataMatrix(int[] ignored) {
		int dataMatrix[][] = new int[VARIABLES][B1.size()];

		for (int j = 0; j < B1.size(); ++j) {
			Node node = B1.get(j);
			for (int n = 0; n < VARIABLES; ++n)
				dataMatrix[n][j] = node.getV(n);

		}

		for (int idx : ignored)
			dataMatrix[idx][0] = -1;

		return dataMatrix;
	}

	int[][] getDataMatrix() {
		return getDataMatrix(ignored);
	}

	int[] getIgnoredVs() {
		return ignored;
	}

	int[] getAllowedVs() {
		return whitelist;
	}

	void allowV(int[] whitelist) {
		this.whitelist = whitelist;
		if (whitelist.length >= VARIABLES) {
			ignored = empty;
			return;
		}

		ignored = new int[VARIABLES - whitelist.length];
		int k = 0;
		for (int i = 0; i < VARIABLES; ++i)
			if (!ArrayUtils.contains(whitelist, i + 1))
				ignored[k++] = i;
	}

	ArrayList<Integer> nums(int max) {
		ArrayList<Integer> list = new ArrayList<>();
		for (int i = 0; i < max; ++i)
			list.add(i);
		return list;
	}

	int getMaskIndex(int j) { // Достаємо з маски M значення ро для поточного елемента
		return ((int) Math.floor((float) j / (float) VARIABLES));
	}

	int getMaskIndex(int j, int rsize) { // Достаємо з маски M значення ро для поточного елемента
		return (j % rsize);
	}

	double getGenerativeSystemWBehavior(List<Integer> r, List<Integer> ignoredS) {
		int rowLen = r.size() * VARIABLES;
		int dataMatrix[][] = getDataMatrix();
		Map<String, Integer> elementFrequency = new HashMap<>();
		List<String> elems = new ArrayList<>();
		String colFormat = "%-5s";

		System.out.print("r = (");
		for (int i = 0; i < r.size(); ++i) {
			System.out.print(r.get(i));
			if (i < r.size() - 1)
				System.out.print(", ");
		}
		System.out.println(")");

		int h = whitelist.length, w = MASK_DEPTH;
		int c = 1, maxS = 0;
		int rho[][] = new int[h][w];
		int rArr[] = new int[h * w];

		for (int i = 0, ri = 0; i < MASK_DEPTH; ++i) {
			for (int j = 0, vj = 0; j < VARIABLES; ++j) {
				if (!ignored(dataMatrix, j)) {
					rho[vj++][i] = c;
					rArr[ri++] = c;
					maxS = c;
				}
				++c;
			}
		}

		for (int j = 0; j < h; ++j) {
			for (int i = 0; i < MASK_DEPTH; ++i) {
				if (i == 0)
					out(String.format(colFormat, ""));
				if (!ignoredS.contains(rho[j][i])) {
					out(String.format(colFormat, rho[j][i]));
				} else {
					out(String.format(colFormat, ""));
				}
				++c;
			}
			out();
		}

		out();

		for (int si : rArr)
			if (!ignoredS.contains(si))
				System.out.print(String.format(colFormat, "S" + si));
		System.out.print(String.format(colFormat, "f'"));
		System.out.println();

		int vi, vj;
		int origin = nums(B1.size()).get(r.indexOf(0)); // Знаходимо стовпець таб. даних, що вiдповiдає нулю у ро
		int elem;
		String elemStr;
		float nums = 0;
		for (int i = 0; i < B1.size(); ++i) {
			elemStr = "";

			if (getMaskIndex(rowLen - 1) + i >= B1.size())
				break;

			// Порядково здiйснюємо заповнення таблицi за формулою S[k,w]=V[i,W+r]
			for (int j = 0; j < rowLen; ++j) {
				if (ignoredS.contains(j + 1))
					continue;
				vi = j % VARIABLES;
				if (ignored(dataMatrix, vi))
					continue;
				vj = origin + i + r.get(getMaskIndex(j));
				elem = dataMatrix[vi][vj];
				elemStr += elem + "    ";
			}
			++nums;
			if (!elems.contains(elemStr))
				elems.add(elemStr);
			elementFrequency.put(elemStr, elementFrequency.getOrDefault(elemStr, 0) + 1);
		}

		// Вивiд таблицi
		double prob;
		double hc = 0;
		for (String line : elems) {
			System.out.print(line);
			prob = (float) elementFrequency.get(line) / nums;
			hc += -prob * log(prob, 2);
			System.out.println(prob);
		}
		out();
		out("<h2>H(C) = " + hc + "</h2>" + System.lineSeparator());

		List<Integer> exg_ = new ArrayList<>();
		double exg_Prob = 0;

		for (int j = 0; j < h; ++j) {
			for (int i = 0; i < MASK_DEPTH; ++i) {
				if (!ignoredS.contains(rho[j][i]))
					exg_.add(rho[j][i]);
			}
		}
		if (exg_.contains(maxS))
			exg_.remove(Integer.valueOf(maxS));
		Collections.sort(exg_);

		List<Integer> g = new ArrayList<>();
		if (!ignoredS.contains(maxS)) {
			out("<h3>g = { " + maxS + " }</h3>");
			g.add(maxS);
			out("<h2>G:</h2>");
			out("<b>");
		} else
			out("<h3>g = {  }</h3>");
		for (int num : g)
			System.out.print(String.format(colFormat, "S" + (num)));
		if (!ignoredS.contains(maxS)) {
			System.out.print(String.format(colFormat, "f'"));
			out("</b>");
		}
		System.out.println();
		float lines = 0;
		elems.clear();
		elementFrequency.clear();
		for (int i = 0; i < B1.size(); ++i) {
			elemStr = "";

			if (getMaskIndex(rowLen - 1) + i >= B1.size())
				break;

			// Порядково здiйснюємо заповнення таблицi за формулою S[k,w]=V[i,W+r]
			for (int j = 0; j < rowLen; ++j) {
				vi = j % VARIABLES;
				if (ignored(dataMatrix, vi))
					continue;
				vj = origin + i + r.get(getMaskIndex(j));
				if (ignoredS.contains(j + 1) || !g.contains(j + 1))
					continue;

				elem = dataMatrix[vi][vj];
				elemStr += elem + "    ";
			}
			++lines;
			if (!elems.contains(elemStr))
				elems.add(elemStr);
			elementFrequency.put(elemStr, elementFrequency.getOrDefault(elemStr, 0) + 1);
		}

		if (!ignoredS.contains(maxS))
			for (String line : elems) {
				System.out.print(line);
				prob = (float) elementFrequency.get(line) / lines;
				System.out.println(prob);
			}

		out();
		out();
		out("<h3>e x g_ = { ");
		for (int e : exg_)
			out(e + " ");
		out("}</h3>");
		out("<h2>E x G_:</h2>");
		out("<b>");
		for (int num : exg_)
			System.out.print(String.format(colFormat, "S" + (num)));
		System.out.print(String.format(colFormat, "f'"));
		out("</b>");
		System.out.println();
		lines = 0;
		elems.clear();
		elementFrequency.clear();
		for (int i = 0; i < B1.size(); ++i) {
			elemStr = "";

			if (getMaskIndex(rowLen - 1) + i >= B1.size())
				break;

			// Порядково здiйснюємо заповнення таблицi за формулою S[k,w]=V[i,W+r]
			for (int j = 0; j < rowLen; ++j) {
				vi = j % VARIABLES;
				if (ignored(dataMatrix, vi))
					continue;
				vj = origin + i + r.get(getMaskIndex(j));
				if (ignoredS.contains(j + 1) || !exg_.contains(j + 1))
					continue;

				elem = dataMatrix[vi][vj];
				elemStr += elem + "    ";
			}
			++lines;
			if (!elems.contains(elemStr))
				elems.add(elemStr);
			elementFrequency.put(elemStr, elementFrequency.getOrDefault(elemStr, 0) + 1);
		}

		for (String line : elems) {
			System.out.print(line);
			prob = (float) elementFrequency.get(line) / lines;
			exg_Prob += -prob * log(prob, 2);
			System.out.println(prob);
		}

		Std.out("<h2>H(E x G_) = %f</h2>", exg_Prob);
		Std.out("<h2>H(G | E x G_) = %f</h2>", hc - exg_Prob);

		return hc - exg_Prob;
	}

	double getSystemWBehavior(List<Integer> r, List<Integer> ignoredS) {
		int rowLen = r.size() * VARIABLES;
		int dataMatrix[][] = getDataMatrix();
		Map<String, Integer> elementFrequency = new HashMap<>();
		List<String> elems = new ArrayList<>();
		String colFormat = "%-5s";

		System.out.print("<b>r = (");
		for (int i = 0; i < r.size(); ++i) {
			System.out.print(r.get(i));
			if (i < r.size() - 1)
				System.out.print(", ");
		}
		System.out.println(")</b>");

		int h = whitelist.length, w = MASK_DEPTH;
		int c = 1;
		int rho[][] = new int[h][w];
		int rArr[] = new int[h * w];

		for (int i = 0, ri = 0; i < MASK_DEPTH; ++i) {
			for (int j = 0, vj = 0; j < VARIABLES; ++j) {
				if (!ignored(dataMatrix, j)) {
					rho[vj++][i] = c;
					rArr[ri++] = c;
				}
				++c;
			}
		}

		for (int j = 0; j < h; ++j) {
			for (int i = 0; i < MASK_DEPTH; ++i) {
				if (i == 0)
					out(String.format(colFormat, ""));
				if (!ignoredS.contains(rho[j][i])) {
					out(String.format(colFormat, rho[j][i]));
				} else {
					out(String.format(colFormat, "-"));
				}
				++c;
			}
			out();
		}
		out();

		for (int si : rArr)
			if (!ignoredS.contains(si))
				System.out.print(String.format(colFormat, "S" + si));
		System.out.print(String.format(colFormat, "f'"));
		System.out.println();

		int vi, vj;
		int origin = nums(B1.size()).get(r.indexOf(0));
		int elem;
		String elemStr;
		float nums = 0;
		for (int i = 0; i < B1.size(); ++i) {
			elemStr = "";

			if (getMaskIndex(rowLen - 1) + i >= B1.size())
				break;

			// S[k, w] = V[i, W + r]
			for (int j = 0; j < rowLen; ++j) {
				if (ignoredS.contains(j + 1))
					continue;
				vi = j % VARIABLES;
				if (ignored(dataMatrix, vi))
					continue;

				vj = origin + i + r.get(getMaskIndex(j));
				elem = dataMatrix[vi][vj];
				elemStr += elem + "    ";
			}
			++nums;
			if (!elems.contains(elemStr))
				elems.add(elemStr);
			elementFrequency.put(elemStr, elementFrequency.getOrDefault(elemStr, 0) + 1);
		}

		// Output
		double prob;
		double hc = 0, hg_ = 0;
		for (String line : elems) {
			System.out.print(line);
			prob = (float) elementFrequency.get(line) / nums;
			hc += -prob * log(prob, 2);
			System.out.println(prob);
		}
		out();
		out("<h3>H(C) = " + hc + "</h3>");

		List<Integer> g = new ArrayList<>();
		List<Integer> g_ = new ArrayList<>();

		for (int j = 0, i = 1; j < h; ++j) {
			if (ignoredS.contains(rho[j][i]))
				g.add(rho[j][i - 1]);
			else
				g.add(rho[j][i]);
		}

		Collections.sort(g);

		for (int j = 0; j < h; ++j) {
			for (int i = 0; i < MASK_DEPTH; ++i) {
				if (!g.contains(rho[j][i]) && !ignoredS.contains(rho[j][i]))
					g_.add(rho[j][i]);
			}
		}
		Collections.sort(g_);

		out();
		out("<h3>g = { ");
		for (int e : g)
			out(e + " ");
		out("}</h3>\n");
		out("<h3>G:</h3>");

		for (int num : g)
			System.out.print(String.format(colFormat, "S" + (num)));
		System.out.print(String.format(colFormat, "f'"));

		System.out.println();
		float lines = 0;
		elems.clear();
		elementFrequency.clear();
		for (int i = 0; i < B1.size(); ++i) {
			elemStr = "";

			if (getMaskIndex(rowLen - 1) + i >= B1.size())
				break;

			// S[k,w]=V[i,W+r]
			for (int j = 0; j < rowLen; ++j) {
				vi = j % VARIABLES;
				if (ignored(dataMatrix, vi))
					continue;

				vj = origin + i + r.get(getMaskIndex(j));
				if (ignoredS.contains(j + 1) || !g.contains(j + 1))
					continue;

				elem = dataMatrix[vi][vj];
				elemStr += elem + "    ";
			}
			++lines;
			if (!elems.contains(elemStr))
				elems.add(elemStr);
			elementFrequency.put(elemStr, elementFrequency.getOrDefault(elemStr, 0) + 1);
		}

		for (String line : elems) {
			System.out.print(line);
			prob = (float) elementFrequency.get(line) / lines;
			System.out.println(prob);
		}

		out();
		if (!g_.isEmpty()) {
			out("<h3>g_ = { ");
			for (int gelem : g_)
				out(gelem + " ");
			out("}</h3>\n");
			out("<h3>G_:</h3>");

			for (int num : g_)
				System.out.print(String.format(colFormat, "S" + (num)));
			System.out.print(String.format(colFormat, "f'"));

			System.out.println();
			lines = 0;
			elems.clear();
			elementFrequency.clear();
			for (int i = 0; i < B1.size(); ++i) {
				elemStr = "";

				if (getMaskIndex(rowLen - 1) + i >= B1.size())
					break;

				// S[k,w]=V[i,W+r]
				for (int j = 0; j < rowLen; ++j) {
					vi = j % VARIABLES;
					if (ignored(dataMatrix, vi))
						continue;

					vj = origin + i + r.get(getMaskIndex(j));
					if (ignoredS.contains(j + 1) || !g_.contains(j + 1))
						continue;

					elem = dataMatrix[vi][vj];
					elemStr += elem + "    ";
				}
				++lines;
				if (!elems.contains(elemStr))
					elems.add(elemStr);
				elementFrequency.put(elemStr, elementFrequency.getOrDefault(elemStr, 0) + 1);
			}

			for (String line : elems) {
				System.out.print(line);
				prob = (double) elementFrequency.get(line) / lines;
				hg_ += -prob * log(prob, 2);
				System.out.println(prob);
			}
		}
		out();
		out("<h3>H(G_) = " + hg_ + "</h3>");
		out("<h3>H(G | G_) = " + (hc - hg_) + "</h3>\n");

		return hc - hg_;
	}

	int[][] getRegularGenSystemWBehavior(List<Integer> r, boolean generated) { // Породжуюча Система з поведiнкою
		// generated == true -> породжуванi | generated == false -> породжуючi
		int rowLen = r.size() * VARIABLES; // <=> кiлькостi елементiв маски M
		int system[][] = new int[rowLen][B1.size()];
		int dataMatrix[][] = getDataMatrix();
		Map<String, Integer> elementFrequency = new HashMap<>();
		List<String> elems = new ArrayList<>();
		Set<Integer> cols = new HashSet<>();

		if (generated)
			System.out.println("Система для породжуваних елементiв (G):");
		else
			System.out.println("Система для породжуючих елементiв (G_):");

		System.out.print("r = (");
		for (int i = 0; i < r.size(); ++i) {
			System.out.print(r.get(i));
			if (i < r.size() - 1)
				System.out.print(", ");
		}
		System.out.println(")");

		String colFormat = "%-5s";
		for (int i = 0; i < rowLen; ++i) {
			if ((generated && getGenMaskIndex(i, r.size()) == r.size() - 1)
					|| (!generated && getGenMaskIndex(i, r.size()) != r.size() - 1)) {
				System.out.print(String.format(colFormat, "S" + (i + 1)));
				cols.add(i);
			}
		}
		System.out.print(String.format(colFormat, "f'"));
		System.out.println();

		int vi, vj;
		int origin = nums(B1.size()).get(r.indexOf(0)); // Знаходимо стовпець таб. даних, що вiдповiдає нулю у ро
		int elem;
		String elemStr;
		for (int i = 0; i < B1.size(); ++i) {
			elemStr = "";

			if (getGenMaskIndex(rowLen - 1, r.size()) + i >= B1.size())
				break;

			// Порядково здiйснюємо заповнення таблицi за формулою S[k,w]=V[i,W+r]
			for (int j = 0; j < cols.size(); ++j) {

				vi = j % VARIABLES;
				vj = origin + i + r.get(generated ? r.size() - 1 : getMaskIndex(j));
				if (vj == -1 && i == 0)
					vj = 0;
				elem = system[j][origin + i] = dataMatrix[vi][vj];
				elemStr += elem + "    ";
			}

			if (!elems.contains(elemStr))
				elems.add(elemStr);
			elementFrequency.put(elemStr, elementFrequency.getOrDefault(elemStr, 0) + 1);

		}

		// Вивiд таблицi
		for (String line : elems) {
			System.out.print(line);
			System.out.println((float) elementFrequency.get(line) / (float) B1.size());
		}
		return system;
	}

	int[][] getRegularSystemWBehavior(ArrayList<Integer> r) { // Система з поведiнкою
		int rowLen = r.size() * VARIABLES; // <=> кiлькостi елементiв маски M
		int system[][] = new int[rowLen][B1.size()];
		int dataMatrix[][] = getDataMatrix();
		HashMap<String, Integer> elementFrequency = new HashMap<>();
		ArrayList<String> elems = new ArrayList<>();

		System.out.print("r = (");
		for (int i = 0; i < r.size(); ++i) {
			System.out.print(r.get(i));
			if (i < r.size() - 1)
				System.out.print(", ");
		}
		System.out.println(")");

		String colFormat = "%-5s";
		for (int i = 0; i < rowLen; ++i)
			System.out.print(String.format(colFormat, "S" + (i + 1)));
		System.out.print(String.format(colFormat, "f'"));
		System.out.println();

		int vi, vj;
		int origin = nums(B1.size()).get(r.indexOf(0)); // Знаходимо стовпець таб. даних, що вiдповiдає нулю у ро
		int elem;
		String elemStr;
		for (int i = 0; i < B1.size(); ++i) {
			elemStr = "";

			if (getMaskIndex(rowLen - 1) + i >= B1.size())
				break;

			// Порядково здiйснюємо заповнення таблицi за формулою S[k,w]=V[i,W+r]
			for (int j = 0; j < rowLen; ++j) {
				vi = j % VARIABLES;
				vj = origin + i + r.get(getMaskIndex(j));
				elem = system[j][origin + i] = dataMatrix[vi][vj];
				elemStr += elem + "    ";
			}

			if (!elems.contains(elemStr))
				elems.add(elemStr);
			elementFrequency.put(elemStr, elementFrequency.getOrDefault(elemStr, 0) + 1);
		}

		// Вивiд таблицi
		for (String line : elems) {
			System.out.print(line);
			System.out.println((float) elementFrequency.get(line) / (float) B1.size());
		}

		return system;
	}

	public static double log(double value, double base) {
		return Math.log(value) / Math.log(base);
	}

	void out() {
		out(System.lineSeparator());
	}

	void out(String arg) {
		System.out.print(arg);
	}

	int getGenMaskIndex(int j, int w) { // Достаємо з одои з масок Mg або Mg_ значення ро для поточного елемента
		return j % w;
	}

	int[][] getGenSystemWBehavior(ArrayList<Integer> r, boolean generated) { // Породжуюча Система з поведiнкою
		// generated == true -> породжуванi | generated == false -> породжуючi
		int rowLen = r.size() * VARIABLES; // <=> кiлькостi елементiв маски M
		int system[][] = new int[rowLen][B1.size()];
		int dataMatrix[][] = getDataMatrix();
		HashMap<String, Integer> elementFrequency = new HashMap<>();
		ArrayList<String> elems = new ArrayList<>();
		HashSet<Integer> cols = new HashSet<>();

		if (generated)
			System.out.println("Таблиця для породжуваних елементiв (G):");
		else
			System.out.println("Таблиця для породжуючих елементiв (G_):");

		System.out.print("r = (");
		for (int i = 0; i < r.size(); ++i) {
			System.out.print(r.get(i));
			if (i < r.size() - 1)
				System.out.print(", ");
		}
		System.out.println(")");

		String colFormat = "%-5s";
		for (int i = 0; i < rowLen; ++i) {
			if ((generated && getGenMaskIndex(i, r.size()) == r.size() - 1)
					|| (!generated && getGenMaskIndex(i, r.size()) != r.size() - 1)) {
				System.out.print(String.format(colFormat, "S" + (i + 1)));
				cols.add(i);
			}
		}
		System.out.print(String.format(colFormat, "f'"));
		System.out.println();

		int vi, vj;
		int origin = nums(B1.size()).get(r.indexOf(0)); // Знаходимо стовпець таб. даних, що вiдповiдає нулю у ро
		int elem;
		String elemStr;
		for (int i = 0; i < B1.size(); ++i) {
			elemStr = "";

			if (getGenMaskIndex(rowLen - 1, r.size()) + i >= B1.size())
				break;

			// Порядково здiйснюємо заповнення таблицi за формулою S[k,w]=V[i,W+r]
			for (int j = 0; j < cols.size(); ++j) {

				vi = j % VARIABLES;
				vj = origin + i + r.get(generated ? r.size() - 1 : getMaskIndex(j));
				if (vj == -1 && i == 0)
					vj = 0;
				elem = system[j][origin + i] = dataMatrix[vi][vj];
				elemStr += elem + "    ";
			}

			if (!elems.contains(elemStr))
				elems.add(elemStr);
			elementFrequency.put(elemStr, elementFrequency.getOrDefault(elemStr, 0) + 1);

		}

		// Вивiд таблицi
		for (String line : elems) {
			System.out.print(line);
			System.out.println((float) elementFrequency.get(line) / (float) B1.size());
		}

		return system;
	}

	int[][] getDirectedSystemWBehavior(ArrayList<Integer> r, boolean out, boolean g) {
		int rowLen = r.size() * VARIABLES; // <=> кiлькостi елементiв маски M
		int system[][] = new int[rowLen][B1.size()];
		int dataMatrix[][] = getDataMatrix();
		HashMap<String, Integer> elementFrequency = new HashMap<>();
		ArrayList<String> elems = new ArrayList<>();
		boolean end = false;
		int sz = B1.size();

		System.out.print("r = (");
		for (int i = 0; i < r.size(); ++i) {
			System.out.print(r.get(i));
			if (i < r.size() - 1)
				System.out.print(", ");
		}
		System.out.println(")");

		String colFormat = "%-5s";
		for (int i = 0 + (out ? (g ? rowLen - 1 : (r.size() * VARIABLES) - r.size()) : 0); i < rowLen
				- (out ? (g ? 0 : 1) : 1); ++i)
			System.out.print(String.format(colFormat, "S" + (i + 1)));
		System.out.print(String.format(colFormat, "f'"));
		System.out.println();

		int vi, vj;
		int origin = nums(B1.size()).get(r.indexOf(0));
		int elem;
		String elemStr;
		for (int i = 0; i < B1.size(); ++i) {
			elemStr = "";

			for (int j = (out ? (g ? rowLen - 1 : (r.size() * VARIABLES) - r.size()) : 0); j < rowLen
					- (out ? (g ? 0 : 1) : 1); ++j) {

				vi = (j / r.size());
				vj = origin + i + r.get(getMaskIndex(j, r.size()));

				if (vj >= B1.size()) {
					end = true;
					--sz;
					break;
				}
				elem = dataMatrix[vi][vj];
				elemStr += elem + "    ";
			}

			if (!end && !elems.contains(elemStr) && !elemStr.equals(""))
				elems.add(elemStr);
			if (!end && !elemStr.equals(""))
				elementFrequency.put(elemStr, elementFrequency.getOrDefault(elemStr, 0) + 1);
		}

		// Вивiд таблицi
		for (String line : elems) {
			System.out.print(line);
			System.out.println(((float) elementFrequency.get(line) / (float) sz));
		}

		return system;
	}

	public static double round(double num) {
		return (Math.round(num * 100) / 100.00);
	}

	private boolean ignored(int dataMatrix[][], int v) {
		if (dataMatrix.length <= v)
			return true;

		return dataMatrix[v][0] == -1;
	}

	public void printTable() {
		printTable(false);
	}

	public void printTable(boolean withSemantics) {
		int dataMatrix[][] = getDataMatrix();
		if (ignored.length > 0) {
			out("\t<h2>Система с переменными: { ");
			for (int idx : whitelist)
				Std.o("V" + (idx) + " ");
			Std.o("} / Упрощенные переменные: { ");
			for (int idx : ignored)
				Std.o("V" + (idx + 1) + " ");
			Std.out("}</h2>\n");
		} else
			Std.out("\t<h2>Полная нейтральная система данных:</h2>\n");

		String headerFormat = withSemantics ? "%-20s" : "%-12s";
		String bodyFormat = withSemantics ? "%-13s" : "%-6s";
		int cols;
		int k = 0;
		while (k < B1.size()) {
			cols = 10;
			k += cols;
			if (k > B1.size())
				k = B1.size();
			System.out.print(String.format(headerFormat, withSemantics ? "Вузли" : " "));
			for (int j = k - cols; j < k; ++j)
				System.out.print(
						"<b>" + String.format(bodyFormat, withSemantics ? ("Вузол #" + (j + 1)) : (j + 1)) + "</b>");
			System.out.println(System.lineSeparator());

			if (!ignored(dataMatrix, 0)) {
				System.out.print("<b>" + String.format(headerFormat, withSemantics ? "гучн. помiх" : "V1") + "</b>");
				for (int j = k - cols; j < k; ++j)
					System.out.print(String.format(bodyFormat,
							withSemantics ? V.StaticLoudness.values()[dataMatrix[0][j]] : dataMatrix[0][j]));
				System.out.println();
			}

			if (!ignored(dataMatrix, 1)) {
				System.out.print(
						"<b>" + String.format(headerFormat, withSemantics ? "затр. прий./перед." : "V2") + "</b>");
				for (int j = k - cols; j < k; ++j)
					System.out.print(String.format(bodyFormat,
							withSemantics ? V.TransmissionLatency.values()[dataMatrix[1][j]] : dataMatrix[1][j]));
				System.out.println();
			}

			if (!ignored(dataMatrix, 2)) {
				System.out.print("<b>" + String.format(headerFormat, withSemantics ? "вiдстань" : "V3") + "</b>");
				for (int j = k - cols; j < k; ++j)
					System.out.print(String.format(bodyFormat,
							withSemantics ? V.NodeDistance.values()[dataMatrix[2][j]] : dataMatrix[2][j]));
				System.out.println();
			}

			if (!ignored(dataMatrix, 3)) {
				System.out.print("<b>" + String.format(headerFormat, withSemantics ? "дов. повiд." : "V4") + "</b>");
				for (int j = k - cols; j < k; ++j)
					System.out.print(String.format(bodyFormat,
							withSemantics ? V.MessageLength.values()[dataMatrix[3][j] + 3] : dataMatrix[3][j]));
				System.out.println();
			}

			if (!ignored(dataMatrix, 4)) {
				System.out.print("<b>" + String.format(headerFormat, withSemantics ? "якiсть зв." : "V5") + "</b>");
				for (int j = k - cols; j < k; ++j)
					System.out.print(String.format(bodyFormat,
							withSemantics ? V.SignalQuality.values()[dataMatrix[4][j] + 5] : dataMatrix[4][j]));
				System.out.println();
			}

			System.out.println(System.lineSeparator());
		}
	}
}
