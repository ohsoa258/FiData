//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.davis.client.model;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.swagger.annotations.ApiModelProperty;
import java.io.IOException;
import java.util.Objects;

public class ProcessorRunStatusEntity {
    @SerializedName("revision")
    private RevisionDTO revision = null;
    @SerializedName("state")
    private StateEnum state = null;
    @SerializedName("disconnectedNodeAcknowledged")
    private Boolean disconnectedNodeAcknowledged = null;

    public ProcessorRunStatusEntity() {
    }

    public ProcessorRunStatusEntity revision(RevisionDTO revision) {
        this.revision = revision;
        return this;
    }

    @ApiModelProperty("The revision for this request/response. The revision is required for any mutable flow requests and is included in all responses.")
    public RevisionDTO getRevision() {
        return this.revision;
    }

    public void setRevision(RevisionDTO revision) {
        this.revision = revision;
    }

    public ProcessorRunStatusEntity state(StateEnum state) {
        this.state = state;
        return this;
    }

    @ApiModelProperty("The run status of the Processor.")
    public StateEnum getState() {
        return this.state;
    }

    public void setState(StateEnum state) {
        this.state = state;
    }

    public ProcessorRunStatusEntity disconnectedNodeAcknowledged(Boolean disconnectedNodeAcknowledged) {
        this.disconnectedNodeAcknowledged = disconnectedNodeAcknowledged;
        return this;
    }

    @ApiModelProperty("Acknowledges that this node is disconnected to allow for mutable requests to proceed.")
    public Boolean isDisconnectedNodeAcknowledged() {
        return this.disconnectedNodeAcknowledged;
    }

    public void setDisconnectedNodeAcknowledged(Boolean disconnectedNodeAcknowledged) {
        this.disconnectedNodeAcknowledged = disconnectedNodeAcknowledged;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            ProcessorRunStatusEntity processorRunStatusEntity = (ProcessorRunStatusEntity)o;
            return Objects.equals(this.revision, processorRunStatusEntity.revision) && Objects.equals(this.state, processorRunStatusEntity.state) && Objects.equals(this.disconnectedNodeAcknowledged, processorRunStatusEntity.disconnectedNodeAcknowledged);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.revision, this.state, this.disconnectedNodeAcknowledged});
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ProcessorRunStatusEntity {\n");
        sb.append("    revision: ").append(this.toIndentedString(this.revision)).append("\n");
        sb.append("    state: ").append(this.toIndentedString(this.state)).append("\n");
        sb.append("    disconnectedNodeAcknowledged: ").append(this.toIndentedString(this.disconnectedNodeAcknowledged)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(Object o) {
        return o == null ? "null" : o.toString().replace("\n", "\n    ");
    }

    @JsonAdapter(StateEnum.Adapter.class)
    public static enum StateEnum {
        RUNNING("RUNNING"),
        STOPPED("STOPPED"),
        DISABLED("DISABLED"),
        RUN_ONCE("RUN_ONCE");
        private String value;

        private StateEnum(String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }

        public String toString() {
            return String.valueOf(this.value);
        }

        public static StateEnum fromValue(String text) {
            StateEnum[] var1 = values();
            int var2 = var1.length;

            for(int var3 = 0; var3 < var2; ++var3) {
                StateEnum b = var1[var3];
                if (String.valueOf(b.value).equals(text)) {
                    return b;
                }
            }

            return null;
        }

        public static class Adapter extends TypeAdapter<StateEnum> {
            public Adapter() {
            }

            public void write(JsonWriter jsonWriter, StateEnum enumeration) throws IOException {
                jsonWriter.value(enumeration.getValue());
            }

            public StateEnum read(JsonReader jsonReader) throws IOException {
                String value = jsonReader.nextString();
                return ProcessorRunStatusEntity.StateEnum.fromValue(String.valueOf(value));
            }
        }
    }
}
