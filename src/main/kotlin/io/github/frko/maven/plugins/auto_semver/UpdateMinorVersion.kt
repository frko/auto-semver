package io.github.frko.maven.plugins.auto_semver

import com.vdurmont.semver4j.Semver
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo

@Mojo(name = "increment-minor", defaultPhase = LifecyclePhase.NONE, threadSafe = true)
class UpdateMinorVersion: SemverVersionManager() {

    override fun execute() = execute {
        val latest = latestStableSemverForMajor()
        requireSuffixlessVersion(latest)

        val next = latest.nextMinor()
        log.info("new semver version proposal '${next}'.")
        updateSemverVersion(next)
    }
}

internal fun requireSuffixlessVersion(latest: Semver) {
    val suffixes = latest.suffixTokens
    if (suffixes.isNotEmpty()) throw MojoExecutionException(
        "semver version '$latest' has suffixes."
    )
}
