package com.github.jmsoft.socketclient.interfaces;

import java.sql.SQLException;

/**
 * Generic operations for activities
 */
public interface ActivityGenericsInterface {

    public void initializeUIComponents() throws SQLException;

    public void getIntentValues();
}
