package org.mbi.nussinovrna;


import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@ToString
public final class UnorderedPair<E> {
    @Getter private final E first;
    @Getter private final E second;

    private UnorderedPair(final E a, final E b) {
        this.first = a;
        this.second = b;
    }

    public static <E> UnorderedPair<E> of(@NonNull final E a, @NonNull final E b) {
        return new UnorderedPair<>(a, b);
    }

    @Override
    public int hashCode() {
        return first.hashCode() + second.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null) {
            return false;
        }

        if (getClass() != other.getClass()) {
            return false;
        }

        if(!(other instanceof UnorderedPair)) {
            return false;
        }

        final UnorderedPair<?> otherPair = (UnorderedPair<?>) other;

        return (first.equals(otherPair.first) && second.equals(otherPair.second)) ||
                (first.equals(otherPair.second) && second.equals(otherPair.first));
    }
}