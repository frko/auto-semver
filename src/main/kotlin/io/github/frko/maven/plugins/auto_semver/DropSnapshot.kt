package io.github.frko.maven.plugins.auto_semver

import com.vdurmont.semver4j.Semver
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo

@Mojo(name = "drop-snapshot", defaultPhase = LifecyclePhase.INITIALIZE, threadSafe = true)
class DropSnapshot: SemverVersionManager() {

    override fun execute() = execute {
        val latest = latestStableSemverForMajor()
        requireSnapshotVersion(latest)

        val next = latest.withClearedSuffix()

        log.info("new semver version proposal '${next}'.")
        updateSemverVersion(next)
    }

    private fun requireSnapshotVersion(latest: Semver) {
        val suffixes = latest.suffixTokens
        if (suffixes.size != 1) throw MojoExecutionException(
            "semver version '$latest' has multiple suffixes."
        )

        val suffix = suffixes[0]
        if (suffix != "SNAPSHOT") throw MojoExecutionException(
            "semver version '$latest' does not have a snapshot suffix."
        )
    }
}
