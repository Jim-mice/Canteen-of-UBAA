package edu.buaa.canteen.domain.order;

public record ResourceRequirement(int chefCount, boolean sameWindowRequired) {
    public ResourceRequirement { if (chefCount <= 0) throw new IllegalArgumentException("chefCount must be positive"); }
}
