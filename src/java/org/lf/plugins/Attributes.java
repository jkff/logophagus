package org.lf.plugins;

/**
 * User: jkff
 * Date: Jan 26, 2010
 * Time: 3:45:26 PM
 */
public abstract class Attributes {
    public static final Attributes NONE = new Attributes() {
        public <T> T getValue(Class<T> attributeClass) {
            return null;
        }
    };

    public interface Combiner<T> {
        T combine(T a, T b);
    }

    public static <T> Attributes with(final Attributes base, final Class<T> clazz,
                                      final T newValue, final Combiner<T> combiner)
    {
        return new Attributes() {
            @Override
            public <U> U getValue(Class<U> attributeClass) {
                if(attributeClass != clazz)
                    return base.getValue(attributeClass);
                T oldValue = base.getValue(clazz);
                return (U) (oldValue==null ? newValue : combiner.combine(oldValue, newValue));
            }
        };
    }

    public abstract <T> T getValue(Class<T> attributeClass);
}
