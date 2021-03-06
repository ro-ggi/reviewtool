package de.setsoftware.reviewtool.model.api;

/**
 * Encapsulates a single difference between two revisions of a file, i.e. a pair (source fragment, target fragment).
 * <p/>
 * Hunks can be ordered according to their source fragment.
 */
public interface IHunk extends Comparable<IHunk> {

    /**
     * @return The source fragment.
     */
    public abstract IFragment getSource();

    /**
     * @return The target fragment.
     */
    public abstract IFragment getTarget();

    /**
     * @return The hunk delta.
     */
    public abstract IDelta getDelta();

    /**
     * @return {@code true} if this is an in-line hunk.
     */
    public abstract boolean isInline();

    /**
     * Creates a new hunk whose source fragment's start and end positions are shifted by the given delta.
     * @param delta The delta to add.
     * @return The resulting hunk.
     */
    public abstract IHunk adjustSource(IDelta delta);

    /**
     * Creates a new hunk whose target fragment's start and end positions are shifted by the given delta.
     * @param delta The delta to add.
     * @return The resulting hunk.
     */
    public abstract IHunk adjustTarget(IDelta delta);

    /**
     * Creates a new hunk whose source fragment's file is set to the one passed.
     * @param source The {@link IRevisionedFile} to use.
     * @return The resulting hunk.
     */
    public abstract IHunk adjustSourceFile(IRevisionedFile source);

    /**
     * Creates a new hunk whose target fragment's file is set to the one passed.
     * @param target The {@link IRevisionedFile} to use.
     * @return The resulting hunk.
     */
    public abstract IHunk adjustTargetFile(IRevisionedFile target);

}
