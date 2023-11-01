package io.github.frko.maven.plugins.auto_semver

import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo

@Mojo(name = "increment-patch", defaultPhase = LifecyclePhase.INITIALIZE, threadSafe = true)
class UpdatePatchVersion: SemverVersionManager() {

    override fun execute() = execute {
        val latest = latestStableSemverForMajor()
        val next = latest.nextPatch()
        log.info("new semver version proposal '${next}'.")
        updateSemverVersion(next)
    }
}
