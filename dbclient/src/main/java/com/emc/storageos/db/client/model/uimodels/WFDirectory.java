/*
 * Copyright 2016 Dell Inc. or its subsidiaries.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.emc.storageos.db.client.model.uimodels;

import com.emc.storageos.db.client.model.*;

import java.net.URI;

/**
 * DB model class for holding workflow directory structure.
 */
@Cf("WFDirectory")
public class WFDirectory extends ModelObject {

    private URI _parent;
    private StringSet _workflows;


    @RelationIndex(cf = "RelationIndex", type = WFDirectory.class)
    @Name("parent")
    public URI getParent() {
        return _parent;
    }

    public void setParent(URI parent) {
        this._parent = parent;
        setChanged("parent");
    }

    @Name("workflows")
    public StringSet getWorkflows() {
        return _workflows;
    }

    public void setWorkflows(StringSet workflows) {

        this._workflows = workflows;
        setChanged("workflows");
    }
}
