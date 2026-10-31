package pe.edu.utec.marketing.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pe.edu.utec.marketing.model.Network;

public class CampaignForm {

    @NotBlank(message = "Ingresa el nombre del negocio")
    @Size(max = 80)
    private String businessName;

    @Size(max = 300, message = "Maximo 300 caracteres")
    private String description;

    @NotNull(message = "Elige una red social")
    private Network network = Network.INSTAGRAM;

    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Network getNetwork() { return network; }
    public void setNetwork(Network network) { this.network = network; }
}
