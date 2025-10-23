package fr.eriniumgroup.erinium_faction.core.faction;

/**
 * Represents the rank hierarchy within a faction
 */
public enum Rank {
    OWNER,      // Full permissions, can disband faction
    OFFICER,    // Can manage members, claims, relations
    MEMBER,     // Can use faction features, claim land
    RECRUIT;    // Limited permissions, probationary member

    public boolean canClaim() {
        return this != RECRUIT;
    }

    public boolean canInvite() {
        return this == OWNER || this == OFFICER;
    }

    public boolean canPromote() {
        return this == OWNER;
    }

    public boolean canKick() {
        return this == OWNER || this == OFFICER;
    }

    public boolean canManageRelations() {
        return this == OWNER || this == OFFICER;
    }

    public boolean canSetHome() {
        return this == OWNER || this == OFFICER;
    }

    public boolean canDisband() {
        return this == OWNER;
    }

    public boolean canUnclaim() {
        return this != RECRUIT;
    }

    public boolean canManageSettings() {
        return this == OWNER || this == OFFICER;
    }
}