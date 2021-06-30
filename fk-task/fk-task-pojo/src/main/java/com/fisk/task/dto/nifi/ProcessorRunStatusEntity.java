package com.fisk.task.dto.nifi;

import com.davis.client.model.RevisionDTO;

/**
 * @author gy
 */
public class ProcessorRunStatusEntity {
    public String state;
    public boolean disconnectedNodeAcknowledged;
    public RevisionDTO revision;
}
