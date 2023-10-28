package io.github.frko.maven.plugins.auto_semver

import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo

@Mojo(name = "increment-minor", defaultPhase = LifecyclePhase.NONE, threadSafe = true)
class UpdateMinorVersion: SemverVersionManager() {

    override fun execute() {
        if (project.hasParent()) {
            log.info("skipping module '${project.name}'")
            return
        }
        val latest = latestStableSemverForMajor()
        val next = latest.nextMinor()
        updateSemverVersion(next)
    }
}
