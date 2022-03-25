package combinations;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import radiocommsystem.Std;

public class CombinationGenerator<T> {
	private ImmutableSet<T> elements;
	private List<List<T>> combinations;

	@SafeVarargs
	public CombinationGenerator(T... elements) {
		this(ImmutableSet.copyOf(elements));
	}

	public CombinationGenerator(List<T> elements) {
		this(ImmutableSet.copyOf(elements));
	}

	public CombinationGenerator(ImmutableSet<T> elements) {
		this.elements = elements;
		reset();
	}

	CombinationGenerator<T> setElements(List<T> elements) {
		this.elements = ImmutableSet.copyOf(elements);
		return this;
	}
	
	public void reset() {
		combinations = new ArrayList<>();
	}

	public List<List<T>> generate(int r) {
		Set<Set<T>> combs = Sets.combinations(elements, r);
		List<List<T>> combList = new ArrayList<>();
		combs.forEach(comb -> combList.add(new ArrayList<>(comb)));
		combinations.addAll(combList);
		return combList;
	}

	public void forEach(Consumer<List<T>> combConsumer) {
		for (List<T> comb : combinations)
			combConsumer.accept(comb);
	}

	public List<T> elements() {
		return elements.asList();
	}

	public List<List<T>> combinations() {
		return combinations;
	}

	@SafeVarargs
	public static <T> CombinationGenerator<T> make(T... elements) {
		return new CombinationGenerator<>(elements);
	}

	public static <T> CombinationGenerator<T> make(List<T> elements) {
		return new CombinationGenerator<>(elements);
	}
}
