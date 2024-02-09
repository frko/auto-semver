package io.github.frko.maven.plugins.auto_semver

import com.vdurmont.semver4j.Semver
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo

@Mojo(name = "increment-snapshot-patch", defaultPhase = LifecyclePhase.INITIALIZE, threadSafe = true)
class UpdateSnapshotPatchVersion: SemverVersionManager() {

    override fun execute() = execute {
        val latest = latestStableSemverForMajor()
        requireSnapshotOrSuffixlessVersion(latest)

        val next = latest.nextPatch().withSuffix("SNAPSHOT")
        log.info("new semver version proposal '${next}'.")
        updateSemverVersion(next)
    }
}

internal fun requireSnapshotOrSuffixlessVersion(latest: Semver) {
    val suffixes = latest.suffixTokens
    if (suffixes.size > 1) throw MojoExecutionException(
        "semver version '$latest' has multiple suffixes."
    )

    val suffix = suffixes[0]
    if (suffix != "SNAPSHOT") throw MojoExecutionException(
        "semver version '$latest' has a suffix that is not 'SNAPSHOT'."
    )
}
