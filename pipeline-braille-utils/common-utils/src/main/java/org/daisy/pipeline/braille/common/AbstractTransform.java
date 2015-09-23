package org.daisy.pipeline.braille.common;

import java.util.concurrent.atomic.AtomicLong;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;
import com.google.common.base.Optional;
import com.google.common.collect.AbstractIterator;

import org.slf4j.Logger;

public abstract class AbstractTransform implements Transform {
	
	private final String id = "transform" + getUniqueId();
	
	public String getIdentifier() {
		return id;
	}
	
	private static AtomicLong i = new AtomicLong(0);
	
	private static long getUniqueId() {
		return i.incrementAndGet();
	}
	
	public ToStringHelper toStringHelper() {
		return Objects.toStringHelper(this);
	}
	
	@Override
	public String toString() {
		return toStringHelper().add("id", id).toString();
	}
	
	public static abstract class Provider<T extends Transform> implements Transform.Provider<T> {
		
		protected interface Iterable<T> extends WithSideEffect.util.Iterable<T,Logger>,
		                                        java.lang.Iterable<WithSideEffect<T,Logger>>{}
		
		private final static Pattern ID_FEATURE_RE = Pattern.compile(
			"\\s*\\(\\s*id\\s*\\:\\s*(?<id>[_a-zA-Z][_a-zA-Z0-9-]*)\\s*\\)\\s*"
		);
		
		private Map<String,Iterable<T>> transformCache = new HashMap<String,Iterable<T>>();
		
		private Map<Logger,Transform.Provider<T>> providerCache = new HashMap<Logger,Transform.Provider<T>>();
		
		protected abstract Iterable<T> _get(String query);
		
		private final java.lang.Iterable<T> get(String query, Logger context) {
			Matcher m = ID_FEATURE_RE.matcher(query);
			if (m.matches()) {
				String id = m.group("id");
				return Optional.fromNullable(fromId(id)).asSet(); }
			else {
				Iterable<T> i;
				if (transformCache.containsKey(query))
					i = transformCache.get(query);
				else {
					// memoize() doesn't make sense 
					i = util.Iterables.memoize(_get(query));
					transformCache.put(query, i); }
				return rememberId(i.apply(context)); }
		}
		
		public java.lang.Iterable<T> get(String query) {
			return get(query, null);
		}
		
		public final Transform.Provider<T> withContext(Logger context) {
			if (providerCache.containsKey(context))
				return providerCache.get(context);
			Transform.Provider<T> provider = new DerivativeProvider(context);
			providerCache.put(context, provider);
			return provider;
		}
		
		public void invalidateCache() {
			transformCache.clear();
		}
		
		public ToStringHelper toStringHelper() {
			return Objects.toStringHelper(this);
		}
		
		@Override
		public String toString() {
			return toStringHelper().add("context", null).toString();
		}
		
		private class DerivativeProvider implements Transform.Provider<T> {
			private final Logger context;
			private DerivativeProvider(final Logger context) {
				this.context = context;
			}
			public java.lang.Iterable<T> get(String query) {
				return Provider.this.get(query, context);
			}
			public Transform.Provider<T> withContext(Logger context) {
				return Provider.this.withContext(context);
			}
			@Override
			public String toString() {
				return Provider.this.toStringHelper().add("context", context).toString();
			}
		}
		
		private Map<String,T> fromId = new HashMap<String,T>();
		
		protected T fromId(String id) {
			return fromId.get(id);
		}
		
		private java.lang.Iterable<T> rememberId(final java.lang.Iterable<T> iterable) {
			final Map<String,T> fromId = this.fromId;
			return new java.lang.Iterable<T>() {
				public Iterator<T> iterator() {
					return new Iterator<T>() {
						Iterator<T> i = iterable.iterator();
						public boolean hasNext() {
							return i.hasNext();
						}
						public T next() {
							T t = i.next();
							fromId.put(t.getIdentifier(), t);
							return t;
						}
						public void remove() {
							i.remove();
						}
					};
				}
			};
		}
		
		public static abstract class util {
			
			public static <T extends Transform> WithSideEffect<T,Logger> logCreate(final T t) {
				return new WithSideEffect<T,Logger>() {
					public T _apply() {
						__apply(debug("Created " + t));
						return t; }};
			}
			
			public static <T extends Transform> Iterable<T> logSelect(final String query,
			                                                          final Transform.Provider<T> provider) {
				// not using provider.withContext() because memoizing only makes sense if sub-providers
				// have no side-effects and provided transformers have no context
				return logSelect(query, provider.withContext(null).get(query));
			}
			
			public static <T extends Transform> Iterable<T> logSelect(final String query,
			                                                          final java.lang.Iterable<T> iterable) {
				return Iterables.of(
					new java.lang.Iterable<WithSideEffect<T,Logger>>() {
						public Iterator<WithSideEffect<T,Logger>> iterator() {
							return new Iterator<WithSideEffect<T,Logger>>() {
								Iterator<T> i = iterable.iterator();
								boolean first = true;
								public boolean hasNext() {
									if (i == null)
										return true;
									return i.hasNext();
								}
								public WithSideEffect<T,Logger> next() {
									final T t;
									if (first) {
										first = false;
										try { t = i.next(); }
										catch (final NoSuchElementException e) {
											return new WithSideEffect<T,Logger>() {
												public T _apply() {
													__apply(debug("No match for query " + query));
													throw e;
												}
											};
										}
									} else
										t = i.next();
									return new WithSideEffect<T,Logger>() {
										public T _apply() {
											__apply(info("Selected " + t + " for query " + query));
											return t;
										}
									};
								}
								public void remove() {
									throw new UnsupportedOperationException();
								}
							};
						}
					}
				);
			}
			
			public static com.google.common.base.Function<Logger,Void> debug(final String message) {
				return new com.google.common.base.Function<Logger,Void>() {
					public Void apply(Logger logger) {
						if (logger != null)
							logger.debug(message);
						return null;
					}
				};
			}
			
			public static com.google.common.base.Function<Logger,Void> info(final String message) {
				return new com.google.common.base.Function<Logger,Void>() {
					public Void apply(Logger logger) {
						if (logger != null)
							logger.info(message);
						return null;
					}
				};
			}
			
			public static com.google.common.base.Function<Logger,Void> warn(final String message) {
				return new com.google.common.base.Function<Logger,Void>() {
					public Void apply(Logger logger) {
						if (logger != null)
							logger.warn(message);
						return null;
					}
				};
			}
			
			public static abstract class Function<F,T> extends WithSideEffect.util.Function<F,T,Logger> {}
			
			public static abstract class Iterables {
				
				public static <T> Provider.Iterable<T> memoize(Provider.Iterable<T> iterable) {
					return of(org.daisy.pipeline.braille.common.util.Iterables.memoize(iterable));
				}
				
				public static <T> Provider.Iterable<T> empty() {
					return of(Optional.<WithSideEffect<T,Logger>>absent().asSet());
				}
				
				public static <T> Provider.Iterable<T> fromNullable(T element) {
					return of(WithSideEffect.<T,Logger>fromNullable(element));
				}
				
				public static <T> Provider.Iterable<T> of(T element) {
					return of(WithSideEffect.<T,Logger>of(element));
				}
				
				public static <T> Provider.Iterable<T> of(WithSideEffect<T,Logger> element) {
					return of(Optional.of(element).asSet());
				}
				
				public static <T> Provider.Iterable<T> of(final java.lang.Iterable<WithSideEffect<T,Logger>> iterable) {
					return new of<T>(iterable);
				}
				
				private static class of<T> extends WithSideEffect.util.Iterables.of<T,Logger> implements Provider.Iterable<T> {
					protected of(java.lang.Iterable<WithSideEffect<T,Logger>> iterable) {
						super(iterable);
					}
					public Iterator<WithSideEffect<T,Logger>> iterator() {
						return iterable.iterator();
					}
				}
				
				public static <F,T> Provider.Iterable<T> transform(Provider.Iterable<F> from,
				                                                   final com.google.common.base.Function<F,T> function) {
					return transform(
						from,
						new Function<F,T>() {
							public T _apply(F from) {
								return function.apply(from);
							}
						}
					);
				}
				
				public static <F,T> Provider.Iterable<T> transform(Iterable<F> from, final Function<F,T> function) {
					return of(
						com.google.common.collect.Iterables.transform(
							from,
							new Function<WithSideEffect<F,Logger>,T>() {
								public T _apply(WithSideEffect<F,Logger> from) {
									return __apply(function.apply(__apply(from)));
								}
							}
						)
					);
				}
				
				public static <F,T> Provider.Iterable<T> transform(final java.lang.Iterable<F> from, final Function<F,T> function) {
					return of(
						com.google.common.collect.Iterables.transform(from, function)
					);
				}
				
				public static <T> Iterable<T> concat(Iterable<T> a, T b) {
					return of(
						com.google.common.collect.Iterables.concat(a, of(b))
					);
				}
				
				public static <T> Iterable<T> concat(Iterable<T> a, WithSideEffect<T,Logger> b) {
					return of(
						com.google.common.collect.Iterables.concat(a, of(b))
					);
				}
				
				public static <T> Iterable<T> concat(Iterable<T> a, Iterable<T> b) {
					return of(
						com.google.common.collect.Iterables.concat(a, b)
					);
				}
				
				public static <T> Iterable<T> concat(final java.lang.Iterable<? extends Iterable<T>> inputs) {
					return of(
						com.google.common.collect.Iterables.concat(inputs)
					);
				}
				
				public static <T> Iterable<T> concat(final Iterable<? extends Iterable<T>> inputs) {
					return new concat<T>() {
						protected Iterator<? extends Iterable<T>> iterator(Logger context) {
							return inputs.apply(context).iterator();
						}
						public Iterator<WithSideEffect<T,Logger>> iterator() {
							return new AbstractIterator<WithSideEffect<T,Logger>>() {
								Iterator<? extends WithSideEffect<? extends Iterable<T>,Logger>> iterableIterator = inputs.iterator();
								Iterator<WithSideEffect<T,Logger>> current;
								WithSideEffect<T,Logger> nextEvaluate;
								boolean evaluated = true;
								protected WithSideEffect<T,Logger> computeNext() {
									if (!evaluated)
										throw new RuntimeException("Previous element must be evaluated first");
									if (current != null && current.hasNext())
										return current.next();
									else if (!iterableIterator.hasNext())
										return endOfData();
									nextEvaluate = new WithSideEffect<T,Logger>() {
										public T _apply() throws Throwable {
											if (nextEvaluate == this)
												evaluated = true;
											while (current == null || !current.hasNext()) {
												try {
													current = __apply(iterableIterator.next()).iterator();
													break; }
												catch (WithSideEffect.Exception e) {
													continue; }}
											try {
												return __apply(current.next()); }
											catch (WithSideEffect.Exception e) {
												throw e.getCause(); }
										}
									};
									evaluated = false;
									return nextEvaluate;
								}
							};
						}
					};
				}
				
				protected static abstract class concat<T> extends WithSideEffect.util.Iterables.concat<T,Logger>
				                                          implements Iterable<T> {}
			}
		}
	}
}
