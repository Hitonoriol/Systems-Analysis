package radiocommsystem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

public class CombinationGenerator {
	private ImmutableSet<Integer> elements;
	private List<int[]> combinations;

	public CombinationGenerator(List<Integer> elements) {
		this.elements = ImmutableSet.copyOf(elements);
		reset();
	}

	public CombinationGenerator(int start, int n) {
		this(IntStream.range(start, n + 1).boxed().collect(Collectors.toList()));
	}

	public CombinationGenerator(int... elements) {
		this(Arrays.stream(elements).boxed().collect(Collectors.toList()));
	}

	public void reset() {
		combinations = new ArrayList<>();
	}

	public CombinationGenerator generate(int r) {
		Set<Set<Integer>> combs = Sets.combinations(elements, r);
		combs.forEach(comb -> {
			int[] arr = new int[comb.size()];
			int i = 0;
			for (int elem : comb)
				arr[i++] = elem;
			combinations.add(arr);
		});
		return this;
	}

	public void forEach(Consumer<int[]> combConsumer) {
		for (int[] comb : combinations)
			combConsumer.accept(comb);
	}

	public List<int[]> combinations() {
		return combinations;
	}
}
