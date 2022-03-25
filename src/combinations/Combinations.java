package combinations;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableInt;

import radiocommsystem.Std;

public class Combinations {

	public static void showCombs(int n, CombinationGenerator<String> combs, boolean ordered) {
		var elems = new ArrayList<>(combs.elements());
		String elemsStr = elems.stream().collect(Collectors.joining());
		Std.out("<h2>%s V%d = %s; m = %d</h2>", ordered ? "Впорядковане" : "Невпорядковане", n,
				Arrays.toString(elems.toArray()), elems.size());

		MutableInt ci = new MutableInt();
		Set<String> usedCombs = new HashSet<>();
		final int m = elems.size();
		IntStream.range(0, m)
				.map(i -> m - i)
				.forEach(i -> {
					Std.out("\n<h3>k = %d</h3>", i);

					List<List<Integer>> possibleSums = new ArrayList<>();
					for (List<Integer> sum : new SumIterator(m)) {
						if (sum.size() == i)
							possibleSums.add(sum);
					}

					if (m == 5 && i == 3)
						possibleSums.add(Arrays.asList(2, 2, 1));

					if (m == 4 && i == 2)
						possibleSums.add(Arrays.asList(2, 2));

					possibleSums.forEach(sumVariant -> {
						// Std.out("\tSum partition: %s", Arrays.toString(sumVariant.toArray()));
						List<List<String>> interm = new ArrayList<>();
						for (int term : sumVariant)
							interm.addAll(combs.generate(term));

						List<String> newAlphabet = new ArrayList<>();
						interm.forEach(iComb -> {
							if (ordered)
								Collections.sort(iComb, (s1, s2) -> {
									return Integer.compare(elemsStr.indexOf(s1), elemsStr.indexOf(s2));
								});

							for (int x = 0; x < iComb.size(); ++x)
								if (x + 1 < iComb.size() && ordered
										&& Math.abs(elemsStr.indexOf(iComb.get(x))
												- elemsStr.indexOf(iComb.get(x + 1))) != 1)
									return;

							newAlphabet.add(iComb.stream().collect(Collectors.joining()));
						});
						var intermCombs = CombinationGenerator.make(newAlphabet);
						// Std.out("Vocabulary: %s", Arrays.toString(intermCombs.elements().toArray()));
						intermCombs.generate(i);
						MutableInt lineBr = new MutableInt();
						Std.out("<table>");
						intermCombs.forEach(iComb -> {
							Collections.sort(iComb, (s1, s2) -> {
								return Integer.compare(elemsStr.indexOf(s1), elemsStr.indexOf(s2));
							});
							String combStr = "";
							for (String elem : iComb)
								combStr += "[" + elem + "]";

							String tmp = combStr.replaceAll("\\[", "").replaceAll("\\]", "");
							if (tmp.length() != m)
								return;
							for (String elem : elems)
								if (StringUtils.countMatches(tmp, elem) > 1) {
									return;
								}

							if (lineBr.intValue() == 0)
								Std.out("<tr>");
							
							if (usedCombs.add(combStr)) {
								Std.o("<td>\t<b>%d. %s</b></td>", ci.incrementAndGet(), combStr);
								if (lineBr.incrementAndGet() % 5 == 0) {
									Std.out("</tr>");
									lineBr.setValue(0);
								}
							}
						});
						Std.out("</table>");
						lineBr.setValue(0);
					});

				});
	}

	static String delim = "******************************************************************************************";

	public static void main(String args[]) throws FileNotFoundException {
		System.setOut(new PrintStream("comp_mod-lab3.html"));
		Std.out("<meta charset = 'utf-8'><style>"
				+ "pre {font-size:14px;}"
				+ "table, tr, td {border: 1px solid black; font-size: 16px;}"
				+ "a {size: 17px; color: #0048ad; text-decoration: none}"
				+ "a:hover {text-decoration: underline}"
				+ ".nopad {margin: 0; padding: 0;}"
				+ "</style><center><div style='width:60%;text-align:left;'><pre>"
				+ "<h1>Дiаграми Хассе</h1>"
				+ "<h2>1. Для заданих змiнних</h2>");
		Std.out("%s\n", delim);
		showCombs(1, CombinationGenerator.make("a", "b", "c", "d", "e"), false);
		Std.out("\n\n%s\n", delim);
		showCombs(1, CombinationGenerator.make("1", "2", "3", "4", "5"), true);
		Std.out("\n\n%s\n", delim);

		Std.out("\n\n\n\n\n<h2>2. Для змiнних побудованої ранiше системи</h2>");
		Std.out("<h2>Економiчна система</h2>");
		Std.out("\n<h2>Якість товару:</h2>");
		showCombs(1, CombinationGenerator.make("Й", "Ц", "У"), true);

		Std.out("\n\n%s\n<h2>Популярність товару:</h2>", delim);
		showCombs(2, CombinationGenerator.make("К", "Е", "Н"), true);

		Std.out("\n\n%s\n<h2>Рівень ціни відносно інших ринків:</h2>", delim);
		showCombs(3, CombinationGenerator.make("Ф", "Ы", "В"), false);

		Std.out("\n\n%s\n<h2>Приблизна очікуваність кількості умовних одиниць товару за рейтингом:</h2>", delim);
		showCombs(4, CombinationGenerator.make("И", "Т", "Ь"), false);
	}
}
