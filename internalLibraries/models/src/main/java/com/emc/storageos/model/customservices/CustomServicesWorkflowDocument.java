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
package com.emc.storageos.model.customservices;

import java.net.URI;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * JAXB model for Custom Services workflow Definition
 */

@XmlRootElement(name = "workflow_document")
public class CustomServicesWorkflowDocument {

    public static final long DEFAULT_STEP_TIMEOUT = 600000; // Setting default to 10 mins

    private String name;
    private String description;
    private List<Step> steps;

    @XmlElement(name = "name", nillable = true)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlElement(name = "description", nillable = true)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @XmlElementWrapper(name = "steps")
    @XmlElement(name = "step")
    public List<Step> getSteps() {
        return steps;
    }

    public void setSteps(List<Step> steps) {
        this.steps = steps;
    }

    public static class InputGroup {

        private List<Input> inputGroup;

        @XmlElement(name = "input")
        public List<Input> getInputGroup() {
            return inputGroup;
        }

        public void setInputGroup(List<Input> inputGroup) {
            this.inputGroup = inputGroup;
        }
    }

    public static class Input {

        private String name;
        // type of CustomServicesConstants.InputType
        private String type;
        private String step;
        private String friendlyName;
        private String defaultValue;
        private String value;
        private String group;
        private String description;
        // type of the value e.g (STRING, INTEGER ...etc)
        private String inputFieldType;
        private String tableName;
        private boolean required = true;
        private boolean locked = false;
        // Use this to set "key,value" pairs for type "InputFromUserMulti"
        private Map<String, String> options;

        @XmlElement(name = "name", nillable = true)
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @XmlElement(name = "type", nillable = true)
        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        @XmlElement(name = "step", nillable = true)
        public String getStep() {
            return step;
        }

        public void setStep(String step) {
            this.step = step;
        }

        @XmlElement(name = "friendly_name", nillable = true)
        public String getFriendlyName() {
            return friendlyName;
        }

        public void setFriendlyName(String friendlyName) {
            this.friendlyName = friendlyName;
        }

        @XmlElement(name = "default_value", nillable = true)
        public String getDefaultValue() {
            return defaultValue;
        }

        public void setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
        }

        @XmlElement(name = "value", nillable = true)
        public String getValue() {
            return value;
        }

        public void setValue(String assetValue) {
            this.value = assetValue;
        }

        @XmlElement(name = "group", nillable = true)
        public String getGroup() {
            return group;
        }

        public void setGroup(String group) {
            this.group = group;
        }

        @XmlElement(name = "required")
        public boolean getRequired() {
            return required;
        }

        public void setRequired(boolean required) {
            this.required = required;
        }

        @XmlElement(name = "locked")
        public boolean getLocked() {
            return locked;
        }

        public void setLocked(boolean locked) {
            this.locked = locked;
        }

        @XmlElement(name = "description", nillable = true)
        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        @XmlElement(name = "input_field_type", nillable = true)
        public String getInputFieldType() {
            return inputFieldType;
        }

        public void setInputFieldType(String inputfieldtype) {
            this.inputFieldType = inputfieldtype;
        }

        @XmlElement(name = "table_name", nillable = true)
        public String getTableName() {
            return tableName;
        }

        public void setTableName(String tablename) {
            this.tableName = tablename;
        }

        @XmlElementWrapper(name = "options")
        public Map<String, String> getOptions() {
            return options;
        }

        public void setOptions(Map<String, String> options) {
            this.options = options;
        }
    }

    public static class Output {

        private String name;
        private String type;
        private String table;

        @XmlElement(name = "name", nillable = true)
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @XmlElement(name = "type", nillable = true)
        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        @XmlElement(name = "table", nillable = true)
        public String getTable() {
            return table;
        }

        public void setTable(String table) {
            this.table = table;
        }
    }

    public static class StepAttribute {

        private boolean waitForTask = true;
        private long timeout = DEFAULT_STEP_TIMEOUT;

        @XmlElement(name = "wait_for_task")
        public boolean getWaitForTask() {
            return waitForTask;
        }

        public void setWaitForTask(boolean waitForTask) {
            this.waitForTask = waitForTask;
        }

        @XmlElement(name = "timeout")
        public long getTimeout() {
            return timeout;
        }

        public void setTimeout(long timeout) {
            this.timeout = timeout;
        }

    }

    public static class Step {

        private String id;
        private String friendlyName;
        private URI operation;
        private String description;
        private Integer positionX;
        private Integer positionY;
        private String type;
        private Map<String, InputGroup> inputGroups;
        private List<Output> output;
        private StepAttribute attributes;
        private String successCriteria;
        private NextStep next;

        @XmlElement(name = "id", required = true)
        public String getId() {
            return id;
        }

        public void setId(String stepId) {
            this.id = stepId;
        }

        //ALl steps should have friendly_name
        @XmlElement(name = "friendly_name", required = true)
        public String getFriendlyName() {
            return friendlyName;
        }

        public void setFriendlyName(String friendlyName) {
            this.friendlyName = friendlyName;
        }

        @XmlElement(name = "position_y")
        public Integer getPositionY() {
            return positionY;
        }

        public void setPositionY(Integer positionY) {
            this.positionY = positionY;
        }

        @XmlElement(name = "position_x")
        public Integer getPositionX() {
            return positionX;
        }

        public void setPositionX(Integer positionX) {
            this.positionX = positionX;
        }

        @XmlElement(name = "operation")
        public URI getOperation() {
            return operation;
        }

        public void setOperation(URI operation) {
            this.operation = operation;
        }

        @XmlElement(name = "description", nillable = true)
        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        //Start and end does not have type
        @XmlElement(name = "type", nillable = true)
        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        @XmlElementWrapper(name = "inputGroups")
        public Map<String, InputGroup> getInputGroups() {
            return inputGroups;
        }

        public void setInputGroups(Map<String, InputGroup> inputGroups) {
            this.inputGroups = inputGroups;
        }

        @XmlElement(name = "output")
        public List<Output> getOutput() {
            return output;
        }

        public void setOutput(List<Output> output) {
            this.output = output;
        }

        @XmlElement(name = "attributes")
        public StepAttribute getAttributes() {
            return attributes;
        }

        public void setAttributes(StepAttribute attributes) {
            this.attributes = attributes;
        }

        @XmlElement(name = "success_criteria", nillable = true)
        public String getSuccessCriteria() {
            return successCriteria;
        }

        public void setSuccessCriteria(String successCriteria) {
            this.successCriteria = successCriteria;
        }

        @XmlElement(name = "next")
        public NextStep getNext() {
            return next;
        }

        public void setNext(NextStep next) {
            this.next = next;
        }
    }

    public static class NextStep {
        private String defaultStep;
        private String failedStep;

        //End does not have next steps
        @XmlElement(name = "default", nillable = true)
        public String getDefaultStep() {
            return defaultStep;
        }

        public void setDefaultStep(String defaultStep) {
            this.defaultStep = defaultStep;
        }

        @XmlElement(name = "failed", nillable = true)
        public String getFailedStep() {
            return failedStep;
        }

        public void setFailedStep(String failedStep) {
            this.failedStep = failedStep;
        }
    }
}
