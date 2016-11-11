package com.thinkbiganalytics.nifi.v1.rest.client;

import com.thinkbiganalytics.nifi.rest.client.NiFiProcessorsRestClient;
import com.thinkbiganalytics.nifi.rest.client.NifiComponentNotFoundException;
import com.thinkbiganalytics.nifi.rest.support.NifiConstants;

import org.apache.nifi.web.api.dto.ProcessorDTO;
import org.apache.nifi.web.api.dto.RevisionDTO;
import org.apache.nifi.web.api.entity.ProcessorEntity;

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.ws.rs.NotFoundException;

/**
 * Implements a {@link NiFiProcessorsRestClient} for communicating with NiFi v1.0.
 */
public class NiFiProcessorsRestClientV1 implements NiFiProcessorsRestClient {

    /** Base path for processor requests */
    private static final String BASE_PATH = "/processors/";

    /** REST client for communicating with NiFi */
    private final NiFiRestClientV1 client;

    /**
     * Constructs a {@code NiFiProcessorsRestClientV1} with the specified NiFi REST client.
     *
     * @param client the REST client
     */
    public NiFiProcessorsRestClientV1(@Nonnull final NiFiRestClientV1 client) {
        this.client = client;
    }

    @Nonnull
    @Override
    public Optional<ProcessorDTO> findById(@Nonnull final String processGroupId, @Nonnull final String processorId) {
        return findEntityById(processorId).map(ProcessorEntity::getComponent);
    }

    @Nonnull
    @Override
    public ProcessorDTO update(@Nonnull final ProcessorDTO processor) {
        return findEntityById(processor.getId())
                .flatMap(current -> {
                    final ProcessorEntity entity = new ProcessorEntity();
                    entity.setComponent(processor);

                    final RevisionDTO revision = new RevisionDTO();
                    revision.setVersion(current.getRevision().getVersion());
                    entity.setRevision(revision);

                    try {
                        return Optional.of(client.put(BASE_PATH + processor.getId(), entity, ProcessorEntity.class).getComponent());
                    } catch (final NotFoundException e) {
                        return Optional.empty();
                    }
                })
                .orElseThrow(() -> new NifiComponentNotFoundException(processor.getId(), NifiConstants.NIFI_COMPONENT_TYPE.PROCESSOR, null));
    }

    /**
     * Gets a processor entity.
     *
     * @param id the processor id
     * @return the processor entity, if found
     */
    @Nonnull
    private Optional<ProcessorEntity> findEntityById(@Nonnull final String id) {
        try {
            return Optional.ofNullable(client.get(BASE_PATH + id, null, ProcessorEntity.class));
        } catch (final NotFoundException e) {
            return Optional.empty();
        }
    }
}