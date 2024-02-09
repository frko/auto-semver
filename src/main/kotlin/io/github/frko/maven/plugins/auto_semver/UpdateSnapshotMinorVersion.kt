package io.github.frko.maven.plugins.auto_semver

import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo

@Mojo(name = "increment-snapshot-minor", defaultPhase = LifecyclePhase.NONE, threadSafe = true)
class UpdateSnapshotMinorVersion: SemverVersionManager() {

    override fun execute() = execute {
        val latest = latestStableSemverForMajor()
        requireSnapshotOrSuffixlessVersion(latest)

        val next = latest.nextMinor().withSuffix("SNAPSHOT")
        log.info("new semver version proposal '${next}'.")
        updateSemverVersion(next)
    }
}
