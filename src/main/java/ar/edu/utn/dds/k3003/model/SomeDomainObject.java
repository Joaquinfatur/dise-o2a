package ar.edu.utn.dds.k3003.model;

import ar.edu.utn.dds.k3003.model.exceptions.SomeDomainException;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Objects;

@Data
@AllArgsConstructor
public class SomeDomainObject {

    private final String anAttribute;
    private final Long otherAttribute;

    public SomeDomainObject(String par, long par1) {
        this.anAttribute = par;
        this.otherAttribute = par1;
    }

    public SomeDomainObject sum(SomeDomainObject other) {
        if (Objects.isNull(other.getAnAttribute())) {
            throw new SomeDomainException("anAttribute is null", other);
        }
        return new SomeDomainObject(
                this.anAttribute + other.getAnAttribute(),
                this.otherAttribute + other.getOtherAttribute()
        );
    }

    public String getAnAttribute() {
        return anAttribute;
    }

    public Long getOtherAttribute() {
        return otherAttribute;
    }
}
