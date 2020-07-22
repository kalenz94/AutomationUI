package model;

import java.io.Serializable;
import java.util.List;

public class CapabilitySet implements Serializable {
    private List<Capability> capabilities;
    private String name;

    public List<Capability> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(List<Capability> capabilities) {
        this.capabilities = capabilities;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
