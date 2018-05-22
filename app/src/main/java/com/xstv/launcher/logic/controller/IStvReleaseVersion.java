package com.xstv.launcher.logic.controller;

/**
 * Created by xubin on 16-12-7.
 * <p>
 * Copyright 2016 STV.
 */
public interface IStvReleaseVersion {

    /**
     * Called when the rom has downgrade. The implementation
     * should use this method to do anything needs to Downgrade to the low schema version.
     *
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
    void onStvDowngrade(String oldVersion, String newVersion);

    /**
     * Called when the rom has upgraded. The implementation
     * should use this method to do anything needs to upgrade to the new schema version.
     *
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
    void onStvUpgrade(String oldVersion, String newVersion);
}
