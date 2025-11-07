package fr.eriniumgroup.erinium_faction.common.block.entity;

import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;

/**
 * Interface pour les machines qui peuvent avoir leurs faces configurées
 */
public interface IConfigurableMachine {
    /**
     * Récupère la configuration des faces de cette machine
     */
    @NotNull
    FaceConfiguration getFaceConfiguration();

    /**
     * Définit le mode d'une face spécifique
     */
    void setFaceMode(Direction face, FaceMode mode);

    /**
     * Récupère le mode actuel d'une face
     */
    FaceMode getFaceMode(Direction face);

    /**
     * Active/désactive l'auto-input
     */
    void setAutoInput(boolean enabled);

    /**
     * Récupère l'état de l'auto-input
     */
    boolean isAutoInput();

    /**
     * Active/désactive l'auto-output
     */
    void setAutoOutput(boolean enabled);

    /**
     * Récupère l'état de l'auto-output
     */
    boolean isAutoOutput();

    /**
     * Appelé quand la configuration est modifiée
     */
    void onConfigurationChanged();
}