package com.fisk.common.core.utils.jcoutils;

import com.sap.conn.jco.ext.DestinationDataEventListener;
import com.sap.conn.jco.ext.DestinationDataProvider;

import java.util.Properties;

public class MyDestinationDataProvider implements DestinationDataProvider {
    private Properties destinationProps;

    public void addDestination(String destinationName, Properties destinationProps) {
        this.destinationProps = destinationProps;
    }

    @Override
    public Properties getDestinationProperties(String destinationName) {
        return destinationProps;
    }

    @Override
    public void setDestinationDataEventListener(DestinationDataEventListener listener) {
    }

    @Override
    public boolean supportsEvents() {
        return true;
    }
}