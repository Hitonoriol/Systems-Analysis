package radiocommsystem;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.mutable.MutableInt;

public class SystemsAnalysis {

	private static RadioSystem system = new RadioSystem();
	private static List<Integer> r = new ArrayList<>();

	private static class HS implements Comparable<HS> {
		public int i;
		public double H;

		public HS(int i, double H) {
			this.i = i;
			this.H = H;
		}

		@Override
		public int compareTo(SystemsAnalysis.HS o) {
			return Double.compare(H, o.H);
		}
	}

	private static List<int[]> getIgnoredSList() {
		final int offset = RadioSystem.VARIABLES;
		List<Integer> availableS = new ArrayList<>();
		int[] allowed = system.getAllowedVs();

		Std.o("Используем маску r = (");
		for (int i = 0; i < r.size(); ++i) {
			Std.o("" + r.get(i));
			if (i < r.size() - 1)
				Std.o(", ");
		}
		Std.out(") глубины %d\n", r.size());

		Std.o("Подмаски для переменных <b>[ ");
		for (int vi : allowed)
			Std.o("V" + vi + " ");
		Std.out("]:</b>\n");

		for (int v : allowed) {
			availableS.add(v);
			availableS.add(v + offset);
		}

		CombinationGenerator gen = new CombinationGenerator(availableS);
		for (int del = 1; del <= allowed.length; ++del)
			gen.generate(del);
		List<int[]> ignored = gen.combinations();

		Iterator<int[]> it = ignored.iterator();
		while (it.hasNext()) {
			int[] ign = it.next();
			for (int i = 1; i <= offset; ++i) {
				if (ArrayUtils.contains(ign, i) && ArrayUtils.contains(ign, i + offset)) {
					it.remove();
					break;
				}
			}
		}

		if (!ignored.isEmpty())
			ignored.remove(ignored.size() - 1);

		ignored.add(0, new int[0]);

		return ignored;
	}

	public static void main(String[] args) throws IOException {
		System.setOut(new PrintStream("out.html"));
		initDataSystem();
		StringBuilder head = new StringBuilder("<meta charset = 'utf-8'><style>"
				+ "pre {font-size:15px;}"
				+ "a {size: 17px; color: #0048ad; text-decoration: none}"
				+ "a:hover {text-decoration: underline}"
				+ ".nopad {margin: 0; padding: 0;}"
				+ "</style><center><div style='width:45%;text-align:left;'><pre>");
		head.append("<h1>Направленные системы с поведением</h1>")
				.append("<h2>H(G | E x G_) = H(C) - H(E x G_)</h2>")
				.append("<h2>Содержание</h2>")
				.append("<ul>")
				.append("<li><a href='#intro'>Нейтральная система данных</a></li><br>");

		List<Integer> nonNullQ = new ArrayList<>();
		List<int[]> unsortedKeys = new ArrayList<>();
		/* System variable combinations (V1V2, V1V3, ...) */
		CombinationGenerator gen = new CombinationGenerator(1, RadioSystem.VARIABLES);
		for (int i = RadioSystem.VARIABLES; i > 0; --i)
			gen.generate(i);
		List<int[]> vCombs = gen.combinations();

		// Mask with depth=2 // r = (0, 1)
		set(r, 0, 1);

		/* Number of the first-order simplified systems = (2 ^ n) - 2 */
		final int simCount = (int) (Math.pow(2, RadioSystem.VARIABLES) - 2);
		Std.out("<h6 id='intro'></h6>");
		system.printTable();
		Std.out("<h3>Существует (2 ^ %d) - 2 = %d упрощенных систем 1-го рода по переменным:</h3>",
				RadioSystem.VARIABLES, simCount);
		vCombs.forEach(vComb -> {
			if (vComb.length == RadioSystem.VARIABLES)
				return;

			Std.o("  ");
			for (int vi : vComb)
				Std.o("<b>V" + vi + "</b> ");
			Std.out();
		});
		hr();

		Map<int[], Map<Integer, List<HS>>> globalOpt = new HashMap<>();

		/* Simplify the system using the generated Vn combinations */
		MutableInt vi = new MutableInt();
		vCombs.forEach(allowedVars -> {
			List<HS> nonNullSystems = new ArrayList<>();
			final int smNum = vi.intValue();
			Std.out("<h1 id='smp-%d'>Упрощение #%d.</h1>", smNum, vi.getAndIncrement());

			head.append("<li><a href='#smp-" + smNum + "'>Упрощение #" + smNum + " [ ");
			for (int alw : allowedVars)
				head.append("V" + alw + " ");
			head.append("]</a></li>");
			head.append("<ul>");
			head.append("<li><a href='#best-" + smNum + "'>Лучшие по нечёткости</a></li><li><a href='#non-null-" + smNum
					+ "'>С ненулевым H(G | E x G_)</a></li>");
			head.append("</ul>");
			head.append("<br>");

			Map<Integer, List<HS>> H = new HashMap<>();
			final int variables = allowedVars.length;

			for (int i = 0; i <= variables; ++i)
				H.put(i, new ArrayList<>());

			MutableInt i = new MutableInt(1);

			system.allowV(allowedVars);
			system.printTable();
			List<int[]> ignoredS = getIgnoredSList();
			MutableInt igi = new MutableInt(1);
			ignoredS.forEach(ign -> {
				Std.o("  " + igi.getAndIncrement() + ". ");
				if (ign.length == 0)
					Std.out("<a href = '#sys-%d-1'>Полная маска мощности <b>%d</b></a>", smNum,
							allowedVars.length * RadioSystem.MASK_DEPTH);
				else {
					Std.o("<a href = '#sys-%d-%d'>С исключёнными <b>[ ", smNum, igi.intValue() - 1);
					for (int si : ign)
						Std.o("S" + si + " ");
					Std.out("]</b></a>");
				}
			});
			Std.out("  <a href='#non-null-%d'>С ненулевым H(G | E x G_)</a>", smNum);
			Std.out("  <a href='#best-%d'>Наилучшие по нечёткости</a>", smNum);

			ignoredS.forEach(ignore -> {
				final int sysNum = i.intValue();
				Std.out("\n<h2 id='sys-%d-%d'>#" + i.getAndIncrement() + ".</h2>", smNum, sysNum);
				HS summary = new HS(i.intValue() - 1,
						system.getGenerativeSystemWBehavior(r,
								Arrays.stream(ignore).boxed().collect(Collectors.toList())));

				if (summary.H > 0)
					nonNullSystems.add(summary);

				H.get(ignore.length)
						.add(summary);
			});
			nonNullQ.add(nonNullSystems.size());
			Std.out();
			hr();
			String vStr = "";
			for (int vvi : allowedVars)
				vStr += "V" + vvi + " ";
			vStr = vStr.substring(0, vStr.length() - 1);
			Std.o("<h3 id='non-null-%d'>Системы с ненулевым H(G | E x G_) по переменным [%s]</h3>", smNum, vStr);
			Std.o("<ul>");
			if (nonNullSystems.isEmpty())
				Std.out("<b>Отсутствуют</b>");
			nonNullSystems.forEach(sys -> {
				Std.out("<li><a href='#sys-%d-%d'>Система #%d</a>; H(G | E x G_) = %f</li>",
						smNum, sys.i, sys.i, sys.H);
			});
			Std.out("</ul>");

			Std.out();
			Std.out("<h3 id='best-%d'>Наиболее оптимальные направленные системы по нечёткости:</h3>", smNum);
			HS hs;
			for (int k = 0; k <= variables; ++k) {
				Collections.sort(H.get(k));
				hs = H.get(k).get(0);
				Std.out("|M| = %d: <a href='#sys-%d-%d'>Система #%d</a>; H(G | E x G_) = %f",
						(variables * RadioSystem.MASK_DEPTH) - k,
						smNum, hs.i,
						hs.i, hs.H);
			}

			unsortedKeys.add(allowedVars);
			globalOpt.put(allowedVars, H);
			hr();
		});
		head.append(
				"<li><a href='#summary'>Наиболее оптимальные направленные системы по сложности и нечёткости</a></li><br>");
		head.append("<li><a href='#src-code'>Исходный код программы</a></li>");
		head.append("</ul>");

		Std.out("<h2 id = \"summary\">Наиболее оптимальные направленные системы по сложности и нечёткости</h2>");
		List<int[]> varList = new ArrayList<>(globalOpt.keySet());
		Collections.sort(varList, (arr1, arr2) -> Integer.compare(arr2.length, arr1.length));
		MutableInt complexity = new MutableInt();
		varList.add(new int[0]);
		varList.forEach(allowedVars -> {
			final int cmx = allowedVars.length;
			boolean cmpChanged = false;
			if (cmx != complexity.intValue()) {
				int prevCmx = complexity.getValue();
				complexity.setValue(cmx);

				if (cmx < RadioSystem.VARIABLES && prevCmx < RadioSystem.VARIABLES) {
					List<int[]> lCmx = varList.stream()
							.filter(entry -> entry.length == prevCmx)
							.collect(Collectors.toList());

					Std.out("\n\n<h4 class='nopad'>Наилучшие:</h4>");
					for (int k = 0; k <= prevCmx; ++k) {
						int mPow = (prevCmx * RadioSystem.MASK_DEPTH) - k;
						final int fk = k;

						int[] bestMPow = lCmx.stream()
								.min((arr1, arr2) -> Double.compare(globalOpt.get(arr1).get(fk).get(0).H,
										globalOpt.get(arr2).get(fk).get(0).H))
								.get();

						HS opt = globalOpt.get(bestMPow).get(k).get(0);
						int smp = unsortedKeys.indexOf(bestMPow);

						String vStr = "";
						for (int vvi : bestMPow)
							vStr += "V" + vvi + " ";
						vStr = vStr.substring(0, vStr.length() - 1);

						Std.out("  |M| = %d: <a href='#sys-%d-%d'>Система #%d упрощения #%d переменных [%s]</a>; H(G | E x G_) = %f",
								mPow,
								smp, opt.i,
								opt.i, smp, vStr, opt.H);
					}
				}

				if (cmx == 0)
					return;

				cmpChanged = true;
				if (cmx != RadioSystem.VARIABLES)
					Std.o("\n\n\n\n\n");
				Std.out("<h3 class='nopad'>Сложность: %d</h3>", cmx);
			}

			if (!cmpChanged)
				Std.o("\n\n");
			Std.o("<h4 class='nopad'>Переменные: { ");
			for (int idx : allowedVars)
				Std.o("V" + (idx) + " ");
			Std.out("}</h3>");
			Map<Integer, List<HS>> h = globalOpt.get(allowedVars);
			final int variables = cmx;
			HS hs;

			for (int k = 0; k <= variables; ++k) {
				hs = h.get(k).get(0);
				Std.out("  |M| = %d: <a href='#sys-%d-%d'>Система #%d</a>; H(G | E x G_) = %f",
						((variables * RadioSystem.MASK_DEPTH) - k),
						unsortedKeys.indexOf(allowedVars), hs.i,
						hs.i, hs.H);
			}
			int smx = unsortedKeys.indexOf(allowedVars);
			Std.out("<b><a href='#non-null-%d'>К системам с ненулевым H(G | E x G_) (%d систем)</a></b>\n", smx,
					nonNullQ.get(smx));
		});

		hr();
		Std.out("<h1 id='src-code'>Исходный код программы</h1>");
		Std.out("<h3>Написано на Java 12</h3>");
		Std.out("<h3>Зависимости:</h3>");
		Std.out("<ul>"
				+ "<li>commons-io-2.11.0</li>"
				+ "<li>commons-lang3-3.12.0</li>"
				+ "<li>guava-31.0.1</li>"
				+ "</ul>");
		Std.out("<h3>SystemsAnalysis.java</h3>\n"
				+ "<h3>RadioSystem.java</h3>\n"
				+ "<h3>V.java</h3>\n"
				+ "<h3>CombinationGenerator.java</h3>\n"
				+ "<h3>Std.java</h3>\n");
		Std.out("</pre></div>");
		System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));

		prependPrefix(new File("out.html"), head.toString());
	}

	public static void prependPrefix(File input, String prefix) throws IOException {
		LineIterator li = FileUtils.lineIterator(input);
		File tempFile = File.createTempFile("temp-out", ".tmp");
		BufferedWriter w = new BufferedWriter(new FileWriter(tempFile));
		try {
			w.write(prefix);
			while (li.hasNext()) {
				w.write(li.next());
				w.write("\n");
			}
		} finally {
			w.close();
			li.close();
		}
		File out = new File("comp_mod-lab2.html");
		if (out.exists())
			out.delete();
		input.delete();
		FileUtils.moveFile(tempFile, out);
	}

	private static void hr() {
		System.out.println(System.lineSeparator()
				+ "\n----------------------------------------------------------------------------------------------------\n");
	}

	private static void initDataSystem() {
		for (int i = 0; i < 50; ++i)
			system.B1.add(randNode());
	}

	static final String AB = "-0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz-";
	static Random random = new Random(42);

	static String randomString(int len) {
		StringBuilder sb = new StringBuilder(len);
		for (int i = 0; i < len; i++)
			sb.append(AB.charAt(random.nextInt(AB.length())));
		return sb.toString();
	}

	public static int rand(int min, int max) {
		return random.ints(min, max).findFirst().getAsInt();
	}

	static RadioSystem.Node randNode() {
		RadioSystem.Node node = new RadioSystem.Node().setName(randomString(5))
				.setInterferenceLoudness(rand(-50, 0)).setIOLatency(rand(0, 5000)).setTestDistance(rand(0, 50))
				.setMsgLength(V.MessageLength.values()[random.nextInt(3)]).setQuality();
		random.nextInt(5);
		return node;
	}

	static void set(List<Integer> list, int... elems) {
		list.clear();
		for (int elem : elems)
			list.add(elem);
	}

	public static ArrayList<String> readFile(String name) {
		ArrayList<String> lines = new ArrayList<String>();
		try {
			File file = new File(name);
			BufferedReader br = new BufferedReader(new FileReader(file));
			String l;
			while ((l = br.readLine()) != null) {
				lines.add(l.trim());
			}
			br.close();
			return lines;
		} catch (Exception e) {
			return null;
		}
	}

	public static int val(String str) {
		return Integer.parseInt(str);
	}

	public static ArrayList<Integer> parseList(String str) {
		if (!str.contains(","))
			str += ",";
		StringTokenizer list = new StringTokenizer(str, ",");
		ArrayList<Integer> ret = new ArrayList<Integer>();

		while (list.hasMoreTokens())
			ret.add(val(list.nextToken()));

		return ret;
	}

	static boolean contains(ArrayList<Integer> list, int a, int b) {
		return list.contains(a) && list.contains(b);
	}
}
