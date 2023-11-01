package io.github.frko.maven.plugins.auto_semver

import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo

@Mojo(name = "increment-minor", defaultPhase = LifecyclePhase.NONE, threadSafe = true)
class UpdateMinorVersion: SemverVersionManager() {

    override fun execute() = execute {
        val latest = latestStableSemverForMajor()
        val next = latest.nextMinor()
        log.info("new semver version proposal '${next}'.")
        updateSemverVersion(next)
    }
}
