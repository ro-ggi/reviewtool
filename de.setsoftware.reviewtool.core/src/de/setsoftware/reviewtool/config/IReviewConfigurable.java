package de.setsoftware.reviewtool.config;

import de.setsoftware.reviewtool.model.IReviewPersistence;
import de.setsoftware.reviewtool.model.api.IChangeSource;
import de.setsoftware.reviewtool.model.changestructure.IIrrelevanceDetermination;
import de.setsoftware.reviewtool.preferredtransitions.api.IPreferredTransitionStrategy;
import de.setsoftware.reviewtool.ui.IStopViewer;
import de.setsoftware.reviewtool.ui.dialogs.EndReviewExtension;

/**
 * Interface to decouple the dynamically configurable review plugin from the
 * concrete implementations of the configurators.
 */
public interface IReviewConfigurable {

    /**
     * Sets the strategy used to load and store review data.
     */
    public abstract void setPersistence(IReviewPersistence persistence);

    /**
     * Adds a source for changes.
     */
    public abstract void setChangeSource(IChangeSource changeSource);

    /**
     * Adds an extension for the end review dialog.
     */
    public abstract void addEndReviewExtension(EndReviewExtension extension);

    /**
     * Sets a stop viewer.
     */
    public abstract void setStopViewer(IStopViewer stopViewer);

    /**
     * Adds a task that is called in the UI thread as soon as configuration is finished.
     */
    public void addPostInitTask(Runnable r);

    /**
     * Adds a strategy to determine the preferred end transition.
     */
    public abstract void addPreferredTransitionStrategy(IPreferredTransitionStrategy strategy);

    /**
     * Adds a strategy to determine irrelevant fragments.
     */
    public abstract void addIrrelevanceStrategy(IIrrelevanceDetermination strategy);

}
