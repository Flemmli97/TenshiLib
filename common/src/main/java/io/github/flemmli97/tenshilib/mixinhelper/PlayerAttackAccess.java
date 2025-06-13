package io.github.flemmli97.tenshilib.mixinhelper;

public interface PlayerAttackAccess {

    void tenshilib$SetNoStrengthResetState(boolean noReset);

    void tenshilib$SetNoSweeping(boolean noSweeping);

    boolean tenshilib$IsSweepDisabled();
}
