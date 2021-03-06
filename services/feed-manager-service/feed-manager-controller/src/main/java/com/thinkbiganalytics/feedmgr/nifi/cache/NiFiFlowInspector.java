package com.thinkbiganalytics.feedmgr.nifi.cache;
/*-
 * #%L
 * thinkbig-feed-manager-controller
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.thinkbiganalytics.nifi.rest.client.NiFiRestClient;

import org.apache.nifi.web.api.dto.flow.ProcessGroupFlowDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Call out to NiFi and inspect the contents of a process group
 */
public class NiFiFlowInspector {
    private static final Logger log = LoggerFactory.getLogger(NiFiFlowInspector.class);

    private NiFiRestClient restClient;
    private String processGroupId;
    NiFiFlowInspection parent;
    int level;

    public NiFiFlowInspector(String processGroupId, int level, NiFiFlowInspection parent, NiFiRestClient restClient) {
        this.processGroupId = processGroupId;
        this.restClient = restClient;
        this.level = level;
        this.parent = parent;
    }

    /**
     * Inspects the process group
     * @return the inspection result with the contents of the process group
     */
    public NiFiFlowInspection inspect() {
        NiFiFlowInspection inspection = new NiFiFlowInspection(processGroupId, level, parent, Thread.currentThread().getName());
        long start = System.currentTimeMillis();
      //  log.debug("inspecting process group: {} on thread {} ",processGroupId,Thread.currentThread().getName());
        ProcessGroupFlowDTO flow = restClient.processGroups().flow(processGroupId);
        if (flow != null) {
            inspection.setProcessGroupName(flow.getBreadcrumb().getBreadcrumb().getName());
            flow.getFlow().getProcessGroups().stream().forEach(processGroupEntity -> {
                inspection.addGroupToInspect(processGroupEntity.getId());
            });
            inspection.setProcessGroupFlow(flow);
        }
        inspection.setTime(System.currentTimeMillis() - start);
        return inspection;
    }
}
