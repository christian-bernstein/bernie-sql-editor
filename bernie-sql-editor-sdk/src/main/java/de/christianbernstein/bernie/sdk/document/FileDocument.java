/*
 * Copyright (C) 2021 Christian Bernstein
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package de.christianbernstein.bernie.sdk.document;

import de.christianbernstein.bernie.sdk.misc.Resource;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.File;

/**
 * todo replace with better solution -> DB kv-store, file-based solution
 *
 * @author Christian Bernstein
 */
@Deprecated
@EqualsAndHashCode(callSuper = true)
@Data
public class FileDocument extends Document {

    private final File file;

    private final Resource<Document> resource;

    public FileDocument() {
        this.file = null;
        this.resource = null;
    }

    public FileDocument(File file) {
        this.file = file;
        this.resource = new Resource<>(this.file);
        final Document document = this.resource.load();
    }

    {
        this.registerListenerAdapter(new IListenerAdapter<>() {

            @Override
            public void onEntryAddedEvent(Events.EntryAddedEvent<Document> event) {
                resource.save(event.getContext());
            }

            @Override
            public void onEntryRemovedEvent(Events.EntryRemovedEvent<Document> event) {
                resource.save(event.getContext());
            }
        });
    }
}
