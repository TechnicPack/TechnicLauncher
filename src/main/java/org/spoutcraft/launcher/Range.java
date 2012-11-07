package org.spoutcraft.launcher;

import java.util.Iterator;

public class Range {
	public static Iterable<Integer> range(final int start, final int stop, final int step) {
		if (step <= 0) throw new IllegalArgumentException("step > 0 isrequired!");

		return new Iterable<Integer>() {
			@Override
			public Iterator<Integer> iterator() {
				return new Iterator<Integer>() {
					private int	counter	= start;

					@Override
					public boolean hasNext() {
						return counter < stop;
					}

					@Override
					public Integer next() {
						try {
							return counter;
						} finally {
							counter += step;
						}
					}

					@Override
					public void remove() {
					}
				};
			}
		};
	}

        @SuppressWarnings("rawtypes")
	public static Iterable range(final int start, final int stop) {
		return range(start, stop, 1);
	}

        @SuppressWarnings("rawtypes")
	public static Iterable range(final int stop) {
		return range(0, stop, 1);
	}

	public static Integer[] rangeArray(final int stop) {
		Integer[] array = new Integer[stop];
		for (int i = 0; i < stop; i++)
			array[i] = Integer.valueOf(i);
		return array;
	}
}